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
from collections import Counter
from pathlib import Path
from string import Template

PAGES_BRANCH = "gh-pages"
RUNS_DIR = "runs"
PUSH_ATTEMPTS = 5
KNOWN_ISSUE = "KNOWN ISSUE"
FIXED_CANDIDATE = "FIXED?"

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


def require(completed: subprocess.CompletedProcess, action: str) -> subprocess.CompletedProcess:
    if completed.returncode != 0:
        raise SystemExit(f"{action} failed: {completed.stderr.strip() or completed.stdout.strip()}")
    return completed


def e(value: object) -> str:
    return html.escape(str(value if value is not None else ""))


def run_dirs(runs_root: Path) -> list[Path]:
    return sorted((d for d in runs_root.iterdir() if d.is_dir() and d.name.isdigit()), key=lambda d: int(d.name), reverse=True)


def read_summary(run_dir: Path) -> dict:
    summary_file = run_dir / "summary.json"
    if not summary_file.exists():
        return {}
    try:
        data = json.loads(summary_file.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}
    return data if isinstance(data, dict) else {}


def pill(css: str, label: str, count: int) -> str:
    return f"<span class='pill {css}'>{count} {label}</span>" if count else ""


def describe_application(application: dict) -> str:
    versions = application.get("versions") or []
    if not versions:
        return e(application.get("image", ""))
    described = []
    for version in versions:
        parts = [" ".join(p for p in (version.get("title") or "", version.get("version") or "") if p)]
        if version.get("buildNumber"):
            parts.append(f"build {version['buildNumber']}")
        described.append(", ".join(p for p in parts if p))
    return e("; ".join(described))


def render_index(runs_root: Path, repository: str) -> str:
    rows = []
    for run_dir in run_dirs(runs_root):
        summary = read_summary(run_dir)
        run = summary.get("run") or {}
        tests = summary.get("tests") or []
        outcomes = Counter(str(t.get("outcome") or t.get("status") or "") for t in tests if isinstance(t, dict))
        results = "".join([
            pill("passed", "passed", outcomes.get("PASSED", 0)),
            pill("failed", "failed", outcomes.get("FAILED", 0)),
            pill("skipped", "skipped", outcomes.get("SKIPPED", 0)),
            pill("known", "known issues", outcomes.get(KNOWN_ISSUE, 0)),
            pill("fixed", "fixed?", outcomes.get(FIXED_CANDIDATE, 0)),
        ]) or "<small>no summary</small>"
        applications = "<br>".join(describe_application(a) for a in (run.get("applications") or []) if isinstance(a, dict)) or e(summary.get("build", ""))
        tests_revision = f"<code>{e(run.get('testsBranch'))}</code> @ <code>{e(str(run.get('testsCommit'))[:12])}</code>" if run.get("testsCommit") else "—"
        selective = " <small>selective</small>" if run.get("selective") else ""
        workflow = f" · <a href='https://github.com/{e(repository)}/actions/runs/{e(run_dir.name)}'>workflow</a>" if repository else ""
        rows.append(
            f"<tr><td><a href='{RUNS_DIR}/{e(run_dir.name)}/'>{e(run.get('startedAt') or run_dir.name)}</a>{selective}<br><small>run {e(run_dir.name)}</small></td>"
            f"<td>{applications}</td><td>{results}<br><small>{e(summary.get('total', ''))} tests</small></td><td>{tests_revision}</td>"
            f"<td><a href='{RUNS_DIR}/{e(run_dir.name)}/'>open report</a>{workflow}</td></tr>"
        )
    return INDEX_TEMPLATE.substitute(count=len(rows), rows="".join(rows) or "<tr><td colspan='5'>No reports published yet.</td></tr>")


def prune(runs_root: Path, keep: int, current: str) -> list[str]:
    removed = []
    for stale in [d for d in run_dirs(runs_root) if d.name != current][max(keep - 1, 0):]:
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
    url = f"{args.pages_url.rstrip('/')}/{RUNS_DIR}/{args.run_id}/"

    for attempt in range(1, PUSH_ATTEMPTS + 1):
        workdir = Path(tempfile.mkdtemp(prefix="gh-pages-"))
        try:
            require(git(token, "clone", "--quiet", "--depth", "1", "--branch", PAGES_BRANCH, remote, str(workdir)), "Cloning the pages branch")
            lease = require(git(token, "rev-parse", "HEAD", cwd=workdir), "Reading the pages branch tip").stdout.strip()
            runs_root = workdir / RUNS_DIR
            runs_root.mkdir(exist_ok=True)
            target = runs_root / args.run_id
            if target.exists():
                shutil.rmtree(target)
            shutil.copytree(report, target)
            removed = prune(runs_root, args.keep, args.run_id)
            (workdir / "index.html").write_text(render_index(runs_root, args.repo), encoding="utf-8")
            (workdir / ".nojekyll").touch()
            require(git(token, "checkout", "--quiet", "--orphan", "publish", cwd=workdir), "Starting the single publish commit")
            require(git(token, "add", "-A", cwd=workdir), "Staging the site")
            unchanged = git(token, "diff", "--cached", "--quiet", lease, cwd=workdir)
            if unchanged.returncode == 0:
                print(f"The site already contains this report; nothing to publish for run {args.run_id}")
                return url
            if unchanged.returncode > 1:
                require(unchanged, "Comparing the site with the published one")
            message = f"Publish the test report of run {args.run_id}" + (f", prune {len(removed)} old run(s)" if removed else "")
            require(git(token, "-c", "user.name=github-actions[bot]", "-c", "user.email=41898282+github-actions[bot]@users.noreply.github.com",
                        "commit", "--quiet", "-m", message, cwd=workdir), "Committing the site")
            pushed = git(token, "push", "--quiet", f"--force-with-lease=refs/heads/{PAGES_BRANCH}:{lease}", remote, f"HEAD:refs/heads/{PAGES_BRANCH}", cwd=workdir)
            if pushed.returncode == 0:
                return url
            print(f"Push to {PAGES_BRANCH} rejected (attempt {attempt}/{PUSH_ATTEMPTS}): {pushed.stderr.strip()}", file=sys.stderr, flush=True)
        finally:
            shutil.rmtree(workdir, ignore_errors=True)
    raise SystemExit(f"Could not publish the report after {PUSH_ATTEMPTS} attempts")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Publish the merged test report to the gh-pages branch under runs/<run id>/, regenerate the index of the last runs "
                    "and prune older ones. The branch always holds a single commit (orphan commit pushed with --force-with-lease), so "
                    "pruned reports do not stay in the git history and the site can be opened in a browser without downloading an artifact."
    )
    parser.add_argument("--repo", required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--report-dir", required=True)
    parser.add_argument("--pages-url", required=True, help="Base URL of the GitHub Pages site, e.g. https://owner.github.io/repo")
    parser.add_argument("--keep", type=int, default=15, help="How many latest runs to keep on the site.")
    parser.add_argument("--step-summary", default=os.environ.get("GITHUB_STEP_SUMMARY"))
    args = parser.parse_args()

    url = publish(args)
    print(f"Report published: {url}")
    if args.step_summary:
        with open(args.step_summary, "a", encoding="utf-8") as handle:
            handle.write(f"\n**Open the report in the browser:** [{url}]({url}) (all runs: {args.pages_url})\n")


if __name__ == "__main__":
    main()
