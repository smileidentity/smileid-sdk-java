#!/usr/bin/env bash
# Release tag helpers shared by the api-compat job and release.yml.
#
# Usage:
#   scripts/release-tags.sh last-stable <ref>
#       Prints the newest stable tag (vX.Y.Z, no pre-release suffix) that is
#       <ref> or an ancestor of it. Prints nothing if there is none.
#   scripts/release-tags.sh breaking-prs <previous-tag> <tag>
#       Prints "#<number> <title>" for each merged pull request labelled
#       breaking whose merge commit is in <previous-tag>..<tag>. Needs gh and
#       GH_TOKEN. For tests, set BREAKING_PRS_FILE to a file of
#       "<number><TAB><merge commit><TAB><title>" lines to use instead of gh.
#   scripts/release-tags.sh check-bump <previous-tag> <tag> <breaking-count>
#       Fails if <breaking-count> breaking pull requests merged and the major
#       version did not increase, or if the major version increased with none.
#       Going from 0.x to 1.0.0 needs no breaking pull request.
set -euo pipefail

usage() {
  sed -n '2,17p' "$0" >&2
  exit 2
}

stable_re='^v[0-9]+\.[0-9]+\.[0-9]+$'

last_stable() {
  local ref="$1"
  git rev-parse --verify --quiet "${ref}^{commit}" >/dev/null || {
    echo "error: '${ref}' is not a commit" >&2
    exit 2
  }
  # Sort ascending by version and let awk print the last line: awk reads all
  # of its input, so nothing upstream gets SIGPIPE.
  git tag --merged "$ref" --list 'v*' \
    | { grep -E "$stable_re" || true; } \
    | sed 's/^v//' \
    | sort -t. -k1,1n -k2,2n -k3,3n \
    | awk 'END { if (NR) print "v" $0 }'
}

breaking_prs() {
  local previous="$1" tag="$2" number oid title
  local list
  if [ -n "${BREAKING_PRS_FILE:-}" ]; then
    list="$(cat "$BREAKING_PRS_FILE")"
  else
    list="$(gh pr list --state merged --label breaking --limit 1000 \
      --json number,mergeCommit,title \
      --jq '.[] | select(.mergeCommit != null) | "\(.number)\t\(.mergeCommit.oid)\t\(.title)"')"
  fi
  while IFS=$'\t' read -r number oid title; do
    [ -n "$number" ] || continue
    git cat-file -e "${oid}^{commit}" 2>/dev/null || continue
    if git merge-base --is-ancestor "$oid" "$tag" \
      && ! git merge-base --is-ancestor "$oid" "$previous"; then
      echo "#${number} ${title}"
    fi
  done <<<"$list"
}

check_bump() {
  local previous="$1" tag="$2" count="$3"
  local previous_major major
  [[ "$previous" =~ $stable_re ]] || {
    echo "error: '${previous}' is not a vX.Y.Z tag" >&2
    exit 2
  }
  [[ "$tag" =~ $stable_re ]] || {
    echo "error: '${tag}' is not a vX.Y.Z tag" >&2
    exit 2
  }
  previous_major="${previous#v}"
  previous_major="${previous_major%%.*}"
  major="${tag#v}"
  major="${major%%.*}"
  if [ "$count" -gt 0 ] && [ "$major" -le "$previous_major" ]; then
    echo "error: ${count} pull request(s) labelled breaking merged since ${previous}, so ${tag} must be a new major version." >&2
    exit 1
  fi
  if [ "$count" -eq 0 ] && [ "$major" -gt "$previous_major" ]; then
    if [ "$previous_major" -eq 0 ] && [ "$major" -eq 1 ]; then
      echo "${tag} stabilises the 0.x API; no breaking pull request needed."
      return 0
    fi
    echo "error: ${tag} is a new major version, but no pull request labelled breaking merged since ${previous}. Label the breaking pull request, or release a minor or patch version." >&2
    exit 1
  fi
  echo "${tag} is a valid bump from ${previous} with ${count} breaking pull request(s)."
}

[ "$#" -ge 1 ] || usage
command="$1"
shift
case "$command" in
  last-stable)
    [ "$#" -eq 1 ] || usage
    last_stable "$1"
    ;;
  breaking-prs)
    [ "$#" -eq 2 ] || usage
    breaking_prs "$1" "$2"
    ;;
  check-bump)
    [ "$#" -eq 3 ] || usage
    check_bump "$1" "$2" "$3"
    ;;
  *) usage ;;
esac
