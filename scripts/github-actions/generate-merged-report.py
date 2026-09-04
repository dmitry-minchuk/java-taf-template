#!/usr/bin/env python3
import argparse
import html
import json
import os
import re
import shutil
import xml.etree.ElementTree as ET
from collections import Counter
from dataclasses import dataclass, field
from pathlib import Path

STATUS_ORDER = {"FAILED": 0, "SKIPPED": 1, "PASSED": 2}
STATUS_STYLE = {
    "PASSED": ("passed", "#1a7f37", "#dafbe1"),
    "FAILED": ("failed", "#cf222e", "#ffebe9"),
    "SKIPPED": ("skipped", "#9a6700", "#fff8c5"),
}


@dataclass
class TestRecord:
    shard: str
    suite: str
    class_name: str
    method: str
    display_name: str
    status: str
    duration_ms: int
    test_case_id: str = ""
    description: str = ""
    error_type: str = ""
    error_message: str = ""
    stack_trace: str = ""
    attachments: list[dict[str, str]] = field(default_factory=list)

    @property
    def short_class(self) -> str:
        return self.class_name.rsplit(".", 1)[-1]


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def shard_name(artifact_dir: Path, input_root: Path) -> str:
    relative = artifact_dir.relative_to(input_root).parts
    return relative[0].removeprefix("openl-tests-") if relative else artifact_dir.name


def suite_name(shard: str, declared_suite: str) -> str:
    if declared_suite and declared_suite != "ad-hoc":
        return declared_suite
    return re.sub(r"-\d+$", "", shard)


def collect_from_rp_export(input_root: Path, output_dir: Path) -> tuple[list[TestRecord], set[Path]]:
    records: list[TestRecord] = []
    covered_artifacts: set[Path] = set()
    for manifest in sorted(input_root.rglob("manifest.json")):
        export_dir = manifest.parent
        artifact_dir = input_root / export_dir.relative_to(input_root).parts[0]
        manifest_data = read_json(manifest)
        shard = shard_name(export_dir, input_root)
        suite = suite_name(shard, str(manifest_data.get("suite") or ""))
        exported_before = len(records)
        for result_file in sorted(export_dir.rglob("result.json")):
            test_dir = result_file.parent
            result = read_json(result_file)
            metadata_file = test_dir / "metadata.json"
            metadata = read_json(metadata_file) if metadata_file.exists() else {}
            class_name = str(metadata.get("className") or test_dir.parent.name)
            method = str(metadata.get("methodName") or test_dir.name)
            record = TestRecord(
                shard=shard,
                suite=suite,
                class_name=class_name,
                method=method,
                display_name=str(metadata.get("displayName") or method),
                status=str(result.get("status") or "SKIPPED").upper(),
                duration_ms=int(result.get("durationMs") or 0),
                test_case_id=str(metadata.get("testCaseId") or ""),
                description=str(metadata.get("description") or ""),
                error_type=str(result.get("errorType") or ""),
                error_message=str(result.get("errorMessage") or ""),
                stack_trace=str(result.get("stackTrace") or ""),
            )
            record.attachments = copy_attachments(test_dir, record, output_dir)
            records.append(record)
        if len(records) > exported_before:
            covered_artifacts.add(artifact_dir)
    return records, covered_artifacts


def copy_attachments(test_dir: Path, record: TestRecord, output_dir: Path) -> list[dict[str, str]]:
    attachments_index = test_dir / "attachments.jsonl"
    copied: list[dict[str, str]] = []
    if not attachments_index.exists():
        return copied
    target_dir = output_dir / "attachments" / record.shard / record.class_name / record.method
    for line in attachments_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        entry = json.loads(line)
        source = test_dir / "attachments" / Path(str(entry.get("path", ""))).name
        if not source.exists():
            continue
        target_dir.mkdir(parents=True, exist_ok=True)
        target = target_dir / source.name
        shutil.copy2(source, target)
        copied.append(
            {
                "message": str(entry.get("message") or source.name),
                "level": str(entry.get("level") or "INFO"),
                "href": target.relative_to(output_dir).as_posix(),
                "kind": attachment_kind(source.suffix.lower()),
            }
        )
    return copied


def attachment_kind(suffix: str) -> str:
    if suffix in {".png", ".jpg", ".jpeg", ".gif"}:
        return "image"
    if suffix in {".webm", ".mp4"}:
        return "video"
    return "file"


def collect_from_testng(input_root: Path, covered_artifacts: set[Path]) -> list[TestRecord]:
    records: list[TestRecord] = []
    for results_file in sorted(input_root.rglob("testng-results.xml")):
        artifact_dir = input_root / results_file.relative_to(input_root).parts[0]
        if artifact_dir in covered_artifacts:
            continue
        shard = shard_name(results_file.parent, input_root)
        root = ET.parse(results_file).getroot()
        for class_node in root.iter("class"):
            class_name = class_node.attrib.get("name", "")
            for method in class_node.findall("test-method"):
                if method.attrib.get("is-config") == "true":
                    continue
                exception = method.find("exception")
                message = exception.findtext("message", default="") if exception is not None else ""
                trace = exception.findtext("full-stacktrace", default="") if exception is not None else ""
                records.append(
                    TestRecord(
                        shard=shard,
                        suite=suite_name(shard, ""),
                        class_name=class_name,
                        method=method.attrib.get("name", ""),
                        display_name=method.attrib.get("name", ""),
                        status={"PASS": "PASSED", "FAIL": "FAILED", "SKIP": "SKIPPED"}.get(method.attrib.get("status", ""), "SKIPPED"),
                        duration_ms=int(method.attrib.get("duration-ms", "0")),
                        error_type=exception.attrib.get("class", "") if exception is not None else "",
                        error_message=(message or "").strip(),
                        stack_trace=(trace or "").strip(),
                    )
                )
    return records


def status_rank(status: str) -> int:
    return STATUS_ORDER.get(status, 1)


def sort_records(records: list[TestRecord]) -> list[TestRecord]:
    return sorted(records, key=lambda r: (status_rank(r.status), r.suite, r.shard, r.class_name, r.method))


def format_duration(duration_ms: int) -> str:
    seconds = round(duration_ms / 1000)
    if seconds < 60:
        return f"{seconds} s"
    if seconds < 3600:
        return f"{seconds // 60} min {seconds % 60:02d} s"
    return f"{seconds // 3600} h {(seconds % 3600) // 60:02d} min"


def render_html(records: list[TestRecord], title: str, run_url: str, build: str) -> str:
    counts = Counter(record.status for record in records)
    total = len(records)
    total_duration = sum(record.duration_ms for record in records)
    suites = sorted({record.suite for record in records})
    rows = []
    for index, record in enumerate(records):
        css, color, background = STATUS_STYLE.get(record.status, STATUS_STYLE["SKIPPED"])
        details = []
        if record.description:
            details.append(f"<p class='description'>{html.escape(record.description)}</p>")
        if record.error_message or record.stack_trace:
            details.append(
                "<details open><summary>"
                + html.escape(record.error_type or "Failure")
                + "</summary><pre>"
                + html.escape((record.error_message or "").strip())
                + ("\n\n" + html.escape(record.stack_trace) if record.stack_trace else "")
                + "</pre></details>"
            )
        if record.attachments:
            items = []
            for attachment in record.attachments:
                label = html.escape(attachment["message"])
                href = html.escape(attachment["href"])
                if attachment["kind"] == "image":
                    items.append(f"<figure><a href='{href}' target='_blank'><img src='{href}' alt='{label}' loading='lazy'></a><figcaption>{label}</figcaption></figure>")
                elif attachment["kind"] == "video":
                    items.append(f"<figure><video controls preload='metadata' src='{href}'></video><figcaption>{label}</figcaption></figure>")
                else:
                    items.append(f"<figure><a href='{href}' target='_blank'>{label}</a></figure>")
            details.append("<div class='attachments'>" + "".join(items) + "</div>")
        detail_html = "".join(details)
        case_id = html.escape(record.test_case_id) if record.test_case_id else ""
        toggle = f"<button class='toggle' data-target='d{index}'>details</button>" if detail_html else ""
        rows.append(
            f"<tr class='row {css}' data-status='{css}' data-suite='{html.escape(record.suite)}'>"
            f"<td><span class='badge' style='color:{color};background:{background}'>{record.status}</span></td>"
            f"<td>{html.escape(record.suite)}<br><small>{html.escape(record.shard)}</small></td>"
            f"<td><strong>{html.escape(record.short_class)}</strong><br>{html.escape(record.display_name)}"
            + (f"<br><small>{case_id}</small>" if case_id else "")
            + f"</td><td>{format_duration(record.duration_ms)}</td>"
            f"<td>{toggle}</td></tr>"
            + (f"<tr class='details-row {css}' id='d{index}' hidden><td colspan='5'>{detail_html}</td></tr>" if detail_html else "")
        )
    suite_options = "".join(f"<option value='{html.escape(s)}'>{html.escape(s)}</option>" for s in suites)
    return f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><title>{html.escape(title)}</title>
<style>
body{{font-family:-apple-system,Segoe UI,Helvetica,Arial,sans-serif;margin:24px;color:#1f2328;background:#f6f8fa}}
h1{{margin:0 0 4px}} .meta{{color:#59636e;margin-bottom:16px}}
.cards{{display:flex;gap:12px;margin:16px 0}} .card{{background:#fff;border:1px solid #d0d7de;border-radius:8px;padding:12px 16px;min-width:120px}}
.card b{{display:block;font-size:24px}} .controls{{display:flex;gap:12px;align-items:center;margin-bottom:12px}}
table{{width:100%;border-collapse:collapse;background:#fff;border:1px solid #d0d7de;border-radius:8px}} th,td{{padding:8px 10px;border-top:1px solid #d0d7de;text-align:left;vertical-align:top}}
th{{background:#f6f8fa}} .badge{{font-weight:600;padding:2px 8px;border-radius:12px;font-size:12px}}
pre{{white-space:pre-wrap;word-break:break-word;background:#f6f8fa;padding:8px;border-radius:6px;max-height:420px;overflow:auto}}
.attachments{{display:flex;flex-wrap:wrap;gap:12px}} figure{{margin:0;max-width:420px}} figure img,figure video{{max-width:420px;border:1px solid #d0d7de;border-radius:6px}}
figcaption,small{{color:#59636e;font-size:12px}} .toggle{{cursor:pointer}} .description{{margin:0 0 8px}}
</style></head><body>
<h1>{html.escape(title)}</h1>
<div class="meta">Build {html.escape(build)} · {total} tests · {format_duration(total_duration)} of test time · <a href="{html.escape(run_url)}">workflow run</a></div>
<div class="cards"><div class="card">Passed<b style="color:#1a7f37">{counts.get('PASSED', 0)}</b></div><div class="card">Failed<b style="color:#cf222e">{counts.get('FAILED', 0)}</b></div><div class="card">Skipped<b style="color:#9a6700">{counts.get('SKIPPED', 0)}</b></div></div>
<div class="controls"><label>Status <select id="status"><option value="">all</option><option value="failed">failed</option><option value="skipped">skipped</option><option value="passed">passed</option></select></label>
<label>Suite <select id="suite"><option value="">all</option>{suite_options}</select></label><input id="search" placeholder="filter by class or test name"></div>
<table><thead><tr><th>Status</th><th>Suite / shard</th><th>Test</th><th>Duration</th><th></th></tr></thead><tbody>{''.join(rows)}</tbody></table>
<script>
const rows=[...document.querySelectorAll('tr.row')];
function apply(){{const s=document.getElementById('status').value,u=document.getElementById('suite').value,q=document.getElementById('search').value.toLowerCase();
rows.forEach(r=>{{const ok=(!s||r.dataset.status===s)&&(!u||r.dataset.suite===u)&&(!q||r.textContent.toLowerCase().includes(q));r.hidden=!ok;const d=r.nextElementSibling;if(d&&d.classList.contains('details-row')&&!ok)d.hidden=true;}});}}
['status','suite'].forEach(id=>document.getElementById(id).addEventListener('change',apply));document.getElementById('search').addEventListener('input',apply);
document.querySelectorAll('button.toggle').forEach(b=>b.addEventListener('click',()=>{{const d=document.getElementById(b.dataset.target);d.hidden=!d.hidden;}}));
</script></body></html>
"""


def write_step_summary(records: list[TestRecord], title: str, build: str, summary_path: str | None) -> str:
    counts = Counter(record.status for record in records)
    lines = [
        f"## {title}",
        "",
        f"Build `{build}` · {len(records)} tests · ✅ {counts.get('PASSED', 0)} passed · ❌ {counts.get('FAILED', 0)} failed · ⏭️ {counts.get('SKIPPED', 0)} skipped",
        "",
    ]
    failed = [record for record in records if record.status != "PASSED"]
    if failed:
        lines += ["| Status | Suite | Test | Error |", "|---|---|---|---|"]
        for record in failed:
            error = (record.error_message or record.error_type or "").strip().splitlines()
            first_line = (error[0][:200] if error else "").replace("|", "\\|")
            lines.append(f"| {record.status} | {record.suite} | `{record.short_class}.{record.method}` | {first_line} |")
    else:
        lines.append("All tests passed.")
    lines += ["", "The merged HTML report with screenshots and videos is attached as the `test-report-merged` artifact."]
    text = "\n".join(lines) + "\n"
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as handle:
            handle.write(text)
    return text


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Merge shard results (rp-export or testng-results.xml) into one HTML report and a job summary."
    )
    parser.add_argument("--input-root", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--title", default="OpenL Tests")
    parser.add_argument("--build", default="")
    parser.add_argument("--run-url", default="")
    parser.add_argument("--step-summary", default=os.environ.get("GITHUB_STEP_SUMMARY"))
    args = parser.parse_args()

    if args.output_dir.exists():
        shutil.rmtree(args.output_dir)
    args.output_dir.mkdir(parents=True)
    rp_records, covered = collect_from_rp_export(args.input_root, args.output_dir)
    records = sort_records(rp_records + collect_from_testng(args.input_root, covered))
    if not records:
        message = f"No test results found under {args.input_root}"
        if args.step_summary:
            with open(args.step_summary, "a", encoding="utf-8") as handle:
                handle.write(f"## {args.title}\n\n{message}\n")
        raise SystemExit(message)

    (args.output_dir / "index.html").write_text(render_html(records, args.title, args.run_url, args.build), encoding="utf-8")
    (args.output_dir / "summary.json").write_text(
        json.dumps(
            {
                "build": args.build,
                "total": len(records),
                "counts": dict(Counter(record.status for record in records)),
                "tests": [
                    {
                        "suite": r.suite,
                        "shard": r.shard,
                        "class": r.class_name,
                        "method": r.method,
                        "status": r.status,
                        "durationMs": r.duration_ms,
                        "testCaseId": r.test_case_id,
                        "errorMessage": r.error_message,
                    }
                    for r in records
                ],
            },
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(write_step_summary(records, args.title, args.build, args.step_summary))


if __name__ == "__main__":
    main()
