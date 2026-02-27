#!/bin/bash
set -ex

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
(
git submodule sync --recursive
git submodule update --init --recursive
git submodule status --recursive
sh $SCRIPT_DIR/build_hev.sh
sh $SCRIPT_DIR/build_xray.sh
)


