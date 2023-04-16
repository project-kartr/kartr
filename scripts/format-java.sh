#!/usr/bin/env bash

# do not prevend word splitting for the command substitution (java files don't contain spaces)
# shellcheck disable=2046
google-java-format --replace $(find src -name '*.java')
