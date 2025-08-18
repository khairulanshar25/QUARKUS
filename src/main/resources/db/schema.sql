-- Database schema for the Product entity
-- Note: The database inventory_db is created via Docker environment variables

-- Create sequence for products ID
CREATE SEQUENCE IF NOT EXISTS products_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY DEFAULT nextval('products_seq'::regclass),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    sku VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Insert sample data
INSERT INTO products (name, description, price, quantity, sku, category, active) VALUES
('Laptop HP Pavilion', 'High-performance laptop for work and gaming', 999.99, 50, 'HP-PAV-001', 'ELECTRONICS', true),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 200, 'MS-WRL-001', 'ELECTRONICS', true),
('Java Programming Book', 'Complete guide to Java programming', 59.99, 75, 'BK-JAVA-001', 'BOOKS', true),
('Running Shoes', 'Comfortable running shoes for daily exercise', 89.99, 120, 'SH-RUN-001', 'SPORTS', true),
('Coffee Mug', 'Ceramic coffee mug with thermal insulation', 15.99, 300, 'MG-COF-001', 'HOME_GARDEN', true)
ON CONFLICT (sku) DO NOTHING;

-- Update sequence to the maximum ID value + 50 to prevent conflicts
-- This ensures the next auto-generated ID will be higher than existing ones
-- DO $$
-- BEGIN
--     PERFORM setval('products_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM products), false);
-- END $$;
SELECT setval('products_seq', (SELECT MAX(id) FROM products) + 1);
