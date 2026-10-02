-- ArquiTech / MySQL 8
-- Non-destructive conversion from integer inventory quantities to DECIMAL(19,4).
-- Take a database backup and run this once during a maintenance window before
-- deploying the BigDecimal version when DDL_AUTO is validate/none.

UPDATE materials
SET quantity = COALESCE(quantity, 0),
    stock = COALESCE(stock, GREATEST(0, COALESCE(quantity, 0) - COALESCE(quantity_exit, 0))),
    minimum_stock = COALESCE(minimum_stock, 0);

UPDATE material_movements
SET quantity = COALESCE(quantity, 0);

ALTER TABLE materials
    MODIFY COLUMN quantity DECIMAL(19,4) NOT NULL,
    MODIFY COLUMN stock DECIMAL(19,4) NOT NULL,
    MODIFY COLUMN minimum_stock DECIMAL(19,4) NOT NULL,
    MODIFY COLUMN unit_price DECIMAL(19,2) NOT NULL;

ALTER TABLE material_movements
    MODIFY COLUMN quantity DECIMAL(19,4) NOT NULL;
