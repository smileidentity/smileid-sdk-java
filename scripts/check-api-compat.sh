#!/usr/bin/env bash
# Compares the public API with the last release, the way the api-compat job
# in quality-gates.yml does.
#
# Usage:
#   scripts/check-api-compat.sh [<ref>]
#       Finds the newest stable vX.Y.Z tag that is <ref> or an ancestor of it
#       (default: HEAD), gets that release's jar and runs ./gradlew japicmp
#       against it. Exits non-zero if the working tree breaks that API.
#   scripts/check-api-compat.sh baseline-jar <tag> <dir>
#       Gets the jar for <tag> into <dir> and prints its path. The jar comes
#       from Maven Central; if the version is not there, it is built from the
#       tag. CI uses this to get the same jar.
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
artifact_url="https://repo1.maven.org/maven2/com/smileidentity/usesmileid-java"

usage() {
  sed -n '2,13p' "$0" >&2
  exit 2
}

baseline_jar() {
  local tag="$1" dir="$2" version jar src built
  version="${tag#v}"
  mkdir -p "$dir"
  jar="${dir}/usesmileid-java-${version}.jar"
  if curl -sSfL -o "$jar" "${artifact_url}/${version}/usesmileid-java-${version}.jar"; then
    echo "Downloaded ${version} from Maven Central." >&2
  else
    echo "${version} is not on Maven Central. Building the jar from ${tag}." >&2
    src="$(mktemp -d)/src"
    git -C "$repo_root" worktree add -q --detach "$src" "$tag"
    "${repo_root}/gradlew" -q -p "$src" jar >&2
    built="$(find "${src}/build/libs" -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | awk 'NR == 1')"
    if [ -z "$built" ]; then
      echo "error: building ${tag} produced no jar" >&2
      exit 1
    fi
    cp "$built" "$jar"
    git -C "$repo_root" worktree remove --force "$src"
  fi
  echo "$jar"
}

compare() {
  local ref="$1" tag jar
  tag="$("${repo_root}/scripts/release-tags.sh" last-stable "$ref")"
  if [ -z "$tag" ]; then
    echo "No vX.Y.Z release tag on ${ref}, so there is no public API to compare with."
    echo "Run 'git fetch --tags' if you expected one."
    exit 0
  fi
  echo "Comparing the working tree with ${tag}."
  jar="$(baseline_jar "$tag" "${repo_root}/build/api-baseline")"
  "${repo_root}/gradlew" -p "$repo_root" japicmp -PapiBaselineJar="$jar"
  echo "The public API is compatible with ${tag}. Report: build/reports/japicmp/japicmp.txt"
}

case "${1:-}" in
  baseline-jar)
    [ "$#" -eq 3 ] || usage
    baseline_jar "$2" "$3"
    ;;
  -h | --help) usage ;;
  *)
    [ "$#" -le 1 ] || usage
    compare "${1:-HEAD}"
    ;;
esac
