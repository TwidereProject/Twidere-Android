#!/bin/bash

if [ -n ${PRIVATE_BUILD_CONFIG} ]; then
    echo ${PRIVATE_BUILD_CONFIG} | base64 -d | tar zxf -
    echo 'Extracted signing config'
fi