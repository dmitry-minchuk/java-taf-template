#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path

REGRESSION_SUITES = [
    "studio_issues",
    "studio_smoke",
    "studio_acl",
    "studio_rules_editor",
    "studio_git",
    "studio_sso",
    "service_smoke",
    "studio_open_api",
]

DEFAULT_SUITE_DIR = Path("src/test/resources/testng_suites")


def read_regression_classes(suite_dir: Path, suites: list[str] | None = None, excluded: set[str] | None = None) -> dict[str, str]:
    classes: dict[str, str] = {}
    for suite in suites or REGRESSION_SUITES:
        suite_file = suite_dir / f"{suite}.xml"
        if not suite_file.exists():
            continue
        root = ET.parse(suite_file).getroot()
        for class_node in root.findall(".//class"):
            name = class_node.attrib["name"]
            if (excluded and name in excluded) or name in classes:
                continue
            classes[name] = suite
    return classes
