-- ============================================================
--  GREEN AURA NURSERY - DATABASE SETUP
--  Step 1: Run this file in MySQL to create your database
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS green_aura_nursery;
USE green_aura_nursery;

-- ============================================================
-- TABLE 1: users
-- Stores customer registration details
-- OOP NOTE: This table maps directly to our User.java model class
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,  -- Unique ID for each user
    fullName VARCHAR(100) NOT NULL,           -- Customer's full name
    email    VARCHAR(100) NOT NULL UNIQUE,    -- Email (used for login, must be unique)
    password VARCHAR(255) NOT NULL,           -- Password (plain text for now; hash in real apps)
    phone    VARCHAR(20),                     -- Phone number (used on checkout page)
    address  VARCHAR(255)                     -- Delivery address (used on checkout page)
);

-- ============================================================
-- TABLE 2: plants
-- Stores the nursery's product catalogue
-- OOP NOTE: This table maps directly to our Plant.java model class
-- ============================================================
CREATE TABLE IF NOT EXISTS plants (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,         -- Plant name e.g. "Aloe Vera"
    description TEXT,                          -- Short description of the plant
    price       DECIMAL(10, 2) NOT NULL,       -- Price in GH₵
    imageUrl    VARCHAR(255)                   -- Path to plant image (optional)
);

-- ============================================================
-- TABLE 3: cart_items
-- Links a user to the plants they've added to their cart
-- OOP NOTE: Maps to CartItem.java — links User + Plant with a quantity
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    userId   INT NOT NULL,                    -- Which user added this item
    plantId  INT NOT NULL,                    -- Which plant was added
    quantity INT NOT NULL DEFAULT 1,          -- How many of this plant
    FOREIGN KEY (userId)  REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (plantId) REFERENCES plants(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 4: orders
-- Stores the final placed orders (one row per checkout)
-- OOP NOTE: Maps to Order.java model class
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    userId     INT NOT NULL,                      -- Who placed the order
    totalPrice DECIMAL(10, 2) NOT NULL,           -- Grand total in GH₵
    orderDate  DATETIME DEFAULT CURRENT_TIMESTAMP,-- When order was placed
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 5: order_items
-- Stores individual items within each order (one row per plant per order)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    orderId  INT NOT NULL,                    -- Which order this belongs to
    plantId  INT NOT NULL,                    -- Which plant was ordered
    quantity INT NOT NULL,                    -- How many
    price    DECIMAL(10, 2) NOT NULL,         -- Price at time of order
    FOREIGN KEY (orderId) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (plantId) REFERENCES plants(id)
);

-- ============================================================
-- SAMPLE DATA: Pre-load 6 plants into the catalogue
-- ============================================================
INSERT INTO plants (name, description, price, imageUrl) VALUES
('Aloe Vera',       'A succulent plant great for skin care and decoration.',  20.00, 'images/aloe.jpg'),
('Rose',            'A classic flowering plant available in many colours.',   15.00, 'images/rose.jpg'),
('Mint',            'Aromatic herb perfect for teas and cooking.',            10.00, 'images/mint.jpg'),
('Peace Lily',      'An elegant indoor plant that purifies the air.',         25.00, 'images/lily.jpg'),
('Snake Plant',     'Very low maintenance — great for beginners.',            30.00, 'images/snake.jpg'),
('Bamboo Palm',     'Tropical palm that thrives indoors with bright light.',  45.00, 'images/bamboo.jpg');

-- ============================================================
-- HOW TO RUN THIS FILE:
--   Option A: MySQL Workbench → Open & Execute
--   Option B: Command line:
--             mysql -u root -p < green_aura.sql
-- ============================================================
