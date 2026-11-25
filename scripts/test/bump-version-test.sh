#!/usr/bin/env bash
# Tests scripts/bump-version.sh against a copy of the repository's version files.
# Usage: scripts/test/bump-version-test.sh (from the repository root)
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
script="${repo_root}/scripts/bump-version.sh"
files=(
  build.gradle.kts
  src/main/java/com/smileidentity/Version.java
  src/test/java/com/smileidentity/VersionTest.java
  src/test/java/com/smileidentity/client/TokenLifecycleTest.java
  example/build.gradle.kts
  README.md
  CHANGELOG.md
)

failures=0
pass() { echo "ok - $1"; }
fail() {
  echo "not ok - $1"
  failures=$((failures + 1))
}

work=""
cleanup() { [ -z "$work" ] || rm -rf "$work"; }
trap cleanup EXIT

# Copies the version files into a fresh directory and sets the version to
# 12.2.0 with one Unreleased entry, so the tests do not depend on the real
# version or changelog.
setup() {
  cleanup
  work="$(mktemp -d)"
  for f in "${files[@]}"; do
    mkdir -p "${work}/$(dirname "$f")"
    cp "${repo_root}/${f}" "${work}/${f}"
  done
  current="$(sed -n 's/^version = "\(.*\)"$/\1/p' "${work}/build.gradle.kts")"
  for f in "${files[@]}"; do
    [ "$f" = CHANGELOG.md ] && continue
    sed "s/${current//./\\.}/12.2.0/g" "${work}/${f}" >"${work}/${f}.tmp"
    mv "${work}/${f}.tmp" "${work}/${f}"
  done
  cat >"${work}/CHANGELOG.md" <<'EOF'
# Changelog

## [Unreleased]

### Added

* Something new.

## [12.2.0] - 2026-10-08

### Changed

* Something changed.

[Unreleased]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.2.0...HEAD
[12.2.0]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.1.0...v12.2.0
EOF
}

bump() {
  (cd "$work" && BUMP_VERSION_DATE=2026-11-09 "$script" "$@")
}

# --- patch bump updates every file and the changelog
setup
if out="$(bump patch 2>&1)" && [ "$out" = "12.2.1" ]; then
  pass "patch bump prints the new version"
else
  fail "patch bump prints the new version (got: $out)"
fi
remaining="$(
  cd "$work"
  grep -l '12\.2\.0' "${files[@]}" | grep -v CHANGELOG.md
)" || true
if [ -z "$remaining" ]; then
  pass "patch bump leaves no old version outside the changelog"
else
  fail "patch bump left the old version in: $remaining"
fi
for f in "${files[@]}"; do
  [ "$f" = CHANGELOG.md ] && continue
  if grep -q '12\.2\.1' "${work}/${f}"; then
    pass "patch bump writes the new version to $f"
  else
    fail "patch bump writes the new version to $f"
  fi
done
expected_changelog='# Changelog

## [Unreleased]

## [12.2.1] - 2026-11-09

### Added

* Something new.

## [12.2.0] - 2026-10-08

### Changed

* Something changed.

[Unreleased]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.2.1...HEAD
[12.2.1]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.2.0...v12.2.1
[12.2.0]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.1.0...v12.2.0'
if [ "$(cat "${work}/CHANGELOG.md")" = "$expected_changelog" ]; then
  pass "patch bump moves Unreleased under a dated heading and updates the links"
else
  fail "patch bump moves Unreleased under a dated heading and updates the links"
  diff <(printf '%s\n' "$expected_changelog") "${work}/CHANGELOG.md" || true
fi

# --- a second bump has nothing to release and changes nothing
before="$(cd "$work" && cat "${files[@]}" | cksum)"
if out="$(bump patch 2>&1)"; then
  fail "second bump fails when Unreleased is empty"
else
  case "$out" in
    *"Nothing to release"*) pass "second bump fails when Unreleased is empty" ;;
    *) fail "second bump explains that Unreleased is empty (got: $out)" ;;
  esac
fi
if [ "$(cd "$work" && cat "${files[@]}" | cksum)" = "$before" ]; then
  pass "a failed bump changes no files"
else
  fail "a failed bump changes no files"
fi

# --- minor and major bumps
setup
out="$(bump minor)"
if [ "$out" = "12.3.0" ]; then pass "minor bump gives 12.3.0"; else fail "minor bump gives 12.3.0 (got: $out)"; fi
setup
out="$(bump major)"
if [ "$out" = "13.0.0" ]; then pass "major bump gives 13.0.0"; else fail "major bump gives 13.0.0 (got: $out)"; fi
if grep -q '^## \[13\.0\.0\] - 2026-11-09$' "${work}/CHANGELOG.md"; then
  pass "major bump writes the changelog heading"
else
  fail "major bump writes the changelog heading"
fi

# --- an exact version jumps straight to it and updates everything a part bump does
setup
if out="$(bump 13.1.0 2>&1)" && [ "$out" = "13.1.0" ]; then
  pass "exact version prints the new version"
else
  fail "exact version prints the new version (got: $out)"
fi
remaining="$(
  cd "$work"
  grep -l '12\.2\.0' "${files[@]}" | grep -v CHANGELOG.md
)" || true
if [ -z "$remaining" ]; then
  pass "exact version leaves no old version outside the changelog"
else
  fail "exact version left the old version in: $remaining"
fi
for f in "${files[@]}"; do
  [ "$f" = CHANGELOG.md ] && continue
  if grep -q '13\.1\.0' "${work}/${f}"; then
    pass "exact version writes the new version to $f"
  else
    fail "exact version writes the new version to $f"
  fi
done
if grep -q '^## \[13\.1\.0\] - 2026-11-09$' "${work}/CHANGELOG.md" \
  && grep -qF '[Unreleased]: https://github.com/smileidentity/smileid-sdk-java/compare/v13.1.0...HEAD' "${work}/CHANGELOG.md" \
  && grep -qF '[13.1.0]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.2.0...v13.1.0' "${work}/CHANGELOG.md"; then
  pass "exact version moves Unreleased under a dated heading and updates the links"
else
  fail "exact version moves Unreleased under a dated heading and updates the links"
fi
setup
out="$(bump 12.2.1)"
if [ "$out" = "12.2.1" ]; then pass "exact version 12.2.1 is accepted"; else fail "exact version 12.2.1 is accepted (got: $out)"; fi

# --- an exact version that is not greater, or malformed, fails and changes nothing
for v in 12.2.0 12.1.9 11.9.9; do
  setup
  before="$(cd "$work" && cat "${files[@]}" | cksum)"
  if out="$(bump "$v" 2>&1)"; then
    fail "exact version $v (not greater) fails"
  elif [ "$(cd "$work" && cat "${files[@]}" | cksum)" != "$before" ]; then
    fail "exact version $v (not greater) fails but changed files"
  else
    case "$out" in
      *"not greater than the current version 12.2.0"*) pass "exact version $v (not greater) fails and changes no files" ;;
      *) fail "exact version $v explains it is not greater (got: $out)" ;;
    esac
  fi
done
for v in 13.0 13.0.0.1 v13.0.0 13.01.0 13.0.0-rc1 " 13.0.0" 1x.0.0; do
  setup
  before="$(cd "$work" && cat "${files[@]}" | cksum)"
  set +e
  out="$(bump "$v" 2>&1)"
  status=$?
  set -e
  if [ "$status" -ne 1 ]; then
    fail "malformed version '$v' exits 1 (got $status)"
  elif [ "$(cd "$work" && cat "${files[@]}" | cksum)" != "$before" ]; then
    fail "malformed version '$v' fails but changed files"
  else
    case "$out" in
      *"is not a valid version"*) pass "malformed version '$v' fails and changes no files" ;;
      *) fail "malformed version '$v' explains the problem (got: $out)" ;;
    esac
  fi
done

# --- a file in the wrong shape stops the bump before anything changes
setup
sed 's/VERSION = "12.2.0";/VERSION = "9.9.9";/' "${work}/src/main/java/com/smileidentity/Version.java" \
  >"${work}/v.tmp" && mv "${work}/v.tmp" "${work}/src/main/java/com/smileidentity/Version.java"
before="$(cd "$work" && cat "${files[@]}" | cksum)"
if bump patch >/dev/null 2>&1; then
  fail "a mismatched Version.java stops the bump"
elif [ "$(cd "$work" && cat "${files[@]}" | cksum)" = "$before" ]; then
  pass "a mismatched Version.java stops the bump and changes no files"
else
  fail "a mismatched Version.java stops the bump but changed files"
fi

# --- bad arguments
setup
set +e
bump >/dev/null 2>&1
status_none=$?
bump huge >/dev/null 2>&1
status_bad=$?
set -e
if [ "$status_none" -eq 2 ]; then pass "no argument exits 2"; else fail "no argument exits 2 (got $status_none)"; fi
if [ "$status_bad" -eq 2 ]; then pass "an unknown part exits 2"; else fail "an unknown part exits 2 (got $status_bad)"; fi

if [ "$failures" -gt 0 ]; then
  echo "${failures} test(s) failed"
  exit 1
fi
echo "All bump-version tests passed"
