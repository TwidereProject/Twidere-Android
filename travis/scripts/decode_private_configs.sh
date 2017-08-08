#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipped for pull request"
    exit 0
fi

openssl aes-256-cbc -K $encrypted_9b8203f9524d_key -iv $encrypted_9b8203f9524d_iv -in travis/configs/twidere_private_config.tar.gz.enc -out travis/configs/twidere_private_config.tar.gz -d
