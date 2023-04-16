#!/usr/bin/env bash

username_file=".username-override"
if [ -f "$username_file" ]; then
    echo "username from .username-override used"
    username="$(cat "$username_file")"
else
    username="$USER"
fi
name="$username-dev"

BASE_URL="https://example.org/${name}"

echo ">>>expect: failed<<<"
curl -sS -b jar -c jar -X POST -i "${BASE_URL}/api/logout"

echo
echo "==="

echo ">>>expect: success<<<"
curl -sS -b jar -c jar -i "${BASE_URL}/auth/login" \
  -F 'email=demo@demo.de' \
  -F 'password=demo'

echo "==="

# POI upload: try too big of a number
echo ">>>expect: failed<<<"
res=$(curl -sS -b jar -c jar -i "${BASE_URL}/api/poi-upload" \
  -F "longitude=8000.12345" \
  -F "latitude=52.654321" \
  -F "displayname=Skript-Test-BANANE2")

echo "$res"

poi_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.poi_id')

echo "==="

echo ">>>expect: success<<<"
res=$(curl -sS -b jar -c jar -i "${BASE_URL}/api/poi-upload" \
  -F "longitude=8.12345" \
  -F "latitude=52.654321" \
  -F "displayname=Skript-Test-BANANE2-$(date +%F-%R)")

echo "$res"

poi_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.poi_id')

echo "==="

file="$(dirname "$0")/test-file.png"

echo ">>>expect: success<<<"
res=$(curl -sS -b jar -c jar -i "${BASE_URL}/api/file-upload" \
  -F "thefile=@${file}")

echo "$res"

file_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.file_id')

echo "==="

echo ">>>expect: success<<<"
res=$(curl -sS -b jar -c jar -i "${BASE_URL}/api/story-upload" \
  -F "headline=SkriptTest" \
  -F "content=Hoppala" \
  -F "poi_id=${poi_id}" \
  -F "files=${file_id}")

echo "$res"

story_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.story_id')

echo "==="

echo ">>>expect: success<<<"
curl -sS -b jar -c jar -i "${BASE_URL}/api/story-upload" \
  -F "headline=SkriptTest 2" \
  -F "content=Changes und so" \
  -F "poi_id=${poi_id}" \
  -F "story_id=${story_id}" \
  -F "files=${file_id}"

echo "==="

echo ">>>expect: success<<<"
curl -sS -b jar -c jar -i "${BASE_URL}/api/poi-upload" \
  -F "longitude=8.12345" \
  -F "latitude=52.654321" \
  -F "displayname=Skript-Test-changed-$(date +%F-%R)" \
  -F "poi_id=${poi_id}"

echo "==="

echo ">>>expect: success<<<"
curl -sS -b jar -c jar -X POST -i "${BASE_URL}/api/logout"
