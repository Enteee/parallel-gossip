#!/bin/bash
GIT_ROOT=$(git rev-parse --show-toplevel)
BIN="${GIT_ROOT}/bin/"

(
	for f in $(find "${BIN}" -iname "node-*") ; do
		sed -nre "s/.*Replica\[(.*)\].*(messages count:.*)/\1 : \2/pg" "${f}"  | tail -n 1
	done
) | sort -n
