#!/usr/bin/env bash
# shellcheck disable=2034 # allow unused

# USAGE:
# functions:
#   expect_result <key> <status> <url> [<param>...]
#     => Behaviour: cURL POST to <url>,
#           stdout: echo <key> from response
#           stderr: colored output if <status> matches response status,
#             file: logs <url> and response to ${tmp_out}
#     => Arguments:
#           <key>    = jq filter (e.g. '.story_id')
#           <status> = string to expect in response status (e.g. 'success')
#           <url>    = url string to curl
#           <param>  = key-value string for curl's '-F' flag
#     => Example:
#           expect_result .file_id success .../file-upload thefile=@file
#
#   expect <status> <url> [<param>...]
#     => Behaviour: like expect_result without echoed key
#     => Example:
#           expect success .../login email=a@b.com password=blub
#
#   do_post <url> [<param>...]
#     => Behaviour: cURL POST to <url> using cookie jar
#           stdout: echo response and headers (curl -i)
#             file: logs <url> and response to ${tmp_out}
#     => Arguments:
#           <url>    = url string to curl
#           <param>  = key-value string for curl's '-F' flag
#
#   do_get <url> [<curl_param>...]
#     => Behaviour: cURL GET to <url> using cookie jar
#           stdout: echo response and headers (curl -i)
#             file: logs <url> and response to ${tmp_out}
#     => Arguments:
#           <url>         = url string to curl
#           <curl_param>  = normal flags for curl
#
#   get_value <filter>
#     => Behaviour: delete newline | grep -o json-object | jq $1
#           stdout: result from jq
#            stdin: data to filter
#     => Arguments:
#           <filter> = jq filter
#
#   check <param>...
#     => Behaviour: checks all params to be non-empty and != "null"
#           return: 0 if all params are ok
#                   1 if any param is empty or == "null"
#     => Arguments:
#           <param> = string
#     => Example:
#           check "$story_id" "$poi_id"
# variables:
#   basename: basename of $0
#    dirname: dirname of $0
#   username: username from override file or $USER
#       name: deployment name (e.g. "jpeters-dev") (except see special behaviour)
#   BASE_URL: e.g. https://wilab08.../$name
#    tmp_out: file to which the logs will be written
#      clear: shell escape code to reset shell colors (\e[0m)
#        red: shell escape code for red
#      green: shell escape code for green
# 
# special behaviour:
#     if $target is set to "kartr", $name will be empty
#     if $target is set to something else, $name will be set to
#         that instead of username-dev

basename=$(basename "$0")
dirname=$(dirname "$0")

# e.g. target=kartr
if [[ -z "$target" ]]; then
  username_file=".username-override"
  if [ -f "$username_file" ]; then
    echo "username from .username-override used" >&2
    username="$(cat "$username_file")"
  else
    username="$USER"
  fi
  name="$username-dev"
else
  [[ "$target" != kartr ]] && name=$target
fi

if [[ -z "$name" ]]; then
  BASE_URL="https://example.org"
else
  BASE_URL="https://example.org/${name}"
fi

tmp_out="${basename%.sh}.out"
echo "writing all curl responses to ${tmp_out}" >&2

[[ -f "${tmp_out}" ]] && rm "${tmp_out}"

echo >&2

# shellcheck disable=2016
{
  awk_tail='{ if (x != 0) { print $0 } } $0 == "\r" { x = 1 }'
  # matching on every line: check if x != 0 and then print full line.
  # matching only on lines == "\r": set x = 1
  awk_head='{ if (x == 0 && $0 != "\r") { print $0 } } $0 == "\r" { x = 1 }'
}

# colors
clear='\e[0m'
red='\e[1;31m'
#red='\e[31m'
green='\e[1;32m'
#green='\e[32m'

do_post() {
  local url=$1
  shift

  local -a args
  for i in "$@"; do
    args+=('-F' "$i")
  done

  echo "$url ${args[*]}" >>"$tmp_out"
  curl -sS -X POST -b jar -c jar -i "$url" "${args[@]}" | tee -a "$tmp_out"
  echo >>"$tmp_out"
}

do_get() {
  local url=$1
  shift

  echo "$url $*" >>"${tmp_out}"
  curl -sS -X GET -b jar -c jar -i "$url" "$@" | tee -a "$tmp_out"
  echo >>"$tmp_out"
}

get_value() {
  tr -d '\n' | grep -oE '\{.*\}' | jq -r "$1"
}

get_status() {
  get_value ".status"
}

# shellcheck disable=2155
expect() {
  # expect <success|failed> <url> [<params>...]
  expect_result "" "$@" >/dev/null
}

# shellcheck disable=2155
expect_result() {
  # expect_result <key> <success|failed> <url> [<params>...]
  local key=$1; shift
  local expected=$1; shift
  local res=$(do_post "$@")
  local header=$(echo "$res" | awk "$awk_head")
  local content=$(echo "$res" | awk "$awk_tail")

  local status=$(echo "$content" | get_status)
  local http_status=$(head -n1 <<< "$header" | cut -d' ' -f2)
  if [[ -n "$key" ]]; then
    local value=$(get_value "$key" <<<"$content")
  fi


  if [[ "$status" == "$expected" ]]; then
    if [[ -n "$key" ]]; then
      echo -e "${1#"${BASE_URL}"} ($http_status): ${green}$status${clear} -> $key = $value" >&2
    else
      echo -e "${1#"${BASE_URL}"} ($http_status): ${green}$status${clear}" >&2
    fi
  else
    echo -e "${1#"${BASE_URL}"} ($http_status): ${red}$status${clear}" >&2
  fi

  echo "$value"
}

check() {
  for i in "$@"; do
    [[ -n "$1" && "$1" != null ]] || return 1
  done
}
