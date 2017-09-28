#!/bin/bash

set -o allexport
source $1
#https://docs.travis-ci.com/user/deployment/script/#Ruby-version
rvm default exec fastlane supply run
retcode=$?
set +o allexport

exit $retcode