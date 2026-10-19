"""Tests for the string converter: python3 -m unittest discover -s tools/strings"""
import tempfile
import unittest
from pathlib import Path

from strings import android_value, convert_file, convert_tree


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


if __name__ == "__main__":
    unittest.main()
