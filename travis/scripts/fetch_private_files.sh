#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipped for pull request"
    exit 0
fi

# Extracts build configs into source tree
tar zxf travis/configs/twidere_private_config.tar.gz
# Make sure ~/.ssh/ exists
mkdir -p ~/.ssh/
# Make it secure
chmod 700 ~/.ssh/
# Append ssh_config
cat private/ssh_config >> ~/.ssh/config
# Append known_hosts
cat private/ssh_known_hosts >> ~/.ssh/known_hosts
# Clone Google components
ssh-agent bash -c "ssh-add private/ssh_id_rsa; git clone $COMPONENT_GOOGLE_REPO twidere/src/google" > /dev/null 2>&1
# Force reset to required commit id
git -C twidere/src/google reset --hard `cat twidere/src/.google.commit-id` > /dev/null 2>&1
# Dropbox accessToken for uploading logs
cat private/dropbox_uploader >> ~/.dropbox_uploader
