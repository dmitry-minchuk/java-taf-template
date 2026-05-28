#!/usr/bin/env python3
import argparse
import xml.etree.ElementTree as ET
from pathlib import Path


LISTENERS = [
    "configuration.listeners.AnnotationTransformer",
    "com.epam.reportportal.testng.ReportPortalTestNGListener",
]


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a TestNG suite for a comma-separated class list.")
    parser.add_argument("--classes", required=True, help="Comma-separated fully qualified test class names.")
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--suite-name", default="GitHub Actions Dynamic Shard")
    args = parser.parse_args()

    class_names = [value.strip() for value in args.classes.split(",") if value.strip()]
    if not class_names:
        raise SystemExit("No test classes were provided")

    suite = ET.Element("suite", {
        "verbose": "1",
        "name": args.suite_name,
        "annotations": "JDK",
        "parallel": "false",
        "thread-count": "1",
    })
    listeners = ET.SubElement(suite, "listeners")
    for listener_class in LISTENERS:
        ET.SubElement(listeners, "listener", {"class-name": listener_class})

    for class_name in class_names:
        test = ET.SubElement(suite, "test", {"name": class_name.rsplit(".", 1)[-1]})
        classes = ET.SubElement(test, "classes")
        ET.SubElement(classes, "class", {"name": class_name})

    tree = ET.ElementTree(suite)
    ET.indent(tree, space="    ")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">\n',
        encoding="utf-8",
    )
    with args.output.open("a", encoding="utf-8") as handle:
        tree.write(handle, encoding="unicode", xml_declaration=False)
        handle.write("\n")


if __name__ == "__main__":
    main()
