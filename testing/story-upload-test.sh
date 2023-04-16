#!/usr/bin/env bash

target=$1

source "$(dirname "$0")/_test-lib.sh"

script_time=$(date "+%F %R:%s")

# login
expect success "${BASE_URL}/auth/login" "email=demo@demo.de" "password=demo"

# poi upload
poi_id=$(expect_result .poi_id success \
  "${BASE_URL}/api/poi-upload" \
  "longitude=8.12345" \
  "latitude=52.654321" \
  "displayname=story-upload-test ${script_time}")
# check "$poi_id" || return

# file upload
file="${dirname}/test-file.png"
file_id=$(expect_result .file_id success \
  "${BASE_URL}/api/file-upload" \
  "thefile=@${file}")
# check "$file_id" || return

# story upload
story_id=$(expect_result .story_id success \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time}" \
  "content=Lorem Ipsum abc" \
  "poi_id=${poi_id}" \
  "files=${file_id}")
# check "$story_id" || return

# story modification
old_story_id=$story_id
story_id=$(expect_result .story_id success \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time} Changed" \
  "content=Changes und so" \
  "poi_id=${poi_id}" \
  "story_id=${story_id}" \
  "files=${file_id}")
# check "$story_id" || return

if [[ "$old_story_id" != "$story_id" ]]; then
  echo "updating story resulted in new story id $old_story_id -> $story_id" >&2
fi

# empty story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=" \
  "content=" \
  "poi_id=" \
  "files="

# empty headline story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=" \
  "content=Lorem Ipsum abc" \
  "poi_id=${poi_id}" \
  "files=${file_id}"

# empty content story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time}" \
  "content=" \
  "poi_id=${poi_id}" \
  "files=${file_id}"

# empty poi_id story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time}" \
  "content=Lorem Ipsum" \
  "poi_id=" \
  "files=${file_id}"

# invalid files story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time}" \
  "content=Lorem Ipsum" \
  "poi_id=${poi_id}" \
  "files=../../../../shadow"

# non-number poi_id story upload
expect failed \
  "${BASE_URL}/api/story-upload" \
  "headline=SkriptTest ${script_time}" \
  "content=Lorem Ipsum" \
  "poi_id=hallo" \
  "files=${file_id}"

# logout
expect success "${BASE_URL}/api/logout"
