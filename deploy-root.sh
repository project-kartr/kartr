#!/usr/bin/env bash

name=ROOT
username=kartr
base_domain=example.org

#check if .netrc exists and has $base_domain inside
if [[ "$OSTYPE" == "darwin"* ]]; then
  netrcPath="/Users/$USER/.netrc"
elif [ "$OSTYPE" == "linux-gnu" ]; then
  netrcPath="/home/$USER/.netrc"
fi

if [ ! -f "$netrcPath" ]; then
  echo "No .netrc found in $netrcPath"
  exit
fi

if ! grep -q "$base_domain" "$netrcPath"; then
  echo "$base_domain is not inside of $netrcPath"
  exit
fi 

echo Compile
rm -rf build/*
mkdir -p build
cp -r app/* build

sed -i "s#__DEPLOY_PATH__#/#" "$(find build -name manifest.json)"

export CLASSPATH=.:$(find complibs -name '*jar'|tr '\n' ':'):$(find app/WEB-INF/lib/ -name '*jar'|tr '\n' ':')
javafiles=$(find src -name '*java')
javac -d build/WEB-INF/classes $javafiles
err="$?"
if [ $err != 0 ]; then
  echo "compilation failed"
  exit
else
  echo "success"
  echo ""
fi
CLASSPATH+=build/WEB-INF/classes

# database settings
echo "datasource=java:jboss/datasources/$username" > build/WEB-INF/classes/kartr/services/connection.properties

# storage path settings
echo "upload_base_dir=/opt/bub-data/$username/" > build/WEB-INF/classes/kartr/services/filestorage.properties

# mail settings
SMTP_HOST=127.0.0.1
SMTP_PORT=8101
SMTP_USER=demo
SMTP_PASSWORD=demo
tee build/WEB-INF/classes/kartr/services/mail.properties <<EOF >/dev/null
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=$SMTP_HOST
mail.smtp.port=$SMTP_PORT
mail.user=$SMTP_USER
mail.password=$SMTP_PASSWORD
EOF

echo Build the war-file
jar -cf "$name.war" -C build .

if [ ! -f "$name.war" ]; then
  echo "$name.war does not exist"
  exit
else
  echo "success: deploy as $name"
  echo ""
fi


wildfly_webaddress="$base_domain"
wildfly_address="$base_domain"
wildfly_base_url="https://${wildfly_address}"

start=$(date +%s%N)
echo "Undeploy old war"
result=$(curl -n -S -s -H "content-type: application/json" -d '{"operation":"undeploy", "address":[{"deployment":"'"${name}.war"'"}]}' --digest "${wildfly_base_url}/management")
result=$(echo "$result" | sed -E 's#\{"outcome" : "(.*)"\}#\1#g')
if [ "$result" = "success" ]; then
  echo "success"
  echo ""
else
  echo "failed: happens when nothing is to undeploy under $name"
  echo ""
fi


echo "Remove old war"
result=$(curl -n -S -s -H "content-type: application/json" -d '{"operation":"remove", "address":[{"deployment":"'"${name}.war"'"}]}' --digest "${wildfly_base_url}/management")
result=$(echo "$result" | sed -E 's#\{"outcome" : "(.*)"\}#\1#g')
if [ "$result" = "success" ]; then
  echo "success"
  echo ""
else
  echo "failed: happens, when nothing is to remove unter $name"
  echo ""
fi


echo "Upload new war"
bytes_value=$(curl -n -s --http1.1 -F "file=@${name}.war" --digest "${wildfly_base_url}/management/add-content")
result=$(echo "$bytes_value" | sed -E 's#.*outcome":"([[:alpha:]]*)".*#\1#g')
if [ "$result" = "success" ]; then
  echo "success"
  echo ""
else
  echo "failed"
  echo "$result"
  exit
fi

bytes_value=$(echo "$bytes_value" | sed -E 's#.*BYTES_VALUE":"(.*)"\}\}#\1#g')
json_string='{"content":[{"hash": {"BYTES_VALUE" : "'"${bytes_value}"'"}}], "address": [{"deployment":"'"${name}.war"'"}], "operation":"add", "enabled":"true"}'


echo "Deploy new war"
result=$(curl -n -S -s -H "content-type: application/json" -d "$json_string" --digest "${wildfly_base_url}/management")
result=$(echo "$result" | sed -E 's#\{"outcome" : "(.*)"\}#\1#g')
if [ "$result" = "success" ]; then
  echo "success"
  echo ""
else
  echo "failed"
  exit
fi
end=$(date +%s%N)


echo "Test deployment with Curl"
curl -s "http://${wildfly_webaddress}/${name}/hello">/dev/null
err="$?"
if [ $err != 0 ]; then
  echo "curl failed after $(((end-start)/1000000))ms"
  exit
else
  echo "success: $name was undeployed, removed, uploaded and deployed in $(((end-start)/1000000))ms"
fi
