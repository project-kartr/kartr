#!/bin/bash

if [ ! "$HOSTNAME" = "bub" ]; then
    echo "Bitte auf bub ausführen"
    exit
fi

psql -Atc "SELECT *, pg_terminate_backend(pid)
FROM pg_stat_activity 
WHERE pid <> pg_backend_pid()
AND datname = '$USER'";