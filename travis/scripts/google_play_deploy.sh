#!/bin/bash

set -o allexport
source ./twidere/src/google/.supplyrc
fastlane supply run
set +o allexport