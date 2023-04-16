#!/usr/bin/env bash

# exit code will be non-zero if google-java-format would have changed a file
# prints files not conforming with google's java style

# shellcheck disable=2046 # do not prevend word splitting for the command substitution
google-java-format --dry-run --set-exit-if-changed $(find src -name '*.java')
