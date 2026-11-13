#!/usr/bin/env bash
# Tests scripts/release-tags.sh in a throwaway git repository.
# Usage: scripts/test/release-tags-test.sh
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
script="${repo_root}/scripts/release-tags.sh"

failures=0
pass() { echo "ok - $1"; }
fail() {
  echo "not ok - $1"
  failures=$((failures + 1))
}

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
git_() { git -C "$work" -c user.name=test -c user.email=test@example.com "$@"; }
commit() {
  git_ commit -q --allow-empty -m "$1"
  git_ rev-parse HEAD
}
run() { (cd "$work" && "$script" "$@"); }

# expect <description> <expected output> <command args...>
expect() {
  local description="$1" expected="$2" out
  shift 2
  if out="$(run "$@" 2>&1)" && [ "$out" = "$expected" ]; then
    pass "$description"
  else
    fail "$description (got: $out)"
  fi
}

# expect_status <description> <status> <command args...>
expect_status() {
  local description="$1" expected="$2" status
  shift 2
  set +e
  run "$@" >/dev/null 2>&1
  status=$?
  set -e
  if [ "$status" -eq "$expected" ]; then
    pass "$description"
  else
    fail "$description (status $status, expected $expected)"
  fi
}

# History:
#   main:  base(v1.9.0) - pr1 - v1.10.0 - pr2 - v2.0.0 - pr4 - rc(v3.0.0-rc1)
#   maint: from v1.10.0 - pr3 - v1.10.1
git_ init -q -b main
commit base >/dev/null
git_ tag v1.9.0
pr1="$(commit "PR 1")"
git_ tag v1.10.0
git_ branch maint
pr2="$(commit "PR 2")"
git_ tag v2.0.0
pr4="$(commit "PR 4")"
git_ tag v3.0.0-rc1
git_ checkout -q maint
pr3="$(commit "PR 3")"
git_ tag v1.10.1
git_ checkout -q main

expect "last-stable sorts versions numerically and skips pre-releases" v2.0.0 last-stable main
expect "last-stable on a maintenance branch stays on its line" v1.10.1 last-stable maint
expect "last-stable of a tag's parent excludes the tag itself" v1.10.0 last-stable "v2.0.0^"
expect "last-stable includes a tag on the ref itself" v1.9.0 last-stable v1.9.0
expect_status "last-stable rejects an unknown ref" 2 last-stable no-such-ref

prs="${work}/prs.tsv"
printf '1\t%s\tFirst break\n2\t%s\tSecond break\n3\t%s\tBackport break\n4\t%s\tLater break\n5\t%s\tUnknown commit\n' \
  "$pr1" "$pr2" "$pr3" "$pr4" "0000000000000000000000000000000000000000" >"$prs"
export BREAKING_PRS_FILE="$prs"
expect "breaking-prs counts only merges between the two tags" "#2 Second break" breaking-prs v1.10.0 v2.0.0
expect "breaking-prs on a backport sees only its own commits" "#3 Backport break" breaking-prs v1.10.0 v1.10.1
expect "breaking-prs spanning two releases lists both" "#1 First break
#2 Second break" breaking-prs v1.9.0 v2.0.0
expect "breaking-prs with no breaking merges prints nothing" "" breaking-prs v1.10.1 v1.10.1
unset BREAKING_PRS_FILE

expect_status "check-bump allows a minor bump with no breaks" 0 check-bump v1.9.0 v1.10.0 0
expect_status "check-bump allows a major bump with breaks" 0 check-bump v1.10.0 v2.0.0 1
expect_status "check-bump refuses a minor bump with breaks" 1 check-bump v1.9.0 v1.10.0 2
expect_status "check-bump refuses a major bump without breaks" 1 check-bump v1.10.0 v2.0.0 0
expect_status "check-bump allows 0.x to 1.0.0 without breaks" 0 check-bump v0.9.3 v1.0.0 0
expect_status "check-bump refuses 1.x to 2.0.0 without breaks" 1 check-bump v1.4.0 v2.0.0 0
expect_status "check-bump allows a major pre-release with breaks" 0 check-bump v1.0.0 v2.0.0-rc1 1
expect_status "check-bump refuses a minor pre-release with breaks" 1 check-bump v1.0.0 v1.1.0-rc1 1
expect_status "check-bump refuses a major pre-release without breaks" 1 check-bump v1.0.0 v2.0.0-rc1 0
expect_status "check-bump rejects a malformed tag" 2 check-bump v1.0.0 v2.0 1
expect_status "check-bump rejects a pre-release as the previous tag" 2 check-bump v1.0.0-rc1 v1.0.0 0
expect_status "an unknown command exits 2" 2 nonsense

if [ "$failures" -gt 0 ]; then
  echo "${failures} test(s) failed"
  exit 1
fi
echo "All release-tags tests passed"
