#!/bin/bash

users=(kartr)

for i in "${users[@]}"; do
	pw=$(pwgen -1 24)
	/opt/wildfly/bin/add-user.sh -u "$i" -p "$pw"
	echo "machine example.org login $i password $pw" | sudo -u "$i" tee -a "/home/$i/.netrc"
done;
