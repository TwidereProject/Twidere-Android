#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipped for pull request"
    exit 0
fi

curl 'https://raw.githubusercontent.com/andreafabrizi/Dropbox-Uploader/master/dropbox_uploader.sh' -o travis/scripts/dropbox_uploader.sh
chmod +x travis/scripts/dropbox_uploader.sh
find /tmp -maxdepth 1 -name 'kotlin-daemon.*.log' -exec sh -c 'travis/scripts/dropbox_uploader.sh upload $1 $TRAVIS_BUILD_ID/$(basename $1)' find-sh {} \;
find /tmp -maxdepth 1 -name 'hs_err_pid*.log' -exec sh -c 'travis/scripts/dropbox_uploader.sh upload $1 $TRAVIS_BUILD_ID/$(basename $1)' find-sh {} \;
find ~/.gradle/daemon/ -name 'daemon-*.log' -exec sh -c 'travis/scripts/dropbox_uploader.sh upload $1 $TRAVIS_BUILD_ID/gradle-$(basename $1)' find-sh {} \;
dmesg > dmesg.log; travis/scripts/dropbox_uploader.sh upload dmesg.log ${TRAVIS_BUILD_ID}/dmesg.log
