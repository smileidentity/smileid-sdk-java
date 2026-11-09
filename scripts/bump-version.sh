#!/usr/bin/env bash
# Bumps the SDK version and dates the Unreleased changelog section.
#
# Usage: scripts/bump-version.sh <major|minor|patch>
#
# Reads the current version from build.gradle.kts (the version release.yml
# checks the tag against), then:
#   - writes the new version to build.gradle.kts, Version.java, the tests that
#     pin it, example/build.gradle.kts and the README install snippets
#   - moves the CHANGELOG.md Unreleased section under a new
#     "## [x.y.z] - YYYY-MM-DD" heading, leaves an empty Unreleased section and
#     updates the compare links at the bottom
#   - prints the new version
#
# It changes nothing and exits 1 if the Unreleased section is empty, the new
# version already has a changelog entry, or a file is not in the expected
# shape. Set BUMP_VERSION_DATE=YYYY-MM-DD to fix the date (used by the tests).
# Run it from the repository root.
set -euo pipefail

usage() {
  echo "Usage: $0 <major|minor|patch>" >&2
  exit 2
}

fail() {
  echo "error: $*" >&2
  exit 1
}

[ "$#" -eq 1 ] || usage
part="$1"
case "$part" in
  major | minor | patch) ;;
  *) usage ;;
esac

build_file="build.gradle.kts"
version_file="src/main/java/com/smileidentity/Version.java"
version_test="src/test/java/com/smileidentity/VersionTest.java"
token_test="src/test/java/com/smileidentity/client/TokenLifecycleTest.java"
example_build="example/build.gradle.kts"
readme="README.md"
changelog="CHANGELOG.md"
repo_url="https://github.com/smileidentity/smileid-sdk-java"

for f in "$build_file" "$version_file" "$version_test" "$token_test" "$example_build" "$readme" "$changelog"; do
  [ -f "$f" ] || fail "$f not found. Run this script from the repository root."
done

current="$(sed -n 's/^version = "\([0-9]*\.[0-9]*\.[0-9]*\)"$/\1/p' "$build_file")"
[ -n "$current" ] || fail "$build_file has no line of the form: version = \"x.y.z\""
[ "$(printf '%s\n' "$current" | wc -l | tr -d ' ')" -eq 1 ] || fail "$build_file has more than one version line"

IFS=. read -r major minor patch <<EOF
$current
EOF
case "$part" in
  major) next="$((major + 1)).0.0" ;;
  minor) next="${major}.$((minor + 1)).0" ;;
  patch) next="${major}.${minor}.$((patch + 1))" ;;
esac
release_date="${BUMP_VERSION_DATE:-$(date -u +%Y-%m-%d)}"

current_re="${current//./\\.}"

# --- Check every file before changing any of them.

grep -q "^## \[Unreleased\]$" "$changelog" || fail "$changelog has no '## [Unreleased]' heading"
if grep -q "^## \[${next//./\\.}\]" "$changelog"; then
  fail "$changelog already has a '## [${next}]' entry"
fi
unreleased_body="$(awk '
  /^## \[Unreleased\]$/ { inside = 1; next }
  inside && /^## / { exit }
  inside && /^\[[^]]*\]: / { exit }
  inside { print }
' "$changelog" | grep -v '^[[:space:]]*$' || true)"
[ -n "$unreleased_body" ] || fail "$changelog has nothing under '## [Unreleased]'. Nothing to release."
grep -q "^\[Unreleased\]: ${repo_url}/compare/v${current_re}\.\.\.HEAD$" "$changelog" \
  || fail "$changelog has no '[Unreleased]: ${repo_url}/compare/v${current}...HEAD' link"

require() {
  # require <file> <basic regex> <description>
  grep -q "$2" "$1" || fail "$1 does not contain $3 for version ${current}"
}
require "$version_file" "VERSION = \"${current_re}\";" 'VERSION = "x.y.z"'
require "$version_test" "assertEquals(\"${current_re}\", Version.VERSION)" 'the pinned VERSION assertion'
require "$token_test" "\"${current_re}\"" 'the SDK version header assertion'
require "$token_test" "smileid-sdk-java/${current_re} (" 'the User-Agent assertion'
require "$example_build" "com.smileidentity:usesmileid-java:${current_re}\"" 'the SDK dependency'
require "$readme" "com.smileidentity:usesmileid-java:${current_re}\"" 'the Gradle install snippet'
require "$readme" "<version>${current_re}</version>" 'the Maven install snippet'

# --- Apply the changes. Each edit writes a temporary file and moves it into
# place, which works with both GNU and BSD sed.

replace() {
  # replace <file> <sed expression>
  local tmp
  tmp="$(mktemp)"
  sed "$2" "$1" >"$tmp"
  cat "$tmp" >"$1"
  rm -f "$tmp"
}

replace "$build_file" "s/^version = \"${current_re}\"$/version = \"${next}\"/"
replace "$version_file" "s/VERSION = \"${current_re}\";/VERSION = \"${next}\";/"
replace "$version_test" "s/assertEquals(\"${current_re}\", Version.VERSION)/assertEquals(\"${next}\", Version.VERSION)/"
replace "$token_test" "s/\"${current_re}\"/\"${next}\"/; s#smileid-sdk-java/${current_re} (#smileid-sdk-java/${next} (#"
replace "$example_build" "s/com.smileidentity:usesmileid-java:${current_re}\"/com.smileidentity:usesmileid-java:${next}\"/"
replace "$readme" "s/com.smileidentity:usesmileid-java:${current_re}\"/com.smileidentity:usesmileid-java:${next}\"/; s#<version>${current_re}</version>#<version>${next}</version>#"

tmp="$(mktemp)"
awk -v next_version="$next" -v current="$current" -v date="$release_date" -v url="$repo_url" '
  $0 == "## [Unreleased]" {
    print
    print ""
    print "## [" next_version "] - " date
    next
  }
  $0 == "[Unreleased]: " url "/compare/v" current "...HEAD" {
    print "[Unreleased]: " url "/compare/v" next_version "...HEAD"
    print "[" next_version "]: " url "/compare/v" current "...v" next_version
    next
  }
  { print }
' "$changelog" >"$tmp"
cat "$tmp" >"$changelog"
rm -f "$tmp"

echo "$next"
