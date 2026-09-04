#!/usr/bin/env python3
import re
from pathlib import Path

TEST_SOURCES = Path("src/test/java")
PACKAGE_PREFIXES = ("tests.ui.webstudio.", "tests.ui.", "tests.")
TEST_ANNOTATION = re.compile(r"^\s*@Test\b", re.M)
LOCAL_ONLY_ANNOTATION = re.compile(r"^\s*@LocalOnly\b", re.M)
EXTENDS = re.compile(r"\bclass\s+(\w+)(?:<[^>]*>)?\s+extends\s+([\w.]+)")


def group_of(class_name: str) -> str:
    package = class_name.rsplit(".", 1)[0] if "." in class_name else ""
    for prefix in PACKAGE_PREFIXES:
        if package.startswith(prefix):
            return package[len(prefix):].replace(".", "_")
    return package.replace(".", "_") or "default"


def is_abstract(text: str, simple_name: str) -> bool:
    return re.search(r"^\s*(?:public\s+|protected\s+|private\s+)?(?:final\s+)?abstract\s+class\s+" + re.escape(simple_name) + r"\b", text, re.M) is not None


def base_class_simple_name(text: str, simple_name: str) -> str | None:
    for match in EXTENDS.finditer(text):
        if match.group(1) == simple_name:
            return match.group(2).rsplit(".", 1)[-1]
    return None


def discover_test_classes(sources: Path = TEST_SOURCES) -> dict[str, bool]:
    files = {source: source.read_text(encoding="utf-8", errors="replace") for source in sorted(sources.rglob("*.java"))}
    by_simple_name = {source.stem: text for source, text in files.items()}

    def has_tests(simple_name: str, seen: set[str]) -> bool:
        text = by_simple_name.get(simple_name)
        if text is None or simple_name in seen:
            return False
        if TEST_ANNOTATION.search(text):
            return True
        base = base_class_simple_name(text, simple_name)
        return base is not None and has_tests(base, seen | {simple_name})

    classes: dict[str, bool] = {}
    for source, text in files.items():
        if is_abstract(text, source.stem) or not has_tests(source.stem, set()):
            continue
        class_name = ".".join(source.relative_to(sources).with_suffix("").parts)
        classes[class_name] = bool(LOCAL_ONLY_ANNOTATION.search(text))
    return classes


def ci_test_classes(sources: Path = TEST_SOURCES) -> list[str]:
    return [name for name, local_only in discover_test_classes(sources).items() if not local_only]
