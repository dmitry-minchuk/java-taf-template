#!/usr/bin/env python3
import argparse
import base64
import html
import json
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from string import Template

PAGES_BRANCH = "gh-pages"
RUNS_DIR = "runs"
PUSH_ATTEMPTS = 5

INDEX_TEMPLATE = Template("""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>OpenL Tests reports</title>
<style>
:root{--bg:#f3f5f9;--surface:#fff;--line:#e3e8ef;--text:#0f172a;--muted:#64748b;--accent:#2563eb;--shadow:0 1px 2px rgba(15,23,42,.06),0 1px 3px rgba(15,23,42,.04)}
body{margin:0;font-family:Inter,ui-sans-serif,-apple-system,"Segoe UI",Roboto,Helvetica,Arial,sans-serif;font-size:14px;line-height:1.5;color:var(--text);background:var(--bg)}
.page{max-width:1200px;margin:0 auto;padding:32px 28px 64px} h1{margin:0 0 6px;font-size:26px;letter-spacing:-.02em} .meta{color:var(--muted);margin-bottom:20px}
a{color:var(--accent);text-decoration:none} a:hover{text-decoration:underline}
.table-wrap{background:var(--surface);border:1px solid var(--line);border-radius:14px;box-shadow:var(--shadow);overflow-x:auto}
table{width:100%;border-collapse:separate;border-spacing:0} th{padding:12px 14px;text-align:left;font-size:11px;font-weight:600;letter-spacing:.06em;text-transform:uppercase;color:var(--muted);background:#f8fafc;border-bottom:1px solid var(--line)}
td{padding:12px 14px;border-bottom:1px solid var(--line);vertical-align:top} tr:hover td{background:#f8fafc}
.pill{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;font-weight:600;margin-right:6px}
.passed{color:#15803d;background:#dcfce7} .failed{color:#dc2626;background:#fee2e2} .known{color:#c2410c;background:#ffedd5} .fixed{color:#1d4ed8;background:#dbeafe} .skipped{color:#a16207;background:#fef9c3}
small{color:var(--muted)} code{padding:1px 6px;border-radius:6px;background:#eef2f6;font-size:12px}
</style></head><body><div class="page">
<h1>OpenL Tests reports</h1>
<div class="meta">The last $count GitHub Actions runs, newest first. Every report is self-contained: step logs, Playwright traces, application logs and screenshots.</div>
<div class="table-wrap"><table><thead><tr><th>Run</th><th>Build</th><th>Results</th><th>Tests</th><th></th></tr></thead><tbody>$rows</tbody></table></div>
</div></body></html>
""")


def git(token: str, *args: str, cwd: Path | None = None) -> subprocess.CompletedProcess:
    basic = base64.b64encode(f"x-access-token:{token}".encode("utf-8")).decode("ascii")
    return subprocess.run(
        ["git", "-c", f"http.extraheader=AUTHORIZATION: basic {basic}", *args],
        cwd=cwd, capture_output=True, text=True,
    )


def require(completed: subprocess.CompletedProcess, action: str) -> None:
    if completed.returncode != 0:
        raise SystemExit(f"{action} failed: {completed.stderr.strip() or completed.stdout.strip()}")


def read_summary(run_dir: Path) -> dict:
    summary_file = run_dir / "summary.json"
    if not summary_file.exists():
        return {}
    try:
        return json.loads(summary_file.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}


def pill(css: str, label: str, count: int) -> str:
    return f"<span class='pill {css}'>{count} {label}</span>" if count else ""


def render_index(runs_root: Path, repository: str) -> str:
    rows = []
    run_dirs = sorted((d for d in runs_root.iterdir() if d.is_dir()), key=lambda d: int(d.name) if d.name.isdigit() else 0, reverse=True)
    for run_dir in run_dirs:
        summary = read_summary(run_dir)
        run = summary.get("run", {})
        counts = summary.get("counts", {})
        outcomes = {}
        for test in summary.get("tests", []):
            outcomes[test.get("outcome") or test.get("status")] = outcomes.get(test.get("outcome") or test.get("status"), 0) + 1
        results = "".join([
            pill("passed", "passed", outcomes.get("PASSED", counts.get("PASSED", 0))),
            pill("failed", "failed", outcomes.get("FAILED", 0)),
            pill("skipped", "skipped", outcomes.get("SKIPPED", 0)),
            pill("known", "known issues", outcomes.get("KNOWN ISSUE", 0)),
            pill("fixed", "fixed?", outcomes.get("FIXED?", 0)),
        ]) or "<small>no summary</small>"
        applications = "<br>".join(
            html.escape(", ".join(" ".join(p for p in (v.get("title", ""), v.get("version", "")) if p) + (f" build {v['buildNumber']}" if v.get("buildNumber") else "") for v in a.get("versions", [])) or a.get("image", ""))
            for a in run.get("applications", []) if a.get("versions")
        ) or html.escape(str(summary.get("build", "")))
        tests = f"<code>{html.escape(str(run.get('testsBranch', '')))}</code> @ <code>{html.escape(str(run.get('testsCommit', ''))[:12])}</code>" if run.get("testsCommit") else "—"
        selective = " <small>selective</small>" if run.get("selective") else ""
        workflow = f" · <a href='https://github.com/{html.escape(repository)}/actions/runs/{html.escape(run_dir.name)}'>workflow</a>" if repository else ""
        rows.append(
            f"<tr><td><a href='{RUNS_DIR}/{html.escape(run_dir.name)}/'>{html.escape(str(run.get('startedAt') or run_dir.name))}</a>{selective}<br><small>run {html.escape(run_dir.name)}</small></td>"
            f"<td>{applications}</td><td>{results}<br><small>{summary.get('total', '')} tests</small></td><td>{tests}</td>"
            f"<td><a href='{RUNS_DIR}/{html.escape(run_dir.name)}/'>open report</a>{workflow}</td></tr>"
        )
    return INDEX_TEMPLATE.substitute(count=len(rows), rows="".join(rows) or "<tr><td colspan='5'>No reports published yet.</td></tr>")


def prune(runs_root: Path, keep: int) -> list[str]:
    run_dirs = sorted((d for d in runs_root.iterdir() if d.is_dir()), key=lambda d: int(d.name) if d.name.isdigit() else 0, reverse=True)
    removed = []
    for stale in run_dirs[keep:]:
        shutil.rmtree(stale)
        removed.append(stale.name)
    return removed


def publish(args: argparse.Namespace) -> str:
    token = os.environ.get("GITHUB_TOKEN", "")
    if not token:
        raise SystemExit("GITHUB_TOKEN is required")
    remote = f"https://github.com/{args.repo}.git"
    report = Path(args.report_dir)
    if not (report / "index.html").exists():
        raise SystemExit(f"No index.html under {report}")

    for attempt in range(1, PUSH_ATTEMPTS + 1):
        workdir = Path(tempfile.mkdtemp(prefix="gh-pages-"))
        require(git(token, "clone", "--quiet", "--depth", "1", "--branch", PAGES_BRANCH, remote, str(workdir)), "Cloning the pages branch")
        runs_root = workdir / RUNS_DIR
        runs_root.mkdir(exist_ok=True)
        target = runs_root / args.run_id
        if target.exists():
            shutil.rmtree(target)
        shutil.copytree(report, target)
        removed = prune(runs_root, args.keep)
        (workdir / "index.html").write_text(render_index(runs_root, args.repo), encoding="utf-8")
        (workdir / ".nojekyll").touch()
        require(git(token, "add", "-A", cwd=workdir), "Staging the report")
        message = f"Publish the test report of run {args.run_id}" + (f", prune {len(removed)} old run(s)" if removed else "")
        require(git(token, "-c", "user.name=github-actions[bot]", "-c", "user.email=41898282+github-actions[bot]@users.noreply.github.com",
                    "commit", "--quiet", "-m", message, cwd=workdir), "Committing the report")
        pushed = git(token, "push", "--quiet", remote, f"HEAD:{PAGES_BRANCH}", cwd=workdir)
        shutil.rmtree(workdir, ignore_errors=True)
        if pushed.returncode == 0:
            return f"{args.pages_url.rstrip('/')}/{RUNS_DIR}/{args.run_id}/"
        print(f"Push to {PAGES_BRANCH} rejected (attempt {attempt}/{PUSH_ATTEMPTS}): {pushed.stderr.strip()}", file=sys.stderr, flush=True)
    raise SystemExit(f"Could not publish the report after {PUSH_ATTEMPTS} attempts")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Publish the merged test report to the gh-pages branch under runs/<run id>/, regenerate the index of the "
                    "last runs and prune older ones, so the report can be opened in a browser without downloading an artifact."
    )
    parser.add_argument("--repo", required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--report-dir", required=True)
    parser.add_argument("--pages-url", required=True, help="Base URL of the GitHub Pages site, e.g. https://owner.github.io/repo")
    parser.add_argument("--keep", type=int, default=30, help="How many latest runs to keep on the site.")
    parser.add_argument("--step-summary", default=os.environ.get("GITHUB_STEP_SUMMARY"))
    args = parser.parse_args()

    url = publish(args)
    print(f"Report published: {url}")
    if args.step_summary:
        with open(args.step_summary, "a", encoding="utf-8") as handle:
            handle.write(f"\n**Open the report in the browser:** [{url}]({url}) (all runs: {args.pages_url})\n")


if __name__ == "__main__":
    main()
