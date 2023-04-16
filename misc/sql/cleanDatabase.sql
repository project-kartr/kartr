DELETE FROM poi WHERE id NOT IN (SELECT poi_id FROM story);
DELETE FROM story WHERE content = NULL OR content = '';