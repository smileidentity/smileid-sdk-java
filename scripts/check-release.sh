#!/usr/bin/env bash
# Runs the checks a release tag must pass before release.yml publishes it.
# release.yml runs this on the tag, and the release-guards job in
# quality-gates.yml runs it on release pull requests, so a bad release is
# caught before the tag is pushed and the version is burnt.
#
# Usage: scripts/check-release.sh <tag> [<rev>]
#
# <tag> is the release tag, such as v13.1.0. <rev> is the commit being
# released and defaults to <tag>. Pass a <rev> (such as HEAD) before tagging:
# the check then also fails if <tag> already exists.
#
# Checks:
#   - <tag> is vX.Y.Z or vX.Y.Z-<pre-release>
#   - the version in build.gradle.kts and Version.java matches <tag>
#   - CHANGELOG.md has a dated "## [x.y.z] - YYYY-MM-DD" heading
#   - the major version matches the breaking pull requests since the last
#     release (scripts/release-tags.sh; needs gh and GH_TOKEN, or
#     BREAKING_PRS_FILE for tests)
set -euo pipefail

scripts_dir="$(cd "$(dirname "$0")" && pwd)"
release_re='^v[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$'
failures=0

error() {
  echo "::error::$*"
  failures=$((failures + 1))
}

if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
  sed -n '2,19p' "$0" >&2
  exit 2
fi
tag="$1"
rev="${2:-$1}"

if [[ ! "$tag" =~ $release_re ]]; then
  echo "::error::${tag} is not a vX.Y.Z or vX.Y.Z-<pre-release> tag."
  exit 1
fi
version="${tag#v}"

if [ "$#" -eq 2 ] && git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  error "Tag ${tag} already exists. Tags can't be moved, so release a new version."
fi

build_version="$(sed -n 's/^version = "\(.*\)"$/\1/p' build.gradle.kts)"
if [ "$build_version" != "$version" ]; then
  error "${tag} would publish version '${build_version}' from build.gradle.kts. Run scripts/bump-version.sh, or fix the version."
fi

sdk_version="$(sed -n 's/.*VERSION = "\(.*\)";.*/\1/p' src/main/java/com/smileidentity/Version.java)"
if [ "$sdk_version" != "$version" ]; then
  error "Version.java has VERSION '${sdk_version}', but the tag is ${tag}. Run scripts/bump-version.sh, or fix the version."
fi

if ! grep -qE "^## \[${version//./\\.}\] - [0-9]{4}-[0-9]{2}-[0-9]{2}" CHANGELOG.md; then
  error "CHANGELOG.md has no '## [${version}] - YYYY-MM-DD' heading. Run scripts/bump-version.sh, or add the entry."
fi

# The previous stable tag that is an ancestor of this release, so a backport
# on an older line sees only its own commits.
previous_tag="$("${scripts_dir}/release-tags.sh" last-stable "${rev}^")"
if [ -z "$previous_tag" ]; then
  echo "::notice::No earlier vX.Y.Z tag, so there is no version bump to check."
else
  breaking="$("${scripts_dir}/release-tags.sh" breaking-prs "$previous_tag" "$rev")"
  count=0
  if [ -n "$breaking" ]; then
    echo "Pull requests labelled breaking since ${previous_tag}:"
    echo "$breaking"
    count="$(printf '%s\n' "$breaking" | wc -l | tr -d ' ')"
  fi
  if ! "${scripts_dir}/release-tags.sh" check-bump "$previous_tag" "$tag" "$count"; then
    error "${tag} does not match the breaking pull requests merged since ${previous_tag}. See the message above."
  fi
fi

if [ "$failures" -gt 0 ]; then
  exit 1
fi
echo "${tag} passes the release checks."
