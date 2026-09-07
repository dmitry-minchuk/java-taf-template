#!/usr/bin/env python3
import argparse
import html
import json
import os
import re
import shutil
import sys
from urllib.parse import quote
from collections import Counter
from dataclasses import dataclass, field
from pathlib import Path
from string import Template

sys.path.insert(0, str(Path(__file__).resolve().parent))
from testgroups import group_of  # noqa: E402

KNOWN_ISSUE = "KNOWN ISSUE"
FIXED_CANDIDATE = "FIXED?"
STATUS_ORDER = {"FAILED": 0, "SKIPPED": 1, KNOWN_ISSUE: 2, FIXED_CANDIDATE: 3, "PASSED": 4}
STATUS_STYLE = {
    "PASSED": ("passed", "#1a7f37", "#dafbe1"),
    "FAILED": ("failed", "#cf222e", "#ffebe9"),
    "SKIPPED": ("skipped", "#9a6700", "#fff8c5"),
    KNOWN_ISSUE: ("known", "#bc4c00", "#fff1e5"),
    FIXED_CANDIDATE: ("fixed", "#0969da", "#ddf4ff"),
}
STEPS_TAIL_LINES = 250
APP_LOG_EXTRACT_LINES = 200
APP_LOG_MARKERS = re.compile(r"\b(ERROR|WARN|WARNING|SEVERE)\b|\w*(Exception|Error)\b|^Caused by")
STEPS_TAIL_LINES_PASSED = 50
TRACE_HINT = "npx playwright show-trace trace.zip (or drop the file onto https://trace.playwright.dev)"
REPORT_EXCLUDED_KINDS = {"video"}


@dataclass
class Attachment:
    message: str
    level: str
    href: str
    kind: str
    path: Path


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
    started_at: str = ""
    finished_at: str = ""
    git_sha: str = ""
    git_branch: str = ""
    repository: str = ""
    known_issue: str = ""
    known_issue_url: str = ""
    attachments: list[Attachment] = field(default_factory=list)

    @property
    def outcome(self) -> str:
        if self.known_issue and self.status == "FAILED":
            return KNOWN_ISSUE
        if self.known_issue and self.status == "PASSED":
            return FIXED_CANDIDATE
        return self.status

    @property
    def short_class(self) -> str:
        return self.class_name.rsplit(".", 1)[-1]

    @property
    def slug(self) -> str:
        return re.sub(r"[^A-Za-z0-9._-]+", "_", f"{self.class_name}.{self.display_name}")

    def first(self, kind: str) -> Attachment | None:
        return next((a for a in self.attachments if a.kind == kind), None)

    def all(self, *kinds: str) -> list[Attachment]:
        return [a for a in self.attachments if a.kind in kinds]

    def source_url(self) -> str:
        if not self.repository or not self.git_sha:
            return ""
        return f"https://github.com/{self.repository}/blob/{self.git_sha}/src/test/java/{self.class_name.replace('.', '/')}.java"


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")


def shard_name(artifact_dir: Path, input_root: Path) -> str:
    relative = artifact_dir.relative_to(input_root).parts
    return relative[0].removeprefix("openl-tests-") if relative else artifact_dir.name


def group_label(declared: str, class_name: str) -> str:
    if not declared or declared == "ad-hoc" or declared.startswith("OpenL GHA "):
        return group_of(class_name)
    return declared


def collect_from_rp_export(input_root: Path, output_dir: Path) -> tuple[list[TestRecord], set[Path]]:
    records: list[TestRecord] = []
    covered_artifacts: set[Path] = set()
    for manifest in sorted(input_root.rglob("manifest.json")):
        export_dir = manifest.parent
        artifact_dir = input_root / export_dir.relative_to(input_root).parts[0]
        manifest_data = read_json(manifest)
        shard = shard_name(export_dir, input_root)
        exported_before = len(records)
        for result_file in sorted(export_dir.rglob("result.json")):
            test_dir = result_file.parent
            result = read_json(result_file)
            metadata_file = test_dir / "metadata.json"
            metadata = read_json(metadata_file) if metadata_file.exists() else {}
            known_issue = result.get("knownIssue") or {}
            class_name = str(metadata.get("className") or test_dir.parent.name)
            method = str(metadata.get("methodName") or test_dir.name)
            record = TestRecord(
                shard=shard,
                suite=group_label(str(metadata.get("suite") or ""), class_name),
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
                started_at=str(result.get("startedAt") or metadata.get("startedAt") or ""),
                finished_at=str(result.get("finishedAt") or ""),
                git_sha=str(manifest_data.get("gitSha") or ""),
                git_branch=str(manifest_data.get("gitBranch") or ""),
                repository=str(manifest_data.get("githubRepository") or ""),
                known_issue=str(known_issue.get("ticket") or ""),
                known_issue_url=str(known_issue.get("url") or ""),
            )
            record.attachments = copy_attachments(test_dir, record, output_dir)
            records.append(record)
        if len(records) > exported_before:
            covered_artifacts.add(artifact_dir)
    return records, covered_artifacts


def copy_attachments(test_dir: Path, record: TestRecord, output_dir: Path) -> list[Attachment]:
    attachments_index = test_dir / "attachments.jsonl"
    copied: list[Attachment] = []
    if not attachments_index.exists():
        return copied
    target_dir = output_dir / "attachments" / record.shard / record.class_name / re.sub(r"[^A-Za-z0-9._-]+", "_", record.display_name)
    for line in attachments_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        entry = json.loads(line)
        source = test_dir / "attachments" / Path(str(entry.get("path", ""))).name
        if not source.exists():
            continue
        message = str(entry.get("message") or source.name)
        kind = attachment_kind(message, source.name)
        if kind in REPORT_EXCLUDED_KINDS:
            continue
        target_dir.mkdir(parents=True, exist_ok=True)
        target = target_dir / source.name
        shutil.copy2(source, target)
        copied.append(
            Attachment(
                message=message,
                level=str(entry.get("level") or "INFO"),
                href=quote(target.relative_to(output_dir).as_posix()),
                kind=kind,
                path=target,
            )
        )
    return copied


def attachment_kind(message: str, file_name: str) -> str:
    lowered = message.lower()
    suffix = Path(file_name).suffix.lower()
    if file_name.endswith("test-steps.log") or "steps log" in lowered:
        return "steps"
    if "application log" in lowered:
        return "applog"
    if suffix == ".zip" and "trace" in lowered:
        return "trace"
    if "execution information" in lowered:
        return "execinfo"
    if suffix in {".png", ".jpg", ".jpeg", ".gif"}:
        return "image"
    if suffix in {".webm", ".mp4"}:
        return "video"
    if suffix in {".html", ".htm"}:
        return "page"
    return "file"


def status_rank(status: str) -> int:
    return STATUS_ORDER.get(status, 1)


def sort_records(records: list[TestRecord]) -> list[TestRecord]:
    return sorted(records, key=lambda r: (status_rank(r.outcome), r.suite, r.shard, r.class_name, r.method))


def known_issue_link(record: TestRecord) -> str:
    if not record.known_issue:
        return ""
    if record.known_issue_url:
        return f"<a href='{e(record.known_issue_url)}' target='_blank'>{e(record.known_issue)}</a>"
    return e(record.known_issue)


def format_duration(duration_ms: int) -> str:
    seconds = round(duration_ms / 1000)
    if seconds < 60:
        return f"{seconds} s"
    if seconds < 3600:
        return f"{seconds // 60} min {seconds % 60:02d} s"
    return f"{seconds // 3600} h {(seconds % 3600) // 60:02d} min"


def first_error_line(record: TestRecord, limit: int) -> str:
    text = (record.error_message or record.error_type or "").strip()
    playwright_message = re.search(r"message='(.*?)'?\s*$", text, re.M)
    if playwright_message:
        return playwright_message.group(1).strip()[:limit]
    for line in text.splitlines():
        candidate = line.strip()
        if candidate and candidate not in {"Error {", "{", "}"}:
            return candidate[:limit]
    return ""


def tail_lines(text: str, limit: int) -> tuple[list[str], int]:
    lines = text.splitlines()
    return lines[-limit:], len(lines)


def app_log_extract(text: str, limit: int) -> tuple[list[str], int]:
    lines = text.splitlines()
    picked: list[str] = []
    index = 0
    while index < len(lines) and len(picked) < limit:
        line = lines[index]
        if APP_LOG_MARKERS.search(line):
            picked.append(line)
            follow = index + 1
            while follow < len(lines) and len(picked) < limit and (lines[follow].startswith(("\t", " ", "Caused by")) or lines[follow].startswith("at ")):
                picked.append(lines[follow])
                follow += 1
            index = follow
            continue
        index += 1
    return picked, len(lines)


def e(value: str) -> str:
    return html.escape(value or "")


def render_pre(lines: list[str], css: str = "") -> str:
    return f"<pre class='{css}'>{e(chr(10).join(lines))}</pre>"


def render_tabs(record: TestRecord, index: int, build: str) -> str:
    tabs: list[tuple[str, str]] = []

    overview_rows = [
        ("Group / shard", f"{e(record.suite)} / {e(record.shard)}"),
        ("Class", f"<code>{e(record.class_name)}</code>" + (f" · <a href='{e(record.source_url())}' target='_blank'>source</a>" if record.source_url() else "")),
        ("Method", f"<code>{e(record.display_name)}</code>"),
        ("Test case", e(record.test_case_id) or "—"),
        ("Known issue", (known_issue_link(record) + (" — the test still fails as expected" if record.outcome == KNOWN_ISSUE else " — the test PASSED: verify the fix and remove @KnownIssue")) if record.known_issue else "—"),
        ("Description", e(record.description) or "—"),
        ("Started / finished", f"{e(record.started_at) or '—'} → {e(record.finished_at) or '—'} ({format_duration(record.duration_ms)})"),
        ("Application build", e(build) or "—"),
        ("Tests revision", (f"{e(record.git_branch)} @ {e(record.git_sha[:12])}" if record.git_sha else "—")),
    ]
    execinfo = record.first("execinfo")
    if execinfo is not None:
        overview_rows.append(("Execution info", render_pre(read_text(execinfo.path).splitlines(), "small")))
    if record.status != "PASSED":
        overview_rows.append(("AI debug bundle", f"<a href='debug/index.json' target='_blank'>debug/index.json</a> lists the bundle file of this test — everything below in one JSON file, see <a href='debug/README.md' target='_blank'>debug/README.md</a>"))
    tabs.append(("Overview", "<table class='kv'>" + "".join(f"<tr><th>{k}</th><td>{v}</td></tr>" for k, v in overview_rows) + "</table>"))

    if record.error_message or record.stack_trace:
        failure = f"<p class='error-type'>{e(record.error_type or 'Failure')}</p>"
        failure += f"<pre class='error'>{e(record.error_message.strip())}</pre>"
        if record.stack_trace:
            failure += f"<details open><summary>Stack trace</summary><pre>{e(record.stack_trace)}</pre></details>"
        tabs.append(("Failure", failure))

    steps = record.first("steps")
    if steps is not None:
        lines, total = tail_lines(read_text(steps.path), STEPS_TAIL_LINES if record.status != "PASSED" else STEPS_TAIL_LINES_PASSED)
        header = f"<p class='hint'>Framework log of this test: container start, every page action, waits, REST calls and assertions. Showing the last {len(lines)} of {total} lines · <a href='{e(steps.href)}' target='_blank'>full log</a></p>"
        tabs.append((f"Steps ({total})", header + render_pre(lines, "log")))

    applog = record.first("applog")
    if applog is not None:
        lines, total = app_log_extract(read_text(applog.path), APP_LOG_EXTRACT_LINES)
        header = f"<p class='hint'>Application container log (OpenL Studio / Rule Services). Showing {len(lines)} lines with WARN, ERROR or exceptions out of {total} · <a href='{e(applog.href)}' target='_blank'>full log</a></p>"
        body = render_pre(lines, "log") if lines else "<p class='hint'>No warnings or errors in the application log.</p>"
        tabs.append(("Application log", header + body))

    trace = record.first("trace")
    if trace is not None:
        size_kb = max(1, trace.path.stat().st_size // 1024)
        body = (
            f"<p><a class='button' href='{e(trace.href)}' download>Download trace.zip ({size_kb} KB)</a></p>"
            f"<p class='hint'>Playwright trace with DOM snapshots before and after every action, network and console. Open it with <code>{e(TRACE_HINT)}</code>.</p>"
        )
        tabs.append(("Trace", body))

    media = record.all("image", "video", "page", "file")
    if media:
        items = []
        for attachment in media:
            label = e(attachment.message)
            href = e(attachment.href)
            if attachment.kind == "image":
                items.append(f"<figure><a href='{href}' target='_blank'><img src='{href}' alt='{label}' loading='lazy'></a><figcaption>{label}</figcaption></figure>")
            elif attachment.kind == "video":
                items.append(f"<figure><video controls preload='metadata' src='{href}'></video><figcaption>{label}</figcaption></figure>")
            else:
                items.append(f"<figure><a href='{href}' target='_blank'>{label}</a><figcaption>{e(Path(attachment.href).name)}</figcaption></figure>")
        tabs.append(("Media", "<div class='attachments'>" + "".join(items) + "</div>"))

    buttons = "".join(
        f"<button class='tab{' active' if i == 0 else ''}' data-tab='t{index}-{i}'>{e(name)}</button>" for i, (name, _) in enumerate(tabs)
    )
    panels = "".join(
        f"<div class='panel' id='t{index}-{i}'{'' if i == 0 else ' hidden'}>{body}</div>" for i, (_, body) in enumerate(tabs)
    )
    return f"<div class='tabs'>{buttons}</div>{panels}"


PAGE_TEMPLATE = Template("""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>$title</title>
<style>
:root{--bg:#f3f5f9;--surface:#ffffff;--surface-2:#f8fafc;--line:#e3e8ef;--line-strong:#d0d7e2;--text:#0f172a;--muted:#64748b;--accent:#2563eb;--accent-soft:#e8f0fe;--shadow:0 1px 2px rgba(15,23,42,.06),0 1px 3px rgba(15,23,42,.04);--radius:14px;--mono:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace}
*{box-sizing:border-box}
body{margin:0;font-family:Inter,ui-sans-serif,-apple-system,"Segoe UI",Roboto,Helvetica,Arial,sans-serif;font-size:14px;line-height:1.5;color:var(--text);background:var(--bg);-webkit-font-smoothing:antialiased}
a{color:var(--accent);text-decoration:none} a:hover{text-decoration:underline}
.page{max-width:1440px;margin:0 auto;padding:32px 28px 64px}
header.top{display:flex;flex-wrap:wrap;align-items:baseline;justify-content:space-between;gap:12px;margin-bottom:20px}
h1{margin:0;font-size:26px;font-weight:700;letter-spacing:-.02em}
.meta{display:flex;flex-wrap:wrap;gap:8px;align-items:center;color:var(--muted);font-size:13px}
.meta .chip{background:var(--surface);border:1px solid var(--line);border-radius:999px;padding:3px 10px;color:var(--text)}
.cards{display:grid;grid-template-columns:repeat(6,minmax(0,1fr));gap:12px;margin:0 0 20px}
.card{position:relative;overflow:hidden;min-width:0;background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);padding:14px 14px 14px 18px;box-shadow:var(--shadow);color:var(--muted);font-size:clamp(10px,.85vw,12px);font-weight:600;letter-spacing:.04em;text-transform:uppercase;white-space:nowrap;text-overflow:ellipsis}
.card::before{content:"";position:absolute;left:0;top:0;bottom:0;width:4px;background:var(--card-color,var(--line-strong))}
.card b{display:block;margin-top:4px;font-size:clamp(20px,2vw,28px);line-height:1.1;font-weight:700;letter-spacing:-.02em;color:var(--card-color,var(--text))}
.controls{position:sticky;top:0;z-index:5;display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin:0 0 14px;padding:12px 14px;background:rgba(255,255,255,.88);backdrop-filter:blur(8px);border:1px solid var(--line);border-radius:var(--radius);box-shadow:var(--shadow)}
.controls label{display:inline-flex;align-items:center;gap:8px;color:var(--muted);font-size:13px}
.controls select,.controls input{height:36px;padding:0 12px;border:1px solid var(--line-strong);border-radius:10px;background:var(--surface);color:var(--text);font:inherit;font-size:13px}
.controls input{flex:1 1 260px;min-width:200px}
.controls select:focus,.controls input:focus{outline:2px solid var(--accent-soft);border-color:var(--accent)}
.controls button{height:36px;padding:0 14px;border:1px solid var(--line-strong);border-radius:10px;background:var(--surface);color:var(--text);font:inherit;font-size:13px;font-weight:600;cursor:pointer}
.controls button:hover{border-color:var(--accent);color:var(--accent)}
.table-wrap{background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);box-shadow:var(--shadow);overflow-x:auto}
.table-wrap table.tests{min-width:760px}
table.tests{width:100%;table-layout:fixed;border-collapse:separate;border-spacing:0}
table.tests col.c-status{width:19%} table.tests col.c-group{width:10%} table.tests col.c-duration{width:88px} table.tests col.c-artifacts{width:13%} table.tests col.c-details{width:112px}
table.tests>thead th{padding:12px 14px;text-align:left;font-size:11px;font-weight:600;letter-spacing:.06em;text-transform:uppercase;color:var(--muted);background:var(--surface-2);border-bottom:1px solid var(--line)}
table.tests>tbody>tr>td{padding:12px 14px;border-bottom:1px solid var(--line);vertical-align:top;overflow-wrap:anywhere;min-width:0}
tr.row:hover td{background:var(--surface-2)}
.badge{display:inline-flex;flex-wrap:wrap;align-items:center;gap:4px 7px;font-weight:600;padding:3px 10px 3px 8px;border-radius:999px;font-size:12px;line-height:1.4}
.badge::before{content:"";width:8px;height:8px;border-radius:50%;background:currentColor;flex:none}
.badge a{color:inherit;text-decoration:underline;text-decoration-color:rgba(0,0,0,.25)}
strong{font-weight:600}
small,.hint,figcaption{color:var(--muted);font-size:12px}
small.err{display:inline-block;margin-top:2px;color:#b42318;font-family:var(--mono);font-size:11px;overflow-wrap:anywhere}
.artifacts small{display:inline-block;margin:0 6px 4px 0;padding:1px 8px;border:1px solid var(--line);border-radius:999px;background:var(--surface-2);color:var(--muted);font-size:11px}
button.toggle{display:inline-flex;align-items:center;gap:6px;height:30px;padding:0 10px 0 12px;border:1px solid var(--line-strong);border-radius:999px;background:var(--surface);color:var(--text);font:inherit;font-size:12px;font-weight:600;cursor:pointer;white-space:nowrap}
button.toggle::after{content:"";width:6px;height:6px;border-right:1.5px solid currentColor;border-bottom:1.5px solid currentColor;transform:rotate(45deg) translateY(-1px);transition:transform .15s ease}
button.toggle:hover{border-color:var(--accent);color:var(--accent)}
button.toggle.open{background:var(--accent-soft);border-color:var(--accent);color:var(--accent)} button.toggle.open::after{transform:rotate(-135deg) translateY(-1px)}
table.tests>tbody>tr.details-row>td{padding:16px 18px 20px;background:var(--surface-2)}
.tabs{display:inline-flex;flex-wrap:wrap;gap:2px;margin-bottom:12px;padding:4px;background:#e9edf3;border-radius:12px}
.tab{padding:6px 14px;border:0;border-radius:9px;background:transparent;color:#475569;font:inherit;font-size:13px;font-weight:600;cursor:pointer}
.tab:hover{color:var(--text)} .tab.active{background:var(--surface);color:var(--text);box-shadow:var(--shadow)}
.panel{background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);padding:16px 18px;box-shadow:var(--shadow)}
table.kv{border-collapse:collapse;width:100%} table.kv th{width:180px;text-align:left;padding:6px 16px 6px 0;color:var(--muted);font-weight:500;vertical-align:top;font-size:13px} table.kv td{padding:6px 0;border-bottom:1px solid var(--line)} table.kv tr:last-child td{border-bottom:0}
pre{margin:0;white-space:pre-wrap;word-break:break-word;background:var(--surface-2);border:1px solid var(--line);padding:12px 14px;border-radius:10px;max-height:520px;overflow:auto;font-family:var(--mono);font-size:12px;line-height:1.5}
pre.error{background:#fff1f0;border-color:#fecdca;color:#912018} pre.log{background:#0b1220;border-color:#1e293b;color:#dbe4f0} pre.small{max-height:200px}
.error-type{margin:0 0 8px;font-weight:600;color:#b42318;font-family:var(--mono);font-size:12px}
details summary{cursor:pointer;margin:12px 0 8px;font-weight:600;color:var(--muted)}
.hint{margin:0 0 10px}
.attachments{display:flex;flex-wrap:wrap;gap:16px} figure{margin:0;max-width:420px} figure img,figure video{display:block;max-width:420px;border:1px solid var(--line);border-radius:10px;box-shadow:var(--shadow)} figcaption{margin-top:6px}
.button{display:inline-flex;align-items:center;height:36px;padding:0 16px;background:var(--accent);color:#fff;border-radius:10px;font-weight:600;text-decoration:none;box-shadow:var(--shadow)} .button:hover{text-decoration:none;filter:brightness(1.05)}
code{padding:1px 6px;border-radius:6px;background:#eef2f6;font-family:var(--mono);font-size:12px}
@media (max-width:900px){.page{padding:20px 14px 48px} h1{font-size:22px} .controls{position:static} .cards{gap:8px} .card{padding:10px 10px 10px 14px}}
</style></head><body>
<div class="page">
<header class="top"><h1>$title</h1>
<div class="meta"><span class="chip">Build $build</span><span class="chip">$total tests</span><span class="chip">$total_duration of test time</span><a href="$run_url">workflow run</a> · <a href="debug/index.json">debug/index.json</a> · <a href="debug/README.md">how to debug with an AI assistant</a></div></header>
<div class="cards"><div class="card" style="--card-color:#15803d">Passed<b>$passed</b></div><div class="card" style="--card-color:#dc2626">Failed<b>$failed</b></div><div class="card" style="--card-color:#ca8a04">Skipped<b>$skipped</b></div><div class="card" style="--card-color:#ea580c">Known issues<b>$known</b></div><div class="card" style="--card-color:#2563eb">Fixed?<b>$fixed</b></div><div class="card">Shards<b>$shards</b></div></div>
<div class="controls"><label>Status <select id="status"><option value="">all</option><option value="failed">failed</option><option value="skipped">skipped</option><option value="known">known issue</option><option value="fixed">fixed?</option><option value="passed">passed</option></select></label>
<label>Group <select id="suite"><option value="">all</option>$suite_options</select></label><input id="search" placeholder="filter by class, test name or error" size="40"><button id="expand-failed">expand all failed</button></div>
<div class="table-wrap"><table class="tests"><colgroup><col class="c-status"><col class="c-group"><col class="c-test"><col class="c-duration"><col class="c-artifacts"><col class="c-details"></colgroup><thead><tr><th>Status</th><th>Group / shard</th><th>Test</th><th>Duration</th><th>Artifacts</th><th></th></tr></thead><tbody>$rows</tbody></table></div>
</div>
<script>
const rows=[...document.querySelectorAll('tr.row')];
function apply(){const s=document.getElementById('status').value,u=document.getElementById('suite').value,q=document.getElementById('search').value.toLowerCase();
rows.forEach(r=>{const ok=(!s||r.dataset.status===s)&&(!u||r.dataset.suite===u)&&(!q||(r.textContent+' '+(r.dataset.error||'')).toLowerCase().includes(q));r.hidden=!ok;const d=r.nextElementSibling;if(d&&d.classList.contains('details-row')&&!ok){d.hidden=true;r.querySelector('button.toggle')?.classList.remove('open');}});}
['status','suite'].forEach(id=>document.getElementById(id).addEventListener('change',apply));document.getElementById('search').addEventListener('input',apply);
document.querySelectorAll('button.toggle').forEach(b=>b.addEventListener('click',()=>{const d=document.getElementById(b.dataset.target);d.hidden=!d.hidden;b.classList.toggle('open',!d.hidden);}));
document.getElementById('expand-failed').addEventListener('click',()=>{document.querySelectorAll('tr.details-row.failed').forEach(d=>{d.hidden=false;const b=d.previousElementSibling.querySelector('button.toggle');if(b)b.classList.add('open');});});
document.querySelectorAll('button.tab').forEach(b=>b.addEventListener('click',()=>{const panel=document.getElementById(b.dataset.tab);const box=b.closest('td');box.querySelectorAll('.panel').forEach(p=>p.hidden=true);box.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));panel.hidden=false;b.classList.add('active');}));
</script></body></html>
""")


def artifact_badges(record: TestRecord) -> str:
    labels = []
    for kind, label in (("steps", "steps"), ("trace", "trace"), ("applog", "app log"), ("image", "screenshot"), ("video", "video")):
        if record.first(kind) is not None:
            labels.append(f"<small>{label}</small>")
    return "".join(labels)


def render_html(records: list[TestRecord], title: str, run_url: str, build: str) -> str:
    counts = Counter(record.outcome for record in records)
    rows = []
    for index, record in enumerate(records):
        css, color, background = STATUS_STYLE.get(record.outcome, STATUS_STYLE["SKIPPED"])
        detail_html = render_tabs(record, index, build)
        case_id = e(record.test_case_id)
        error_line = first_error_line(record, 160)
        badge = record.outcome + (f" · {known_issue_link(record)}" if record.known_issue else "")
        rows.append(
            f"<tr class='row {css}' data-status='{css}' data-suite='{e(record.suite)}' data-error='{e(record.error_message[:2000])}'>"
            f"<td><span class='badge' style='color:{color};background:{background}'>{badge}</span></td>"
            f"<td>{e(record.suite)}<br><small>{e(record.shard)}</small></td>"
            f"<td><strong>{e(record.short_class)}</strong><br>{e(record.display_name)}"
            + (f"<br><small>{case_id}</small>" if case_id else "")
            + (f"<br><small class='err'>{e(error_line)}</small>" if error_line else "")
            + f"</td><td>{format_duration(record.duration_ms)}</td>"
            f"<td class='artifacts'>{artifact_badges(record)}</td>"
            f"<td><button class='toggle' data-target='d{index}'>details</button></td></tr>"
            f"<tr class='details-row {css}' id='d{index}' hidden><td colspan='6'>{detail_html}</td></tr>"
        )
    suites = sorted({record.suite for record in records})
    return PAGE_TEMPLATE.substitute(
        title=e(title),
        build=e(build),
        total=len(records),
        total_duration=format_duration(sum(record.duration_ms for record in records)),
        run_url=e(run_url),
        passed=counts.get("PASSED", 0),
        failed=counts.get("FAILED", 0),
        skipped=counts.get("SKIPPED", 0),
        known=counts.get(KNOWN_ISSUE, 0),
        fixed=counts.get(FIXED_CANDIDATE, 0),
        shards=len({record.shard for record in records}),
        suite_options="".join(f"<option value='{e(s)}'>{e(s)}</option>" for s in suites),
        rows="".join(rows),
    )


def debug_bundle(record: TestRecord, build: str, run_url: str) -> dict:
    steps = record.first("steps")
    applog = record.first("applog")
    trace = record.first("trace")
    bundle = {
        "test": {
            "class": record.class_name,
            "method": record.method,
            "displayName": record.display_name,
            "testCaseId": record.test_case_id,
            "description": record.description,
            "suite": record.suite,
            "shard": record.shard,
            "sourceUrl": record.source_url(),
        },
        "run": {
            "applicationBuild": build,
            "workflowRunUrl": run_url,
            "testsRepository": record.repository,
            "testsBranch": record.git_branch,
            "testsCommit": record.git_sha,
        },
        "result": {
            "status": record.status,
            "outcome": record.outcome,
            "knownIssue": record.known_issue,
            "knownIssueUrl": record.known_issue_url,
            "startedAt": record.started_at,
            "finishedAt": record.finished_at,
            "durationMs": record.duration_ms,
            "errorType": record.error_type,
            "errorMessage": record.error_message,
            "stackTrace": record.stack_trace,
        },
        "steps": {"path": steps.href if steps else "", "lines": read_text(steps.path).splitlines() if steps else []},
        "applicationLog": {
            "path": applog.href if applog else "",
            "totalLines": 0,
            "warningsAndErrors": [],
        },
        "trace": {"path": trace.href if trace else "", "howToOpen": TRACE_HINT if trace else ""},
        "attachments": [{"message": a.message, "kind": a.kind, "path": a.href} for a in record.attachments],
    }
    if applog is not None:
        extract, total = app_log_extract(read_text(applog.path), APP_LOG_EXTRACT_LINES * 5)
        bundle["applicationLog"]["totalLines"] = total
        bundle["applicationLog"]["warningsAndErrors"] = extract
    return bundle


DEBUG_README = """# Debugging a failed test with an AI assistant

Every test that did not pass has one JSON file in this directory, named `<TestClass>.<testMethod>.json`,
and `index.json` lists all tests of the run. A bundle is self-contained: hand it to the assistant together
with the files it points to (paths are relative to the report root).

What a bundle holds:

- `test` — class, method, `@TestCaseId`, description, suite, shard and a link to the test source at the exact commit.
- `run` — application build, workflow run URL, tests repository, branch and commit.
- `result` — status, timestamps, duration, exception type, message and full stack trace.
- `steps.lines` — the complete framework log of this test only: container start, every page action, wait,
  REST call and assertion, with millisecond timestamps. Read it top to bottom to see what the test did
  before it failed; the last lines show the failing step.
- `applicationLog` — every WARN, ERROR and exception (with the following stack lines) from the
  OpenL Studio or Rule Services container that served this test, plus the path to the full log.
- `trace.path` — the Playwright trace: DOM snapshots before and after every action (the page at every step,
  including the failure), network and console. Open it with `npx playwright show-trace trace.zip` or drop it
  onto https://trace.playwright.dev.
- `attachments` — failure screenshot and downloaded files; the failure video is not copied into the report,
  it stays in the rp-export artifact and in ReportPortal.

Suggested prompt:

    Here is the debug bundle of a failed UI test (JSON) and its Playwright trace. Determine whether the
    failure is a product defect, a test defect or an infrastructure problem. Use `steps.lines` for the
    test's actions, `result.stackTrace` for the failing assertion, `applicationLog.warningsAndErrors`
    for server-side errors around the failure time, and the trace for the page state. Quote the exact
    log lines you base the verdict on.
"""


def write_debug_bundles(records: list[TestRecord], output_dir: Path, build: str, run_url: str) -> None:
    debug_dir = output_dir / "debug"
    debug_dir.mkdir(parents=True, exist_ok=True)
    index = []
    used_slugs: dict[str, int] = {}
    for record in records:
        entry = {
            "class": record.class_name,
            "method": record.method,
            "displayName": record.display_name,
            "suite": record.suite,
            "shard": record.shard,
            "status": record.status,
            "durationMs": record.duration_ms,
            "testCaseId": record.test_case_id,
            "errorMessage": record.error_message,
            "outcome": record.outcome,
            "knownIssue": record.known_issue,
            "bundle": "",
        }
        if record.status != "PASSED":
            slug = record.slug
            used_slugs[slug] = used_slugs.get(slug, 0) + 1
            if used_slugs[slug] > 1:
                slug = f"{slug}-{used_slugs[slug]}"
            bundle_path = debug_dir / f"{slug}.json"
            bundle_path.write_text(json.dumps(debug_bundle(record, build, run_url), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            entry["bundle"] = bundle_path.relative_to(output_dir).as_posix()
        index.append(entry)
    (debug_dir / "index.json").write_text(
        json.dumps({"build": build, "workflowRunUrl": run_url, "tests": index}, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    (debug_dir / "README.md").write_text(DEBUG_README, encoding="utf-8")


def write_step_summary(records: list[TestRecord], title: str, build: str, summary_path: str | None) -> str:
    counts = Counter(record.outcome for record in records)
    lines = [
        f"## {title}",
        "",
        f"Build `{build}` · {len(records)} tests · ✅ {counts.get('PASSED', 0)} passed · ❌ {counts.get('FAILED', 0)} failed · ⏭️ {counts.get('SKIPPED', 0)} skipped"
        f" · 🟠 {counts.get(KNOWN_ISSUE, 0)} known issues · 🔵 {counts.get(FIXED_CANDIDATE, 0)} passed despite a known issue",
        "",
    ]
    blocking = [record for record in records if record.outcome in {"FAILED", "SKIPPED"}]
    if blocking:
        lines += ["| Status | Group | Test | Error |", "|---|---|---|---|"]
        for record in blocking:
            first_line = first_error_line(record, 200).replace("|", "\\|")
            lines.append(f"| {record.status} | {record.suite} | `{record.short_class}.{record.method}` | {first_line} |")
    else:
        lines.append("No failures without a known issue.")
    known = [record for record in records if record.known_issue]
    if known:
        lines += ["", "| Known issue | Outcome | Test |", "|---|---|---|"]
        for record in known:
            ticket = f"[{record.known_issue}]({record.known_issue_url})" if record.known_issue_url else record.known_issue
            lines.append(f"| {ticket} | {record.outcome} | `{record.short_class}.{record.method}` |")
    lines += [
        "",
        "The `test-report-merged` artifact holds `index.html` with per-test step logs, Playwright traces, application logs "
        "and failure screenshots, and `debug/<TestClass>.<method>.json` bundles for every failed test (see `debug/README.md`); "
        "failure videos stay in the rp-export and in ReportPortal.",
    ]
    text = "\n".join(lines) + "\n"
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as handle:
            handle.write(text)
    return text


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Merge the shard rp-exports into one HTML report, per-test debug bundles and a job summary."
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
    rp_records, _ = collect_from_rp_export(args.input_root, args.output_dir)
    records = sort_records(rp_records)
    if not records:
        message = f"No test results found under {args.input_root}"
        if args.step_summary:
            with open(args.step_summary, "a", encoding="utf-8") as handle:
                handle.write(f"## {args.title}\n\n{message}\n")
        raise SystemExit(message)

    write_debug_bundles(records, args.output_dir, args.build, args.run_url)
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
                        "outcome": r.outcome,
                        "knownIssue": r.known_issue,
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
