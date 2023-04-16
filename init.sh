#!/usr/bin/env bash

username_file=".username-override"
if [ -f "$username_file" ]; then
    echo "username from .username-override used"
    username="$(cat "$username_file")"
else
    username="$USER"
fi

files_dir="./misc"
# shellcheck disable=2029 # we want files_dir to expand on the client side
if [[ "$(hostname)" != bub ]]; then
  set -e

  files_dir="/tmp/${username}"
  ssh bub "mkdir -p '${files_dir}'"
  scp -r misc/sql "bub:${files_dir}/"
  ssh bub "bash -s '${files_dir}' '$username'" < <(tail -n "+$(($(grep -n '#### SCRIPT_GUARD ####' < "$0" | cut -d: -f1 | tail -n1)+1))" < "$0")
  exit
fi
#### SCRIPT_GUARD ####
[[ -z "$files_dir" ]] && files_dir="$1"
[[ -z "$username" ]] && username="$2"
echo "Executing as ${USER} on $(hostname)"

set -e

psql < "${files_dir}/sql/create_db_tables.sql"
psql < "${files_dir}/sql/insert_test_data.sql"

# update next generated primary key
# see https://stackoverflow.com/questions/4448340/postgresql-duplicate-key-violates-unique-constraint#21639138
for i in account story poi; do
  psql <<<"SELECT setval(pg_get_serial_sequence('$i', 'id'), (SELECT max($i.id) FROM $i)+1);"
done

bub_data="/opt/bub-data/${username}"

if [[ -t 0 ]]; then
  sudo mkdir -p "$bub_data"
  sudo cp -t "${bub_data}" "${files_dir}"/sql/test-files/*
  sudo chown -R wildfly: "$bub_data"
else
  echo ">>> please run the following commands: "
  echo "sudo mkdir -p '$bub_data'"
  echo "sudo cp -t '${bub_data}' '${files_dir}'/sql/test-files/*"
  echo "sudo chown -R wildfly: '$bub_data'"
fi
