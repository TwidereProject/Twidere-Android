#!/bin/bash

fastlane --version

set -o allexport
source $1
fastlane supply run
retcode=$?
set +o allexport

exit $retcode