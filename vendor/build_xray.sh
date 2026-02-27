#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
(
cd $SCRIPT_DIR/xray-core-rust
sh ./xray_scripts/build_android.sh all
mkdir $SCRIPT_DIR/../app/libs
cp $SCRIPT_DIR/xray-core-rust/build/LibXrayCoreRust.aar $SCRIPT_DIR/../app/libs/LibXrayCoreRust.aar
rm -rf $SCRIPT_DIR/xray-core-rust/build
rm -rf $SCRIPT_DIR/xray-core-rust/target
)


