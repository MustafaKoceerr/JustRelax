"""The app's strings, kept once in Android resources and copied to the other formats.

    python3 tools/strings/strings.py xcstrings           # writes the iOS String Catalog
    python3 tools/strings/strings.py xcstrings --check   # fails if the catalog is out of date (CI)
    python3 tools/strings/strings.py android <composeResources> <res>

`xcstrings` reads every module's src/main/res/values*/strings.xml and writes
iosApp/iosApp/Localizable.xcstrings: Android escapes are undone and format arguments get Swift
specifiers (%1$s -> %1$@, %1$d -> %1$lld). A key must mean the same in every module.

`android` was used once, to move Compose Multiplatform strings to Android resources: Compose
resources take a value as written; Android resources read quotes and apostrophes as markup and a
leading @ or ? as a reference, so those are escaped.
"""
import html
import json
import re
import sys
from pathlib import Path

_STRING = re.compile(r"(<string\b[^>]*>)(.*?)(</string>)", re.DOTALL)
_NAMED_STRING = re.compile(r'<string\b[^>]*\bname="([^"]+)"[^>]*>(.*?)</string>', re.DOTALL)
_ROOT = Path(__file__).resolve().parents[2]
_CATALOG = _ROOT / "iosApp" / "iosApp" / "Localizable.xcstrings"


def android_value(value: str) -> str:
    """A Compose resource string's text, escaped for an Android resource."""
    escaped = re.sub(r"(?<!\\)(['\"])", r"\\\1", value)
    if escaped[:1] in ("@", "?"):
        escaped = "\\" + escaped
    return escaped


def convert_file(text: str) -> str:
    """A whole strings.xml, with each <string> value escaped and the rest untouched."""
    return _STRING.sub(lambda m: m.group(1) + android_value(m.group(2)) + m.group(3), text)


def convert_tree(compose_resources: Path, res: Path) -> list[Path]:
    """Writes res/values*/strings.xml for every composeResources/values*/strings.xml; returns them."""
    written = []
    for source in sorted(compose_resources.glob("values*/strings.xml")):
        target = res / source.parent.name / "strings.xml"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(convert_file(source.read_text(encoding="utf-8")), encoding="utf-8")
        written.append(target)
    return written


def swift_value(value: str) -> str:
    """An Android resource string's text as iOS shows it, with Swift format specifiers."""
    text = re.sub(r"\\([\\'\"@?])", r"\1", value).replace("\\n", "\n")
    text = html.unescape(text)
    text = re.sub(r"%(\d+\$)?s", r"%\1@", text)
    return re.sub(r"%(\d+\$)?d", r"%\1lld", text)


def _language(folder: str) -> str:
    """values -> en (the source language), values-tr -> tr, values-pt-rBR -> pt-BR."""
    if folder == "values":
        return "en"
    return re.sub(r"-r([A-Z]{2})$", r"-\1", folder.removeprefix("values-"))


def catalog_json(res_dirs: list[Path]) -> str:
    """The String Catalog for the strings in [res_dirs], in Xcode's own JSON layout."""
    values: dict[str, dict[str, str]] = {}
    origin: dict[tuple[str, str], Path] = {}
    for res in res_dirs:
        for file in sorted(res.glob("values*/strings.xml")):
            language = _language(file.parent.name)
            for key, raw in _NAMED_STRING.findall(file.read_text(encoding="utf-8")):
                value = swift_value(raw)
                known = values.setdefault(key, {}).get(language)
                if known is not None and known != value:
                    raise ValueError(f"{key} ({language}) differs in {origin[key, language]} and {file}")
                values[key][language] = value
                origin[key, language] = file
    strings = {
        key: {
            "extractionState": "manual",
            "localizations": {
                language: {"stringUnit": {"state": "translated", "value": value}}
                for language, value in sorted(languages.items())
            },
        }
        for key, languages in sorted(values.items())
    }
    catalog = {"sourceLanguage": "en", "strings": strings, "version": "1.0"}
    return json.dumps(catalog, ensure_ascii=False, indent=2, separators=(",", " : ")) + "\n"


def _res_dirs() -> list[Path]:
    return sorted(p for p in _ROOT.glob("**/src/main/res") if "build" not in p.relative_to(_ROOT).parts)


if __name__ == "__main__":
    command = sys.argv[1] if len(sys.argv) > 1 else ""
    if command == "xcstrings":
        text = catalog_json(_res_dirs())
        if "--check" in sys.argv:
            if not _CATALOG.exists() or _CATALOG.read_text(encoding="utf-8") != text:
                sys.exit(f"{_CATALOG} is out of date; run: python3 tools/strings/strings.py xcstrings")
        else:
            _CATALOG.write_text(text, encoding="utf-8")
            print(_CATALOG)
    elif command == "android" and len(sys.argv) == 4:
        for path in convert_tree(Path(sys.argv[2]), Path(sys.argv[3])):
            print(path)
    else:
        sys.exit(__doc__)
