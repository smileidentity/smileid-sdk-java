#!/usr/bin/env bash
# Tests scripts/check-coverage-exclusions.sh in a throwaway git repository.
# Usage: scripts/test/check-coverage-exclusions-test.sh
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
script="${repo_root}/scripts/check-coverage-exclusions.sh"

failures=0
pass() { echo "ok - $1"; }
fail() {
  echo "not ok - $1"
  failures=$((failures + 1))
}

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

git_() { git -C "$work" -c user.name=test -c user.email=test@example.com "$@"; }

# Starts a repository with one commit on main and a feature branch.
setup() {
  rm -rf "$work"
  mkdir -p "$work/src/main/java/com/example/generated" "$work/src/main/java/com/example/client"
  git_ init -q -b main
  printf 'class A {\n  // nocov already on main is not reported\n}\n' >"$work/src/main/java/com/example/client/A.java"
  printf 'plugins { id("jacoco") }\n' >"$work/build.gradle.kts"
  git_ add -A
  git_ commit -q -m base
  git_ checkout -q -b feature
}

commit() {
  git_ add -A
  git_ commit -q -m change
}

# expect_hit <description> <expected output fragment>
expect_hit() {
  local out status
  set +e
  out="$(cd "$work" && "$script" main 2>&1)"
  status=$?
  set -e
  if [ "$status" -eq 1 ] && printf '%s' "$out" | grep -qF "$2"; then
    pass "$1"
  else
    fail "$1 (status $status, output: $out)"
  fi
}

expect_clean() {
  local out status
  set +e
  out="$(cd "$work" && "$script" main 2>&1)"
  status=$?
  set -e
  if [ "$status" -eq 0 ]; then
    pass "$1"
  else
    fail "$1 (status $status, output: $out)"
  fi
}

setup
printf 'class B {\n  int x() { return 1; }\n}\n' >"$work/src/main/java/com/example/client/B.java"
commit
expect_clean "ordinary code passes"

markers="// nocov
# pragma: no cover
/* c8 ignore next */
/* istanbul ignore next */
/** @codeCoverageIgnore */
#[coverage(off)]"
while IFS= read -r marker; do
  setup
  printf 'class C {\n  int ok;\n  %s\n}\n' "$marker" >"$work/src/main/java/com/example/client/C.java"
  commit
  expect_hit "reports '$marker' with its file and line" "src/main/java/com/example/client/C.java:3:"
done <<EOF
$markers
EOF

setup
printf 'class A {\n  // nocov already on main is not reported\n  int y;\n}\n' >"$work/src/main/java/com/example/client/A.java"
commit
expect_clean "markers already on the base branch are not reported"

setup
printf '@javax.annotation.processing.Generated("x")\nclass D {}\n' >"$work/src/main/java/com/example/client/D.java"
commit
expect_hit "reports @Generated outside generated/" "src/main/java/com/example/client/D.java:1:"

setup
printf '@Generated("generator")\nclass E {}\n' >"$work/src/main/java/com/example/generated/E.java"
commit
expect_clean "allows @Generated inside generated/"

setup
printf 'plugins { id("jacoco") }\ntasks.jacocoTestReport {\n  classDirectories.setFrom(fileTree("build") { exclude("**/client/**") })\n}\n' \
  >"$work/build.gradle.kts"
commit
expect_hit "reports a new exclude in the Gradle build" "build.gradle.kts:3:"

setup
printf 'plugins { id("jacoco") }\nval excludes = listOf("**/Foo*")\n' >"$work/build.gradle.kts"
commit
expect_hit "reports a new excludes list in the Gradle build" "build.gradle.kts:2:"

setup
mkdir -p "$work/scripts/test"
printf '# nocov\n' >"$work/scripts/test/fixture.sh"
commit
expect_clean "ignores the script's own tests"

setup
printf 'a\n-- not a header\n' >"$work/src/main/java/com/example/client/Notes.txt"
git_ add -A
git_ commit -q -m notes
printf 'a\n++ src/fake.java\n// nocov\n' >"$work/src/main/java/com/example/client/Notes.txt"
commit
expect_hit "an added line starting with '++ ' does not switch the file" \
  "src/main/java/com/example/client/Notes.txt:3:"

set +e
(cd "$work" && "$script" >/dev/null 2>&1)
status_none=$?
(cd "$work" && "$script" no-such-ref >/dev/null 2>&1)
status_bad=$?
set -e
if [ "$status_none" -eq 2 ]; then pass "no argument exits 2"; else fail "no argument exits 2 (got $status_none)"; fi
if [ "$status_bad" -eq 2 ]; then pass "an unknown ref exits 2"; else fail "an unknown ref exits 2 (got $status_bad)"; fi

if [ "$failures" -gt 0 ]; then
  echo "${failures} test(s) failed"
  exit 1
fi
echo "All check-coverage-exclusions tests passed"
