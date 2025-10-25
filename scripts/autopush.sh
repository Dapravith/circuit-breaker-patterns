#!/usr/bin/env bash
# Auto-push script: run tests, commit changes, and push to the current branch
# Usage: ./scripts/autopush.sh ["commit message"]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# Optional commit message from first arg, otherwise auto-generate
if [ $# -ge 1 ]; then
  MSG="$*"
else
  MSG="Auto-update: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
fi

echo "[autopush] Running mvn test..."
if ! ./mvnw -q test; then
  echo "[autopush] Tests failed — aborting push." >&2
  exit 1
fi

# Stage all changes
git add -A

# Check if there is anything to commit
if git diff --cached --quiet; then
  echo "[autopush] No staged changes to commit. Exiting."
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

