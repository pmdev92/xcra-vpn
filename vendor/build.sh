#!/bin/bash
set -ex

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
(
sh $SCRIPT_DIR/build_hev.sh
sh $SCRIPT_DIR/build_xray.sh
)


