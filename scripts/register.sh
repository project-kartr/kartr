#!/bin/bash
username_file=".username-override"
if [ -f "$username_file" ]; then
    echo "username from .username-override used"
    username="$(cat "$username_file")"
else
    username="$USER"
fi
name="$username-dev"

url=https://example.org/$name/public/register

if [[ -z "$1" || -z "$2" || -z "$3" ]]; then
  echo "USAGE: $0 <displayname> <email> <password>"
  exit 1
fi
curl \
  -X POST \
  -d "displayname=$1" \
  -d "email=$2" \
  -d "password=$3" \
  "$url"
echo "New user created: $1 "
