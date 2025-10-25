#!/usr/bin/env bash
# Auto-push script: run tests, commit changes, and push to the current branch
# Usage: ./scripts/autopush.sh [--skip-tests] [--dry-run] ["commit message"]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SKIP_TESTS=false
DRY_RUN=false
MSG=""

# Simple argument parsing
while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-tests)
      SKIP_TESTS=true; shift ;;
    --dry-run)
      DRY_RUN=true; shift ;;
    --)
      shift; break ;;
    *)
      # remaining args form the commit message
      if [ -z "$MSG" ]; then
        MSG="$*"
        break
      else
        shift
      fi
      ;;
  esac
done

# Helper: build message from a given porcelain-style status input
build_msg_from_status() {
  local STATUS_TEXT="$1"
  if [ -z "$STATUS_TEXT" ]; then
    echo "chore: no changes"
    return
  fi

  # Normalize lines to: CODE<TAB>FILENAME (handles both name-status and porcelain)
  local NORM
  NORM=$(printf "%s" "$STATUS_TEXT" | awk '
    { code=$1; file=$NF;
      # If code looks like two-char porcelain (e.g. " M"), reduce to the non-space char
      if (match(code, /[MADTRC?]/)) {
        printf "%s\t%s\n", code, file
      } else if (code ~ /^R/) {
        # rename lines from git diff --name-status may have old and new names; use last field
        printf "%s\t%s\n", code, file
      } else {
        printf "%s\t%s\n", code, file
      }
    }'
  )

  ADDED=$(printf "%s" "$NORM" | awk -F"\t" '$1 ~ /A|\?/ {print $2}' | tr '\n' ',' | sed 's/,$//')
  MODIFIED=$(printf "%s" "$NORM" | awk -F"\t" '$1 ~ /M/ {print $2}' | tr '\n' ',' | sed 's/,$//')
  DELETED=$(printf "%s" "$NORM" | awk -F"\t" '$1 ~ /D/ {print $2}' | tr '\n' ',' | sed 's/,$//')
  RENAMED=$(printf "%s" "$NORM" | awk -F"\t" '$1 ~ /^R/ {print $2}' | tr '\n' ',' | sed 's/,$//')

  # Insert spaces after commas for readability
  ADDED=$(printf "%s" "$ADDED" | sed 's/,/, /g')
  MODIFIED=$(printf "%s" "$MODIFIED" | sed 's/,/, /g')
  DELETED=$(printf "%s" "$DELETED" | sed 's/,/, /g')
  RENAMED=$(printf "%s" "$RENAMED" | sed 's/,/, /g')

  PARTS=()
  if [ -n "$ADDED" ]; then PARTS+=("add: $ADDED"); fi
  if [ -n "$MODIFIED" ]; then PARTS+=("update: $MODIFIED"); fi
  if [ -n "$RENAMED" ]; then PARTS+=("rename: $RENAMED"); fi
  if [ -n "$DELETED" ]; then PARTS+=("remove: $DELETED"); fi

  if [ ${#PARTS[@]} -gt 0 ]; then
    local JOINED=""
    for p in "${PARTS[@]}"; do
      if [ -z "$JOINED" ]; then
        JOINED="$p"
      else
        JOINED="$JOINED; $p"
      fi
    done
    echo "$JOINED"
  else
    echo "chore: update"
  fi
}

# Determine staged changes (we will only commit these)
STAGED_STATUS=$(git diff --cached --name-status || true)

# For dry-run, also get unstaged status to preview
UNSTAGED_STATUS=$(git status --porcelain || true)

# If no explicit message provided, create one from staged if present; otherwise for dry-run preview from unstaged
if [ -z "$MSG" ]; then
  if [ -n "$STAGED_STATUS" ]; then
    MSG=$(build_msg_from_status "$STAGED_STATUS")
  elif [ "$DRY_RUN" = true ] && [ -n "$UNSTAGED_STATUS" ]; then
    # Build a preview message from unstaged changes for dry-run only
    MSG=$(build_msg_from_status "$UNSTAGED_STATUS")
  else
    # No staged changes and not a dry-run: instruct the user to stage changes first
    if [ "$DRY_RUN" = false ]; then
      echo "[autopush] No staged changes detected. Please 'git add' the files you want to commit, or run with --dry-run to preview." >&2
      exit 0
    else
      MSG="chore: no changes"
    fi
  fi
fi

echo "[autopush] Dry run: $DRY_RUN, Skip tests: $SKIP_TESTS"

if [ "$SKIP_TESTS" = false ]; then
  echo "[autopush] Running mvn test..."
  if ! ./mvnw -q test; then
    echo "[autopush] Tests failed — aborting push." >&2
    exit 1
  fi
else
  echo "[autopush] Skipping tests as requested."
fi

# Show git status for dry-run convenience
if [ "$DRY_RUN" = true ]; then
  echo "[autopush] === DRY RUN ==="
  echo "[autopush] Would commit staged changes with message: $MSG and push to current branch"
  echo "[autopush] Staged changes (to be committed):"
  if [ -n "$STAGED_STATUS" ]; then
    echo "$STAGED_STATUS"
  else
    echo "  (none)"
  fi
  echo "[autopush] Unstaged changes (preview only):"
  echo "$UNSTAGED_STATUS"
  exit 0
fi

# Check again: require staged changes
if [ -z "$STAGED_STATUS" ]; then
  echo "[autopush] No staged changes to commit. Please stage files (git add) first." >&2
  exit 0
fi

# Commit
git commit -m "$MSG"

# Push to the current branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)
REMOTE=${GIT_REMOTE:-origin}

echo "[autopush] Pushing to $REMOTE/$BRANCH..."
# Use default git push behavior; assumes credentials (SSH key or cached HTTPS) are set up
git push "$REMOTE" "$BRANCH"

echo "[autopush] Push complete."
