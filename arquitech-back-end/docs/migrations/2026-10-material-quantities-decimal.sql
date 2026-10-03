-- ArquiTech / MySQL 8
-- Non-destructive conversion from integer inventory quantities to DECIMAL(19,4).
-- Take a database backup and run this during a maintenance window before
-- deploying the BigDecimal version when DDL_AUTO is validate/none.
-- It is safe to run again: the normalization and column definitions are idempotent.
-- Any value that would overflow or lose fractional precision aborts before DDL.

SELECT id, quantity, stock, minimum_stock, unit_price
FROM materials
WHERE quantity < 0 OR stock < 0 OR minimum_stock < 0 OR unit_price < 0
   OR ABS(quantity) > 999999999999999.9999
   OR ABS(stock) > 999999999999999.9999
   OR ABS(minimum_stock) > 999999999999999.9999
   OR ABS(unit_price) > 99999999999999999.99
   OR quantity <> ROUND(quantity, 4)
   OR stock <> ROUND(stock, 4)
   OR minimum_stock <> ROUND(minimum_stock, 4)
   OR unit_price <> ROUND(unit_price, 2);

SELECT id, quantity
FROM material_movements
WHERE quantity < 0
   OR ABS(quantity) > 999999999999999.9999
   OR quantity <> ROUND(quantity, 4);

DROP PROCEDURE IF EXISTS migrate_material_quantities_decimal;
DELIMITER //
CREATE PROCEDURE migrate_material_quantities_decimal()
BEGIN
    DECLARE incompatible_rows BIGINT DEFAULT 0;

    SELECT
        (SELECT COUNT(*)
           FROM materials
          WHERE quantity < 0 OR stock < 0 OR minimum_stock < 0 OR unit_price < 0
             OR ABS(quantity) > 999999999999999.9999
             OR ABS(stock) > 999999999999999.9999
             OR ABS(minimum_stock) > 999999999999999.9999
             OR ABS(unit_price) > 99999999999999999.99
             OR quantity <> ROUND(quantity, 4)
             OR stock <> ROUND(stock, 4)
             OR minimum_stock <> ROUND(minimum_stock, 4)
             OR unit_price <> ROUND(unit_price, 2))
        +
        (SELECT COUNT(*)
           FROM material_movements
          WHERE quantity < 0
             OR ABS(quantity) > 999999999999999.9999
             OR quantity <> ROUND(quantity, 4))
      INTO incompatible_rows;

    IF incompatible_rows > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Material migration aborted: review negative, overflow, or excess-scale values';
    END IF;

    UPDATE materials
       SET quantity = COALESCE(quantity, 0),
           stock = COALESCE(stock, GREATEST(0, COALESCE(quantity, 0) - COALESCE(quantity_exit, 0))),
           minimum_stock = COALESCE(minimum_stock, 0),
           unit_price = COALESCE(unit_price, 0);

    UPDATE material_movements
       SET quantity = COALESCE(quantity, 0);

    ALTER TABLE materials
        MODIFY COLUMN quantity DECIMAL(19,4) NOT NULL,
        MODIFY COLUMN stock DECIMAL(19,4) NOT NULL,
        MODIFY COLUMN minimum_stock DECIMAL(19,4) NOT NULL,
        MODIFY COLUMN unit_price DECIMAL(19,2) NOT NULL;

    ALTER TABLE material_movements
        MODIFY COLUMN quantity DECIMAL(19,4) NOT NULL;
END//
DELIMITER ;

CALL migrate_material_quantities_decimal();
DROP PROCEDURE migrate_material_quantities_decimal;
