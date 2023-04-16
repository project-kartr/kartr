#!/bin/bash
#1 - Delete empty pois and stories
psql -d "$USER" -t -c "DELETE FROM poi WHERE id NOT IN (SELECT poi_id FROM story);
                     DELETE FROM story WHERE content = NULL OR content = '';
                    "

#2- Delete files in the bub/data subdirectories if the file name is not included in the database
data_dir="/opt/bub-data/$USER"
files=$(find "$data_dir" -type f)
for file in $files; do
    filename=$(basename "$file")    
    query=$(psql -d "$USER" -t -c "SELECT count(*) FROM file WHERE filename = '$filename'")
    if [[ $query -eq 0 ]]; then
        sudo -u wildfly rm -v "$file"
    fi
done
