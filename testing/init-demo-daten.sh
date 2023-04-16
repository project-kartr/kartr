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

echo ">>>LOGIN<<<"
curl -sS -b jar -c jar "${BASE_URL}/auth/login" \
    -F 'email=demo@demo.de' \
    -F 'password=demo'

echo "==="

echo ">>>File Upload<<<"
file="$(dirname "$0")/test-file.png"
res=$(curl -sS -b jar -c jar  "${BASE_URL}/api/file-upload" \
    -F "thefile=@${file}")

echo "$res"
file_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.file_id')
echo "==="

echo ">>>POI Upload<<<"
for i in {1..10}; do
    res=$(curl -sS -b jar -c jar "${BASE_URL}/api/poi-upload" \
        -F "longitude=8.583${i}45" \
        -F "latitude=53.539${i}51" \
        -F "displayname=Name-$i-$(date +%F-%R)")

    poi_id=$(echo "$res" | grep -oE '\{.*\}' | jq -r '.poi_id')
    echo "$poi_id"

    for x in {1..5}; do
        curl -sS -b jar -c jar "${BASE_URL}/api/story-upload" \
            -F "headline=Headline $x und so" \
            -F "content=Ganz viel Content und so" \
            -F "poi_id=${poi_id}" \
            -F "files=${file_id}"
    done
    echo "==="

done

echo "==="
echo ""
echo ">>>expect: success<<<"
curl -sS -b jar -c jar -X POST "${BASE_URL}/api/logout"
