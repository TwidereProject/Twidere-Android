#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    patch --dry-run -d twidere < twidere/patches/remove_closed_source_dependencies.patch
else
    echo "Apply real patch for pull request"
    patch -d twidere < twidere/patches/remove_closed_source_dependencies.patch
fi
