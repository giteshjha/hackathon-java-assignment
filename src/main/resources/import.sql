-- =====================================================================
-- Fulfillment Management System – seed data
-- =====================================================================

-- Stores: retail locations that receive products from warehouses.
-- Changes to stores are synchronised with the legacy system after commit.
INSERT INTO store(id, name, quantityProductsInStock) VALUES (1, 'Amsterdam Central Store', 18);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (2, 'Zwolle Retail Hub', 12);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (3, 'Tilburg Outlet', 8);
ALTER SEQUENCE store_seq RESTART WITH 4;

-- Products: stock = AVAILABLE (unallocated) units. Allocating to store/warehouse decrements this;
-- removing a mapping returns units. IDs 1-3 kept for ProductEndpointTest (TONSTAD/KALLAX/BESTÅ).
INSERT INTO product(id, name, stock) VALUES (1, 'TONSTAD', 10);
INSERT INTO product(id, name, stock) VALUES (2, 'KALLAX', 5);
INSERT INTO product(id, name, stock) VALUES (3, 'BESTÅ', 3);
-- BILLY total=120, 100 pre-allocated by seed mappings below → 20 available
INSERT INTO product(id, name, description, price, stock) VALUES (4, 'BILLY Bookcase', 'Classic adjustable bookcase, 80x28x202 cm', 59.99, 20);
-- MALM total=50, 36 pre-allocated by seed mappings below → 14 available
INSERT INTO product(id, name, description, price, stock) VALUES (5, 'MALM Bed Frame', 'Queen size bed frame with 4 storage boxes', 299.00, 14);
ALTER SEQUENCE product_seq RESTART WITH 6;

-- Warehouses: active storage facilities at predefined locations.
-- Capacity must not exceed the location maxCapacity (see LocationGateway).
-- ZWOLLE-001 maxCap=40, AMSTERDAM-001 maxCap=100, TILBURG-001 maxCap=40, HELMOND-001 maxCap=45
INSERT INTO warehouse(id, version, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
    VALUES (1, 0, 'MWH.001', 'ZWOLLE-001',     35,  5, '2024-07-01', null);
INSERT INTO warehouse(id, version, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
    VALUES (2, 0, 'MWH.012', 'AMSTERDAM-001',  80, 75, '2023-07-01', null);
INSERT INTO warehouse(id, version, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
    VALUES (3, 0, 'MWH.023', 'TILBURG-001',    30, 18, '2021-02-01', null);
-- One archived warehouse to demonstrate the warehouse lifecycle
INSERT INTO warehouse(id, version, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
    VALUES (4, 0, 'MWH.OLD', 'HELMOND-001',    40,  0, '2020-01-15', '2022-06-30');
ALTER SEQUENCE warehouse_seq RESTART WITH 5;

-- Store ↔ Product: which products are stocked in which stores (with quantity).
-- Only references products 4 and 5 so that deleting products 1-3 in tests stays safe.
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (1, 1, 4, 12);  -- Amsterdam: BILLY x12
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (2, 1, 5,  6);  -- Amsterdam: MALM  x6
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (3, 2, 4,  8);  -- Zwolle:    BILLY x8
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (4, 2, 5,  4);  -- Zwolle:    MALM  x4
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (5, 3, 4,  5);  -- Tilburg:   BILLY x5
INSERT INTO store_product(id, store_id, product_id, quantity) VALUES (6, 3, 5,  3);  -- Tilburg:   MALM  x3
ALTER SEQUENCE store_product_seq RESTART WITH 7;

-- Warehouse ↔ Product: which products are held in which warehouses (with quantity).
-- Only references products 4 and 5 for the same reason as store_product above.
INSERT INTO warehouse_product(id, warehouse_id, product_id, quantity) VALUES (1, 1, 4,  5);  -- MWH.001: BILLY x5
INSERT INTO warehouse_product(id, warehouse_id, product_id, quantity) VALUES (2, 2, 4, 60);  -- MWH.012: BILLY x60
INSERT INTO warehouse_product(id, warehouse_id, product_id, quantity) VALUES (3, 2, 5, 15);  -- MWH.012: MALM  x15
INSERT INTO warehouse_product(id, warehouse_id, product_id, quantity) VALUES (4, 3, 4, 10);  -- MWH.023: BILLY x10
INSERT INTO warehouse_product(id, warehouse_id, product_id, quantity) VALUES (5, 3, 5,  8);  -- MWH.023: MALM  x8
ALTER SEQUENCE warehouse_product_seq RESTART WITH 6;

