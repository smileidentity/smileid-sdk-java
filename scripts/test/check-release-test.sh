#!/usr/bin/env bash
# Tests scripts/check-release.sh in a throwaway git repository.
# Usage: scripts/test/check-release-test.sh
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
script="${repo_root}/scripts/check-release.sh"

failures=0
pass() { echo "ok - $1"; }
fail() {
  echo "not ok - $1"
  failures=$((failures + 1))
}

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
git_() { git -C "$work" -c user.name=test -c user.email=test@example.com "$@"; }

# write_release <version> <changelog heading>
write_release() {
  mkdir -p "${work}/src/main/java/com/smileidentity"
  printf 'group = "com.smileidentity"\nversion = "%s"\n' "$1" >"${work}/build.gradle.kts"
  printf 'public final class Version {\n  public static final String VERSION = "%s";\n}\n' "$1" \
    >"${work}/src/main/java/com/smileidentity/Version.java"
  printf '# Changelog\n\n## [Unreleased]\n\n%s\n\n* A change.\n' "$2" >"${work}/CHANGELOG.md"
  git_ add -A
  git_ commit -q -m "Release $1"
}

# expect_status <description> <status> <script args...>
expect_status() {
  local description="$1" expected="$2" status
  shift 2
  set +e
  (cd "$work" && "$script" "$@") >/dev/null 2>&1
  status=$?
  set -e
  if [ "$status" -eq "$expected" ]; then
    pass "$description"
  else
    fail "$description (status $status, expected $expected)"
  fi
}

git_ init -q -b main
write_release 1.0.0 "## [1.0.0] - 2026-01-01"
git_ tag v1.0.0
git_ commit -q --allow-empty -m "Breaking change"
breaking_commit="$(git_ rev-parse HEAD)"

prs="${work}/.git/prs.tsv"
: >"$prs"
export BREAKING_PRS_FILE="$prs"

write_release 1.1.0 "## [1.1.0] - 2026-02-01"
expect_status "a minor release with matching files passes before tagging" 0 v1.1.0 HEAD
expect_status "a tag that is not vX.Y.Z fails" 1 v1.1 HEAD
expect_status "a tag that does not match build.gradle.kts fails" 1 v1.2.0 HEAD
expect_status "no arguments is a usage error" 2

printf '1\t%s\tBreaking change\n' "$breaking_commit" >"$prs"
expect_status "a minor release with a breaking pull request fails" 1 v1.1.0 HEAD
git_ reset -q --hard v1.0.0
git_ commit -q --allow-empty -m "Breaking change"
breaking_commit="$(git_ rev-parse HEAD)"
printf '1\t%s\tBreaking change\n' "$breaking_commit" >"$prs"
write_release 2.0.0 "## [2.0.0] - 2026-03-01"
expect_status "a major release with a breaking pull request passes" 0 v2.0.0 HEAD
: >"$prs"
expect_status "a major release without a breaking pull request fails" 1 v2.0.0 HEAD

printf '1\t%s\tBreaking change\n' "$breaking_commit" >"$prs"
write_release 2.0.0 "## [2.0.0]"
expect_status "a changelog heading without a date fails" 1 v2.0.0 HEAD
write_release 2.0.0 "## [2.0.0] - 2026-03-01"
sed 's/2\.0\.0/2.0.1/' "${work}/src/main/java/com/smileidentity/Version.java" >"${work}/v.tmp"
mv "${work}/v.tmp" "${work}/src/main/java/com/smileidentity/Version.java"
git_ commit -q -am "Wrong Version.java"
expect_status "a Version.java that does not match fails" 1 v2.0.0 HEAD
write_release 2.0.0 "## [2.0.0] - 2026-03-01"
git_ tag v2.0.0
expect_status "on the tag itself, the release passes" 0 v2.0.0
expect_status "before tagging, a tag that already exists fails" 1 v2.0.0 HEAD

if [ "$failures" -gt 0 ]; then
  echo "${failures} test(s) failed"
  exit 1
fi
echo "All check-release tests passed"
