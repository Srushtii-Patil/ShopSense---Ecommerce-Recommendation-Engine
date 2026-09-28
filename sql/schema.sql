-- ShopSense Database Schema
-- Compatible with MySQL 8.x / 9.x and ANSI SQL / H2 MySQL Mode

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    budget DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    preferred_categories VARCHAR(255) DEFAULT '',
    preferred_brands VARCHAR(255) DEFAULT '',
    price_sensitivity VARCHAR(20) DEFAULT 'BALANCED', -- 'BUDGET', 'BALANCED', 'PREMIUM'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    rating DECIMAL(3,1) NOT NULL DEFAULT 4.0,
    review_count INT NOT NULL DEFAULT 0,
    tags VARCHAR(255) DEFAULT '',
    stock_quantity INT NOT NULL DEFAULT 50,
    description TEXT,
    image_url VARCHAR(255) DEFAULT ''
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'COMPLETED',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_complements (
    complement_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    complement_product_id INT NOT NULL,
    affinity_score DECIMAL(4,2) DEFAULT 1.00,
    reason VARCHAR(100) DEFAULT 'Frequently bought together',
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (complement_product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
