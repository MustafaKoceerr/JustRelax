"""Tests for the string converter: python3 -m unittest discover -s tools/strings"""
import json
import tempfile
import unittest
from pathlib import Path

from strings import android_value, catalog_json, convert_file, convert_tree, swift_value


class AndroidValueTest(unittest.TestCase):
    def test_apostrophes_are_escaped(self):
        self.assertEqual(r"L\'IA ha bisogno di suoni", android_value("L'IA ha bisogno di suoni"))

    def test_an_escaped_apostrophe_is_not_escaped_twice(self):
        self.assertEqual(r"L\'IA", android_value(r"L\'IA"))

    def test_double_quotes_are_escaped(self):
        self.assertEqual(r'Mix \"%1$s\" saved', android_value('Mix "%1$s" saved'))

    def test_a_leading_at_or_question_mark_is_escaped(self):
        self.assertEqual(r"\@home", android_value("@home"))
        self.assertEqual(r"\?why", android_value("?why"))
        self.assertEqual("Why?", android_value("Why?"))

    def test_format_arguments_and_entities_stay_as_they_are(self):
        self.assertEqual("Downloading… %1$d%%", android_value("Downloading… %1$d%%"))
        self.assertEqual("Rain &amp; thunder", android_value("Rain &amp; thunder"))


class ConvertFileTest(unittest.TestCase):
    def test_only_string_values_change(self):
        source = (
            '<?xml version="1.0" encoding="utf-8"?>\n'
            "<resources>\n"
            "    <!-- Errors -->\n"
            '    <string name="err_ai_empty_response">L\'IA ha restituito una risposta vuota.</string>\n'
            '    <string name="tab_ai">AI</string>\n'
            "</resources>\n"
        )
        expected = (
            '<?xml version="1.0" encoding="utf-8"?>\n'
            "<resources>\n"
            "    <!-- Errors -->\n"
            '    <string name="err_ai_empty_response">L\\\'IA ha restituito una risposta vuota.</string>\n'
            '    <string name="tab_ai">AI</string>\n'
            "</resources>\n"
        )
        self.assertEqual(expected, convert_file(source))


class ConvertTreeTest(unittest.TestCase):
    def test_every_language_folder_lands_under_res_with_the_same_name(self):
        with tempfile.TemporaryDirectory() as tmp:
            compose = Path(tmp, "composeResources")
            for folder in ("values", "values-fr", "drawable"):
                (compose / folder).mkdir(parents=True)
            (compose / "values" / "strings.xml").write_text('<resources><string name="a">It\'s</string></resources>')
            (compose / "values-fr" / "strings.xml").write_text('<resources><string name="a">C\'est</string></resources>')
            res = Path(tmp, "res")

            written = convert_tree(compose, res)

            self.assertEqual({res / "values" / "strings.xml", res / "values-fr" / "strings.xml"}, set(written))
            self.assertEqual('<resources><string name="a">C\\\'est</string></resources>', (res / "values-fr" / "strings.xml").read_text())
            self.assertFalse((res / "drawable").exists())


class SwiftValueTest(unittest.TestCase):
    def test_android_escapes_and_entities_are_undone(self):
        self.assertEqual("L'IA & \"Mix\"", swift_value(r"L\'IA &amp; \"Mix\""))
        self.assertEqual("@home", swift_value(r"\@home"))

    def test_format_arguments_use_swift_specifiers(self):
        self.assertEqual("Mix %1$@ saved, %2$lld left, 40%%", swift_value("Mix %1$s saved, %2$d left, 40%%"))
        self.assertEqual("%@ and %lld", swift_value("%s and %d"))


def write_res(root: Path, folder: str, body: str):
    (root / folder).mkdir(parents=True, exist_ok=True)
    (root / folder / "strings.xml").write_text(f"<resources>{body}</resources>")


class CatalogTest(unittest.TestCase):
    def test_every_module_and_language_lands_in_one_catalog(self):
        with tempfile.TemporaryDirectory() as tmp:
            app, feature = Path(tmp, "app/res"), Path(tmp, "feature/res")
            write_res(app, "values", '<string name="tab_home">Sounds</string>')
            write_res(app, "values-tr", '<string name="tab_home">Sesler</string>')
            write_res(feature, "values", r'<string name="err">It\'s %1$s</string>')

            catalog = json.loads(catalog_json([app, feature]))

            self.assertEqual("en", catalog["sourceLanguage"])
            self.assertEqual({"en": "Sounds", "tr": "Sesler"}, {
                language: unit["stringUnit"]["value"]
                for language, unit in catalog["strings"]["tab_home"]["localizations"].items()
            })
            self.assertEqual("It's %1$@", catalog["strings"]["err"]["localizations"]["en"]["stringUnit"]["value"])
            self.assertEqual("manual", catalog["strings"]["err"]["extractionState"])

    def test_a_key_with_the_same_words_in_two_modules_is_kept_once(self):
        with tempfile.TemporaryDirectory() as tmp:
            a, b = Path(tmp, "a/res"), Path(tmp, "b/res")
            write_res(a, "values", '<string name="action_stop">Stop</string>')
            write_res(b, "values", '<string name="action_stop">Stop</string>')

            self.assertEqual(["action_stop"], list(json.loads(catalog_json([a, b]))["strings"]))

    def test_a_key_with_different_words_in_two_modules_is_an_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            a, b = Path(tmp, "a/res"), Path(tmp, "b/res")
            write_res(a, "values", '<string name="err_unknown">Something went wrong.</string>')
            write_res(b, "values", '<string name="err_unknown">An unexpected error occurred.</string>')

            with self.assertRaisesRegex(ValueError, "err_unknown"):
                catalog_json([a, b])

    def test_region_folders_become_language_tags(self):
        with tempfile.TemporaryDirectory() as tmp:
            res = Path(tmp, "res")
            write_res(res, "values", '<string name="a">A</string>')
            write_res(res, "values-pt-rBR", '<string name="a">B</string>')

            self.assertIn("pt-BR", json.loads(catalog_json([res]))["strings"]["a"]["localizations"])


if __name__ == "__main__":
    unittest.main()
