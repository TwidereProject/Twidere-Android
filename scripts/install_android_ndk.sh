#!/bin/bash

if [ `uname -m` = x86_64 ];
then
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O ndk.bin
else
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86.bin -O ndk.bin
fi

7z x ndk.bin android-ndk-r10e/build/ android-ndk-r10e/ndk-build android-ndk-r10e/platforms/android-21 > /dev/null
export ANDROID_NDK_HOME=`pwd`/android-ndk-r10e
echo "ndk.dir=$ANDROID_NDK_HOME" >> local.properties