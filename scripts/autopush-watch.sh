#!/usr/bin/env bash
# File watcher that runs ./scripts/autopush.sh when project files change.
# macOS recommended: install fswatch (brew install fswatch).
# Usage: ./scripts/autopush-watch.sh [path-to-watch] [debounce-seconds]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WATCH_PATH=${1:-"$REPO_ROOT"}
DEBOUNCE=${2:-2}

# Path to autopush script
AUTOPUSH="$REPO_ROOT/scripts/autopush.sh"

if [ ! -x "$AUTOPUSH" ]; then
  echo "[autopush-watch] Warning: $AUTOPUSH is not executable. Making it executable..."
  chmod +x "$AUTOPUSH" || true
fi

# Prefer fswatch on macOS
if command -v fswatch >/dev/null 2>&1; then
  echo "[autopush-watch] Using fswatch to monitor changes under: $WATCH_PATH"
  fswatch -o "$WATCH_PATH" | while read -r _; do
    echo "[autopush-watch] Change detected — waiting $DEBOUNCE seconds to debounce..."
    sleep "$DEBOUNCE"
    echo "[autopush-watch] Running autopush..."
    "$AUTOPUSH" "Auto push (watch) $(date -u +%Y-%m-%dT%H:%M:%SZ)" || echo "[autopush-watch] autopush failed"
  done
else
  # Fallback polling (portable)
  echo "[autopush-watch] fswatch not found. Falling back to polling mode. Monitoring: $WATCH_PATH"
  TMPSTAMP="$REPO_ROOT/.autopush_ts"
  find "$WATCH_PATH" -type f -name '*.java' -o -name '*.yml' -o -name '*.xml' -o -name '*.md' -o -name '*.properties' | xargs stat -f "%m %N" 2>/dev/null | sort -n | tail -n1 | awk '{print $1}' > "$TMPSTAMP" || date +%s > "$TMPSTAMP"
  while true; do
    sleep "$DEBOUNCE"
    LATEST=$(find "$WATCH_PATH" -type f -name '*.java' -o -name '*.yml' -o -name '*.xml' -o -name '*.md' -o -name '*.properties' | xargs stat -f "%m %N" 2>/dev/null | sort -n | tail -n1 | awk '{print $1}' || echo $(date +%s))
    PREV=$(cat "$TMPSTAMP" 2>/dev/null || echo 0)
    if [ "$LATEST" -gt "$PREV" ]; then
      echo "[autopush-watch] Change detected — running autopush..."
      "$AUTOPUSH" "Auto push (watch) $(date -u +%Y-%m-%dT%H:%M:%SZ)" || echo "[autopush-watch] autopush failed"
      echo "$LATEST" > "$TMPSTAMP"
    fi
  done
fi

