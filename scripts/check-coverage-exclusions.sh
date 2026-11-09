#!/usr/bin/env bash
# Fails if the lines this branch adds exclude code from coverage.
#
# Usage: scripts/check-coverage-exclusions.sh <base-ref>
#
# Scans the lines added in `git diff <base-ref>...HEAD` for coverage exclusion
# markers, prints each hit with its file and line number, and exits 1 if there
# are any. CI runs it on every pull request and fails bot pull requests on a hit.
#
# It looks for:
#   - exclusion comments from any language: nocov, pragma: no cover, c8 ignore,
#     istanbul ignore, @codeCoverageIgnore, #[coverage(off)]
#   - a @Generated annotation added outside the generated/ tree (JaCoCo skips
#     classes and methods annotated @Generated)
#   - a new excludes entry in the Gradle build or the JaCoCo configuration
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <base-ref>" >&2
  exit 2
fi
base_ref="$1"

if ! git rev-parse --verify --quiet "${base_ref}^{commit}" >/dev/null; then
  echo "error: '${base_ref}' is not a commit. Fetch it first, for example: git fetch origin main" >&2
  exit 2
fi

comment_pattern='nocov|pragma:[[:space:]]*no[[:space:]]+cover|c8[[:space:]]+ignore|istanbul[[:space:]]+ignore|@codeCoverageIgnore|#\[coverage\(off\)\]'
generated_pattern='@([A-Za-z_][A-Za-z0-9_]*\.)*Generated([^A-Za-z0-9_]|$)'
excludes_pattern='<excludes?>|(^|[^A-Za-z0-9_])excludes?([^A-Za-z0-9_]|$)'

hits=0
file=""
line_no=0
in_hunk=0
saw_minus=0

report() {
  echo "${file}:${line_no}: $1: $2"
  hits=$((hits + 1))
}

# Walk the zero-context diff, tracking the file and the new-side line number.
# A "+++ " line names a file only when it directly follows a "--- " line
# outside a hunk; inside a hunk it is an added line that starts with "++ ".
while IFS= read -r diff_line; do
  if [ "$in_hunk" -eq 0 ]; then
    case "$diff_line" in
      '--- '*)
        saw_minus=1
        continue
        ;;
      '+++ '*)
        if [ "$saw_minus" -eq 1 ]; then
          file="${diff_line#+++ }"
          file="${file#b/}"
        fi
        saw_minus=0
        continue
        ;;
    esac
  fi
  saw_minus=0
  case "$diff_line" in
    'diff --git '*)
      in_hunk=0
      ;;
    '@@ '*)
      # @@ -a,b +c,d @@: the new side starts at line c.
      in_hunk=1
      new_side="${diff_line#*+}"
      line_no="${new_side%%[, ]*}"
      ;;
    '+'*)
      [ "$in_hunk" -eq 1 ] || continue
      added="${diff_line#+}"
      case "$file" in
        # This script and its test describe the markers; they do not use them.
        scripts/check-coverage-exclusions.sh | scripts/test/*) ;;
        *)
          if [[ "$added" =~ $comment_pattern ]]; then
            report "coverage exclusion comment" "$added"
          fi
          case "$file" in
            */generated/*) ;;
            *.java | *.kt)
              if [[ "$added" =~ $generated_pattern ]]; then
                report "@Generated outside generated/ hides code from JaCoCo" "$added"
              fi
              ;;
          esac
          case "$file" in
            *.gradle | *.gradle.kts | pom.xml | */pom.xml | *jacoco*)
              lowered="$(printf '%s' "$added" | tr '[:upper:]' '[:lower:]')"
              if [[ "$lowered" =~ $excludes_pattern ]]; then
                report "new coverage excludes entry" "$added"
              fi
              ;;
          esac
          ;;
      esac
      line_no=$((line_no + 1))
      ;;
  esac
done < <(git diff --unified=0 --no-color --no-ext-diff "${base_ref}...HEAD")

if [ "$hits" -gt 0 ]; then
  echo "Found ${hits} coverage exclusion(s). Add tests instead of excluding code." >&2
  exit 1
fi
echo "No coverage exclusions added since ${base_ref}."
