#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

(
cd $SCRIPT_DIR
if [ -z "$ANDROID_HOME" ]; then
  export ANDROID_HOME="$HOME/Library/Android/sdk"
fi

if [ -z "$ANDROID_NDK_HOME" ]; then
  export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/28.2.13676358"
fi

if [ -z "$ANDROID_NDK" ]; then
  export ANDROID_NDK="$ANDROID_NDK_HOME"
fi

$ANDROID_NDK_HOME/ndk-build \
NDK_PROJECT_PATH=$SCRIPT_DIR \
APP_BUILD_SCRIPT=$SCRIPT_DIR/Android.mk \
NDK_OUT=$SCRIPT_DIR/../build/ndk/obj \
NDK_LIBS_OUT=$SCRIPT_DIR/../app/src/main/jniLibs
)



