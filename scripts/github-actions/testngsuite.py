#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path

LISTENERS = [
    "configuration.listeners.AnnotationTransformer",
    "configuration.listeners.ReportPortalExportListener",
    "com.epam.reportportal.testng.ReportPortalTestNGListener",
]


def write_suite(class_names: list[str], output: Path, suite_name: str) -> Path:
    if not class_names:
        raise SystemExit("No test classes were provided")
    suite = ET.Element(
        "suite",
        {"verbose": "1", "name": suite_name, "annotations": "JDK", "parallel": "false", "thread-count": "1"},
    )
    listeners = ET.SubElement(suite, "listeners")
    for listener_class in LISTENERS:
        ET.SubElement(listeners, "listener", {"class-name": listener_class})
    for class_name in class_names:
        test = ET.SubElement(suite, "test", {"name": class_name.rsplit(".", 1)[-1]})
        classes = ET.SubElement(test, "classes")
        ET.SubElement(classes, "class", {"name": class_name})

    tree = ET.ElementTree(suite)
    ET.indent(tree, space="    ")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">\n',
        encoding="utf-8",
    )
    with output.open("a", encoding="utf-8") as handle:
        tree.write(handle, encoding="unicode", xml_declaration=False)
        handle.write("\n")
    return output
