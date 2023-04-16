#!/bin/bash

username_file=".username-override"
if [ -f "$username_file" ]; then
    echo "username from .username-override used"
    username="$(cat "$username_file")"
else
    username="$USER"
fi

name="$username-dev"

wildfly_address="example.org:9990"
wildfly_base_url="http://${wildfly_address}"
wildfly_webaddress="example.org:8080"

echo undeploy $name

result=$(curl -n -S -s -H "content-Type: application/json" -d '{"operation":"undeploy", "address":[{"deployment":"'"${name}.war"'"}]}' --digest "${wildfly_base_url}/management")
result=$(echo "$result" | sed -E 's#\{"outcome" : "(.*)"\}#\1#g')
if [ "$result" = "success" ]; then
  echo "success"
  echo ""
else
  echo "failed to undeploy $name"
  echo ""
fi

echo "Test undeploy with curl"
curl -s --fail "http://${wildfly_webaddress}/${name}">/dev/null
err="$?"

if [ $err != 0 ]; then
  echo "$name succesfull undeployed"
else
  echo "$name is still reachable"
fi



