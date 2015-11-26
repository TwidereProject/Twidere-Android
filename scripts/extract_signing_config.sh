#!/bin/bash

if [ -n ${SIGNING_CONFIG} ]; then
    echo ${SIGNING_CONFIG} | base64 -d | tar zxf -
    echo 'Extracted signing config'
fi