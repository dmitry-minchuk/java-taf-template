#!/usr/bin/env python3
import argparse
import email.utils
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from testngsuite import write_suite  # noqa: E402

API_ROOT = "https://api.github.com"
QUEUE_REF_PREFIX = "openl-queue"
TRANSIENT_RETRIES = 8
RATE_LIMIT_MAX_WAIT_SECONDS = 900


class ApiResponse:
    def __init__(self, status: int, body: object, headers: dict[str, str]):
        self.status = status
        self.body = body
        self.headers = headers


class GitHubApi:
    def __init__(self, repo: str, token: str):
        self.repo = repo
        self.token = token

    def request(self, method: str, path: str, payload: dict | None = None) -> ApiResponse:
        data = json.dumps(payload).encode("utf-8") if payload is not None else None
        request = urllib.request.Request(f"{API_ROOT}/repos/{self.repo}/{path}", data=data, method=method)
        request.add_header("Authorization", f"Bearer {self.token}")
        request.add_header("Accept", "application/vnd.github+json")
        request.add_header("X-GitHub-Api-Version", "2022-11-28")
        if data is not None:
            request.add_header("Content-Type", "application/json")
        for attempt in range(1, TRANSIENT_RETRIES + 1):
            try:
                with urllib.request.urlopen(request, timeout=60) as response:
                    body = response.read().decode("utf-8")
                    return ApiResponse(response.status, json.loads(body) if body else None, dict(response.headers))
            except urllib.error.HTTPError as error:
                body = error.read().decode("utf-8", errors="replace")
                try:
                    parsed = json.loads(body)
                except json.JSONDecodeError:
                    parsed = body
                response = ApiResponse(error.code, parsed, dict(error.headers))
                if error.code in (403, 429) and is_rate_limited(response):
                    wait = rate_limit_wait_seconds(response)
                    print(f"GitHub API rate limit hit on {method} {path}; waiting {wait} s", file=sys.stderr, flush=True)
                    time.sleep(wait)
                    continue
                if error.code >= 500:
                    print(f"GitHub API {error.code} on {method} {path} (attempt {attempt}/{TRANSIENT_RETRIES})", file=sys.stderr, flush=True)
                    time.sleep(min(60, 2 ** attempt))
                    continue
                return response
            except urllib.error.URLError as error:
                print(f"GitHub API connection error on {method} {path} (attempt {attempt}/{TRANSIENT_RETRIES}): {error.reason}", file=sys.stderr, flush=True)
                time.sleep(min(60, 2 ** attempt))
        raise SystemExit(f"GitHub API {method} {path} kept failing after {TRANSIENT_RETRIES} attempts")


def is_rate_limited(response: ApiResponse) -> bool:
    if response.headers.get("Retry-After"):
        return True
    if response.headers.get("X-RateLimit-Remaining") == "0":
        return True
    message = json.dumps(response.body).lower()
    return "rate limit" in message or "abuse" in message


def rate_limit_wait_seconds(response: ApiResponse) -> int:
    retry_after = response.headers.get("Retry-After")
    if retry_after:
        try:
            return min(RATE_LIMIT_MAX_WAIT_SECONDS, max(1, int(retry_after)))
        except ValueError:
            parsed = email.utils.parsedate_to_datetime(retry_after)
            return min(RATE_LIMIT_MAX_WAIT_SECONDS, max(1, int(parsed.timestamp() - time.time())))
    reset = response.headers.get("X-RateLimit-Reset")
    if reset:
        return min(RATE_LIMIT_MAX_WAIT_SECONDS, max(1, int(reset) - int(time.time()) + 1))
    return 60


def queue_ref(run_key: str, class_name: str) -> str:
    return f"{QUEUE_REF_PREFIX}/{run_key}/{class_name}"


def claimed_classes(api: GitHubApi, run_key: str) -> set[str]:
    prefix = f"refs/{QUEUE_REF_PREFIX}/{run_key}/"
    claimed: set[str] = set()
    page = 1
    while True:
        response = api.request("GET", f"git/matching-refs/{QUEUE_REF_PREFIX}/{run_key}/?per_page=100&page={page}")
        if response.status != 200 or not isinstance(response.body, list):
            raise SystemExit(f"Could not list queue refs: HTTP {response.status} {response.body}")
        for ref in response.body:
            claimed.add(str(ref.get("ref", "")).removeprefix(prefix))
        if len(response.body) < 100:
            return claimed
        page += 1


def claim(api: GitHubApi, run_key: str, sha: str, class_name: str) -> bool:
    response = api.request("POST", "git/refs", {"ref": f"refs/{queue_ref(run_key, class_name)}", "sha": sha})
    if response.status == 201:
        return True
    if response.status == 422:
        return False
    raise SystemExit(f"Claim of {class_name} failed with HTTP {response.status}: {response.body}")


def claim_next(api: GitHubApi, run_key: str, sha: str, classes: list[str], skipped: set[str]) -> str | None:
    skipped.update(claimed_classes(api, run_key))
    for class_name in classes:
        if class_name in skipped:
            continue
        if claim(api, run_key, sha, class_name):
            return class_name
        skipped.add(class_name)
    return None


def run_class(class_name: str, suite_dir: Path, maven_args: list[str]) -> dict:
    suite = write_suite([class_name], suite_dir / f"{class_name}.xml", f"OpenL GHA {class_name.rsplit('.', 1)[-1]}")
    command = ["mvn", "--batch-mode", "surefire:test", f"-DsuiteXmlFile={suite}", *maven_args]
    print(f"::group::{class_name}")
    print(" ".join(command), flush=True)
    started = time.time()
    exit_code = subprocess.call(command)
    duration = round(time.time() - started, 1)
    print("::endgroup::")
    print(f"{class_name}: Maven exit code {exit_code} after {duration} s", flush=True)
    return {"class": class_name, "exitCode": exit_code, "durationSeconds": duration}


def run(args: argparse.Namespace, maven_args: list[str]) -> None:
    api = GitHubApi(args.repo, required_token())
    classes = json.loads(Path(args.classes_file).read_text(encoding="utf-8")) if args.classes_file else json.loads(args.classes_json)
    report_dir = Path(args.report_dir)
    report_dir.mkdir(parents=True, exist_ok=True)
    report_file = report_dir / f"{args.shard}.json"
    report_file.write_text("[]\n", encoding="utf-8")
    results: list[dict] = []
    skipped: set[str] = set()
    while True:
        class_name = claim_next(api, args.run_key, args.sha, classes, skipped)
        if class_name is None:
            break
        results.append(run_class(class_name, Path(args.suite_dir), maven_args))
        report_file.write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    total = sum(item["durationSeconds"] for item in results)
    print(f"{args.shard}: ran {len(results)} class(es) in {round(total / 60, 1)} min; queue exhausted")


def cleanup(args: argparse.Namespace) -> None:
    api = GitHubApi(args.repo, required_token())
    refs = claimed_classes(api, args.run_key)
    deleted = 0
    for class_name in sorted(refs):
        response = api.request("DELETE", f"git/refs/{queue_ref(args.run_key, class_name)}")
        if response.status in (204, 422):
            deleted += 1
        else:
            print(f"Could not delete the queue ref of {class_name}: HTTP {response.status} {response.body}", file=sys.stderr)
    print(f"Deleted {deleted} of {len(refs)} queue ref(s) for run {args.run_key}")


def required_token() -> str:
    token = os.environ.get("GITHUB_TOKEN", "")
    if not token:
        raise SystemExit("GITHUB_TOKEN is required")
    return token


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Work queue for GitHub Actions shards: every shard claims the next unclaimed test class by creating the "
                    "git ref refs/openl-queue/<run key>/<class> (creation is atomic, a second shard gets HTTP 422), runs the "
                    "class with Maven surefire:test and continues until no class is left. Maven arguments after -- are passed through."
    )
    subparsers = parser.add_subparsers(dest="command", required=True)
    runner = subparsers.add_parser("run")
    runner.add_argument("--repo", required=True)
    runner.add_argument("--run-key", required=True, help="Unique key of this run attempt, e.g. <run id>-<run attempt>.")
    runner.add_argument("--sha", required=True)
    runner.add_argument("--shard", required=True)
    runner.add_argument("--classes-json", default="")
    runner.add_argument("--classes-file", default="")
    runner.add_argument("--suite-dir", default="generated-testng")
    runner.add_argument("--report-dir", default="target/queue")
    cleaner = subparsers.add_parser("cleanup")
    cleaner.add_argument("--repo", required=True)
    cleaner.add_argument("--run-key", required=True)

    argv = sys.argv[1:]
    maven_args: list[str] = []
    if "--" in argv:
        split = argv.index("--")
        argv, maven_args = argv[:split], argv[split + 1:]
    args = parser.parse_args(argv)
    if args.command == "run":
        if not args.classes_json and not args.classes_file:
            raise SystemExit("--classes-json or --classes-file is required")
        run(args, maven_args)
    else:
        cleanup(args)


if __name__ == "__main__":
    main()
