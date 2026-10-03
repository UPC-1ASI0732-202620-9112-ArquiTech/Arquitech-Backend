-- ArquiTech / MySQL 8+
-- Replaces a global unique index on license_plate with project-scoped uniqueness.
-- This migration does not delete or rewrite machinery rows.
-- Back up the database and review the duplicate preflight query before execution.

SELECT project_id, license_plate, COUNT(*) AS duplicate_count
FROM machineries
GROUP BY project_id, license_plate
HAVING COUNT(*) > 1;

DROP PROCEDURE IF EXISTS migrate_machinery_serial_scope;
DELIMITER //
CREATE PROCEDURE migrate_machinery_serial_scope()
BEGIN
    DECLARE old_index VARCHAR(64) DEFAULT NULL;
    DECLARE composite_exists INT DEFAULT 0;
    DECLARE duplicate_pairs BIGINT DEFAULT 0;

    SELECT COUNT(*)
      INTO duplicate_pairs
      FROM (
          SELECT project_id, license_plate
            FROM machineries
           GROUP BY project_id, license_plate
          HAVING COUNT(*) > 1
      ) AS duplicated_project_serials;

    IF duplicate_pairs > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Machinery migration aborted: resolve duplicate project/serial pairs first';
    END IF;

    -- Remove any single-column UNIQUE index left by the former unique=true mapping.
    SELECT MIN(single_column_indexes.index_name)
      INTO old_index
      FROM (
          SELECT index_name
            FROM information_schema.statistics
           WHERE table_schema = DATABASE()
             AND table_name = 'machineries'
             AND non_unique = 0
           GROUP BY index_name
          HAVING COUNT(*) = 1
             AND MAX(column_name) = 'license_plate'
      ) AS single_column_indexes;

    WHILE old_index IS NOT NULL DO
        SET @drop_index_sql = CONCAT(
                'ALTER TABLE machineries DROP INDEX `',
                REPLACE(old_index, '`', '``'),
                '`');
        PREPARE drop_index_statement FROM @drop_index_sql;
        EXECUTE drop_index_statement;
        DEALLOCATE PREPARE drop_index_statement;

        SET old_index = NULL;
        SELECT MIN(single_column_indexes.index_name)
          INTO old_index
          FROM (
              SELECT index_name
                FROM information_schema.statistics
               WHERE table_schema = DATABASE()
                 AND table_name = 'machineries'
                 AND non_unique = 0
               GROUP BY index_name
              HAVING COUNT(*) = 1
                 AND MAX(column_name) = 'license_plate'
          ) AS single_column_indexes;
    END WHILE;

    SELECT COUNT(*)
      INTO composite_exists
      FROM (
          SELECT index_name
            FROM information_schema.statistics
           WHERE table_schema = DATABASE()
             AND table_name = 'machineries'
             AND non_unique = 0
           GROUP BY index_name
          HAVING COUNT(*) = 2
             AND SUM(column_name = 'project_id') = 1
             AND SUM(column_name = 'license_plate') = 1
      ) AS composite_indexes;

    IF composite_exists = 0 THEN
        ALTER TABLE machineries
            ADD CONSTRAINT uk_machineries_project_serial
            UNIQUE (project_id, license_plate);
    END IF;
END//
DELIMITER ;

CALL migrate_machinery_serial_scope();
DROP PROCEDURE migrate_machinery_serial_scope;
