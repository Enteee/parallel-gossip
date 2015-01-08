#!/bin/bash
GIT_ROOT=$(git rev-parse --show-toplevel)
BIN="${GIT_ROOT}/bin/"

pkill -f gossip
echo "cleanup logs [ENTER]"
read
echo rm -rf "${BIN}/*.log*"
