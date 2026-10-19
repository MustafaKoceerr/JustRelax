"""Copies Compose Multiplatform string resources to Android resources.

    python3 tools/strings/strings.py <module>/src/commonMain/composeResources <module>/src/main/res

Compose resources take a value as written; Android resources read quotes and apostrophes as markup
and a leading @ or ? as a reference, so those are escaped. Everything else (format arguments such
as %1$s and %%, XML entities, comments) is copied as it is. Language folders keep their names.
"""
import re
import sys
from pathlib import Path

_STRING = re.compile(r"(<string\b[^>]*>)(.*?)(</string>)", re.DOTALL)


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


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    for path in convert_tree(Path(sys.argv[1]), Path(sys.argv[2])):
        print(path)
