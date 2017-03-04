#!/bin/bash

echo "Current working dir is $(pwd)"

ls

for log_file in hs_err_*.log; do
  echo "Error log ${log_file}:"
  cat ${log_file}
done