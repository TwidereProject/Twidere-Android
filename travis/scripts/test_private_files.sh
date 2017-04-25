#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    return 0
fi

test -f twidere/src/google/AndroidManifest.xml