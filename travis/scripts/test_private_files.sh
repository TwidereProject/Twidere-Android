#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    exit 0
fi

test -f twidere/src/google/AndroidManifest.xml
