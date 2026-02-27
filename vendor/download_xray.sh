#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
(
curl -L https://github.com/pmdev92/xray-core-rust/releases/latest/download/LibXrayCoreRust.aar.zip -o LibXrayCoreRust.aar.zip
unzip LibXrayCoreRust.aar.zip -d $SCRIPT_DIR/../app/libs
rm -rf ./LibXrayCoreRust.aar.zip
)


