#!/bin/bash

users=(kartr)

for i in "${users[@]}"; do
	pw="$(grep ":$i:" "/home/$i/.pgpass" | sed -E 's#([^:]*:){4}(.*)#\2#g')"
	sudo /opt/wildfly/bin/jboss-cli.sh -c "data-source add --name=$i --jndi-name=java:jboss/datasources/$i --driver-name=postgresql-42.4.0.jar --connection-url=jdbc:postgresql://localhost:5432/$i --user-name=$i --password=$pw"
done;
