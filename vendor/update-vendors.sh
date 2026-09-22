#!/bin/bash
set -ex

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "Script dir detected: $SCRIPT_DIR"

echo "=== Sync vendor submodules ==="
(
  rm -rf "$SCRIPT_DIR/xray-core-rust"
  rm -rf "$SCRIPT_DIR/hev-socks5-tunnel"
  git submodule sync --recursive
  git submodule update --init --recursive
  git submodule status --recursive
)

echo "=== Updating vendor submodules to latest tags ==="
(
  cd "$SCRIPT_DIR/xray-core-rust"
  echo "Fetching xray-core-rust..."
  git fetch --tags origin 2>/dev/null || git fetch --tags 2>/dev/null || true
  LATEST_TAG=$(git tag --sort=-creatordate | head -n1 || echo "")
  echo "Latest tag: $LATEST_TAG"
  if [[ -n "$LATEST_TAG" ]]; then
      git checkout "$LATEST_TAG" || git reset --hard "$LATEST_TAG"
  fi
)

(
  cd "$SCRIPT_DIR/hev-socks5-tunnel"
  echo "Fetching hev-socks5-tunnel..."
  git fetch --tags origin 2>/dev/null || git fetch --tags 2>/dev/null || true
  LATEST_TAG=$(git tag --sort=-creatordate | head -n1 || echo "")
  echo "Latest tag: $LATEST_TAG"
  if [[ -n "$LATEST_TAG" ]]; then
      git checkout "$LATEST_TAG" || git reset --hard "$LATEST_TAG"
  fi
)


echo "=== Done ==="
