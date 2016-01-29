SELECT 
	pg_size_pretty(sum(pg_table_size(table_name))) as table_size,
	pg_size_pretty(sum(pg_indexes_size(table_name))) as indexes_size,
	pg_size_pretty(sum(pg_total_relation_size(table_name)))
FROM (
        SELECT ('"' || table_schema || '"."' || table_name || '"') AS table_name
        FROM information_schema.tables
    ) AS all_tables
