-- ShopSense Sample Seed Data
-- Diverse users, product catalog, historical orders, and complement mappings

-- 1. SEED USERS
INSERT INTO users (user_id, name, email, budget, preferred_categories, preferred_brands, price_sensitivity) VALUES
(1, 'Alex Chen', 'alex.chen@techmail.com', 1200.00, 'Laptops,Accessories,Audio', 'Apple,Sony,Logitech', 'PREMIUM'),
(2, 'Sarah Miller', 'sarah.m@musicfan.org', 450.00, 'Audio,Accessories,Wearables', 'Sony,Bose,Apple', 'BALANCED'),
(3, 'David Kim', 'david.kim@campus.edu', 120.00, 'Accessories,Smart Home', 'Anker,Logitech,TP-Link', 'BUDGET'),
(4, 'Emma Watson', 'emma.w@creative.io', 850.00, 'Laptops,Smart Home,Audio', 'Apple,Philips,Sony', 'BALANCED'),
(5, 'Marcus Vance', 'marcus.v@gamerhub.com', 650.00, 'Gaming,Audio,Accessories', 'Razer,Sony,Logitech', 'BALANCED');

-- 2. SEED PRODUCTS
-- Categories: Laptops, Audio, Accessories, Smart Home, Gaming, Wearables
INSERT INTO products (product_id, name, category, brand, price, rating, review_count, tags, stock_quantity, description, image_url) VALUES
-- Laptops
(101, 'MacBook Pro 16 M3', 'Laptops', 'Apple', 2499.00, 4.9, 1420, 'laptop,apple,m3,pro,creator,retina', 15, '16-inch Liquid Retina XDR display, M3 Pro chip, 18GB Unified Memory, 512GB SSD storage.', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80'),
(102, 'MacBook Air 15 M3', 'Laptops', 'Apple', 1299.00, 4.8, 2300, 'laptop,apple,m3,lightweight,battery,portable', 28, 'Ultra-thin 15.3-inch Liquid Retina display, all-day 18-hour battery, silent fanless design.', 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=500&q=80'),
(103, 'Dell XPS 15 OLED', 'Laptops', 'Dell', 1450.00, 4.7, 980, 'laptop,dell,oled,windows,developer,performance', 20, '15.6-inch 3.5K OLED touchscreen, Intel Core i7 13th Gen, 16GB RAM, 1TB NVMe SSD.', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500&q=80'),
(104, 'Lenovo IdeaPad Slim 3', 'Laptops', 'Lenovo', 399.00, 4.3, 850, 'laptop,lenovo,budget,student,office', 45, '15.6-inch FHD display, AMD Ryzen 5, 8GB RAM, 512GB SSD, durable everyday workhorse.', 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500&q=80'),

-- Audio
(201, 'Sony WH-1000XM5 Wireless Headphones', 'Audio', 'Sony', 348.00, 4.8, 3850, 'audio,sony,headphones,noise-canceling,wireless,travel', 40, 'Industry-leading noise cancellation, Auto NC Optimizer, 30-hour battery, crystal-clear hands-free calling.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80'),
(202, 'Bose QuietComfort 45 Headphones', 'Audio', 'Bose', 279.00, 4.7, 2900, 'audio,bose,headphones,noise-canceling,comfort,wireless', 35, 'Iconic quiet, lightweight comfort, high-fidelity audio, adjustable EQ, 24 hours per charge.', 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&q=80'),
(203, 'Apple AirPods Pro 2 (USB-C)', 'Audio', 'Apple', 249.00, 4.9, 5200, 'audio,apple,earbuds,noise-canceling,wireless,portable', 60, 'Active Noise Cancellation, Transparency mode, Personalized Spatial Audio, MagSafe Charging Case.', 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&q=80'),
(204, 'Sony WF-C500 True Wireless Earbuds', 'Audio', 'Sony', 68.00, 4.4, 1890, 'audio,sony,earbuds,budget,compact,sport', 70, 'Compact, lightweight in-ear headphones with DSEE, IPX4 splash resistance, 20-hour total battery.', 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80'),
(205, 'Anker Soundcore Motion+ Bluetooth Speaker', 'Audio', 'Anker', 99.99, 4.6, 2100, 'audio,anker,speaker,bluetooth,waterproof,bass', 55, 'Hi-Res 30W audio, ultra-wide frequency range, customizable EQ, IPX7 waterproof rating.', 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500&q=80'),

-- Accessories & Peripherals
(301, 'Logitech MX Master 3S Ergonomic Mouse', 'Accessories', 'Logitech', 99.99, 4.9, 4100, 'accessories,logitech,mouse,ergonomic,wireless,office,quiet', 65, '8K DPI sensor tracks on glass, Quiet Click switches, MagSpeed electromagnetic scrolling, USB-C fast charge.', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80'),
(302, 'Logitech MX Keys S Wireless Keyboard', 'Accessories', 'Logitech', 109.99, 4.8, 3100, 'accessories,logitech,keyboard,wireless,backlit,typing', 50, 'Smart illumination, low-profile spherically dished keys, multi-device cross-computer control.', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80'),
(303, 'Anker 737 GaN 120W Fast Charger', 'Accessories', 'Anker', 69.99, 4.8, 2400, 'accessories,anker,charger,gan,usb-c,fast-charge', 80, 'Power 3 devices simultaneously with 2 USB-C and 1 USB-A ports; Dynamic Power Distribution.', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&q=80'),
(304, 'Anker 8-in-1 USB-C Hub HDMI & Ethernet', 'Accessories', 'Anker', 49.99, 4.6, 3200, 'accessories,anker,hub,usb-c,hdmi,ethernet,adapter', 90, 'PowerExpand 8-in-1 with 100W Power Delivery, 4K HDMI, 1Gbps Ethernet, SD card reader, 2 USB 3.0.', 'https://images.unsplash.com/photo-1622445262464-84b14e0745b3?w=500&q=80'),
(305, 'Samsung T7 Shield 1TB Rugged SSD', 'Accessories', 'Samsung', 119.00, 4.8, 2700, 'accessories,samsung,ssd,storage,portable,backup', 45, 'Up to 1050 MB/s transfer speed, IP65 water and dust resistant, drop resistant up to 3 meters.', 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=500&q=80'),
(306, 'Tomtoc 360 Protective Laptop Sleeve', 'Accessories', 'Tomtoc', 29.99, 4.7, 4500, 'accessories,tomtoc,sleeve,case,laptop,protective', 100, 'CornerArmor patented technology, shockproof padding, water-resistant recycled fabric, YKK zippers.', 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80'),
(307, 'Ergonomic Aluminum Laptop Stand', 'Accessories', 'Nulaxy', 24.99, 4.6, 6200, 'accessories,stand,laptop,ergonomic,desk,aluminum', 120, 'Universal adjustable height and angle stand, ventilation cooling slots, rubber pads to prevent slips.', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80'),
(308, 'Braided 100W USB-C Cable (2-Pack)', 'Accessories', 'Anker', 14.99, 4.7, 7800, 'accessories,anker,cable,usb-c,fast-charge,durable', 150, 'Heavy-duty braided nylon, tested to withstand 25,000 bends, 480Mbps data transfer speed.', 'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=500&q=80'),

-- Smart Home
(401, 'Philips Hue Smart Color Bulb Starter Kit', 'Smart Home', 'Philips', 139.99, 4.6, 1750, 'smart home,philips,lighting,rgb,automation,alexa', 30, '4 White & Color Ambiance smart bulbs with Hue Bridge, 16 million colors, automated smart routines.', 'https://images.unsplash.com/photo-1550985543-f47f38aeee65?w=500&q=80'),
(402, 'Amazon Echo Show 8 Smart Display', 'Smart Home', 'Amazon', 129.99, 4.5, 3400, 'smart home,amazon,alexa,display,smart-speaker,voice', 40, '8-inch HD touch screen, spatial audio, 13MP camera with auto-framing, smart home control hub.', 'https://images.unsplash.com/photo-1543512214-318c7553f230?w=500&q=80'),
(403, 'TP-Link Kasa Smart Plug 4-Pack', 'Smart Home', 'TP-Link', 27.99, 4.6, 9200, 'smart home,tp-link,plug,smart-plug,budget,automation', 85, 'Control electronics from anywhere via smartphone app, voice control with Google Assistant & Alexa.', 'https://images.unsplash.com/photo-1558002038-1055907df827?w=500&q=80'),

-- Gaming
(501, 'Razer BlackShark V2 Pro Gaming Headset', 'Gaming', 'Razer', 179.99, 4.7, 1600, 'gaming,razer,audio,headset,esports,wireless', 35, 'TriForce Titanium 50mm Drivers, HyperClear Super Wideband Mic, ultra-soft memory foam cushions.', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=500&q=80'),
(502, 'Sony DualSense Wireless Controller', 'Gaming', 'Sony', 69.99, 4.8, 8900, 'gaming,sony,controller,ps5,haptic,pc', 75, 'Haptic feedback, dynamic adaptive triggers, built-in microphone, iconic comfortable grip.', 'https://images.unsplash.com/photo-1606318801954-d46846fe3ecd?w=500&q=80'),
(503, 'Logitech G502 HERO Gaming Mouse', 'Gaming', 'Logitech', 44.99, 4.7, 11400, 'gaming,logitech,mouse,rgb,programmable,high-dpi', 90, 'HERO 25K Sensor, 11 customizable buttons, adjustable weight system, LIGHTSYNC RGB lighting.', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80');

-- 3. SEED PURCHASE HISTORY (Orders & Items)
-- Order 1: Alex Chen bought MacBook Air + Anker USB-C Hub
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1001, 1, 1348.99, '2026-09-03 14:30:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1001, 102, 1, 1299.00),
(1001, 304, 1, 49.99);

-- Order 2: Sarah Miller bought Sony Headphones + Laptop Stand
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1002, 2, 372.99, '2026-09-14 11:15:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1002, 201, 1, 348.00),
(1002, 307, 1, 24.99);

-- Order 3: David Kim bought Anker USB-C cables + TP-Link Smart Plugs
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1003, 3, 42.98, '2026-09-20 09:45:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1003, 308, 1, 14.99),
(1003, 403, 1, 27.99);

-- Order 4: Other user purchase demonstrating item co-occurrence (MacBook Air + Sleeve + GaN Charger)
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1004, 4, 1398.98, '2026-09-10 16:20:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1004, 102, 1, 1299.00),
(1004, 303, 1, 69.99),
(1004, 306, 1, 29.99);

-- Order 5: Gamer purchase (Razer Headset + Controller + Gaming Mouse)
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1005, 5, 294.97, '2026-09-23 18:00:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1005, 501, 1, 179.99),
(1005, 502, 1, 69.99),
(1005, 503, 1, 44.99);

-- Order 6: Multi-item co-occurrence (MX Master 3S + MX Keys S keyboard)
INSERT INTO orders (order_id, user_id, total_amount, order_date, status) VALUES
(1006, 1, 209.98, '2026-08-15 12:00:00', 'COMPLETED');
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1006, 301, 1, 99.99),
(1006, 302, 1, 109.99);

-- 4. SEED ACTIVE CART ITEMS (Starting state for demo)
-- Alex Chen currently has MX Master 3S Mouse in cart
INSERT INTO cart_items (user_id, product_id, quantity) VALUES
(1, 301, 1);

-- Sarah Miller has Anker Fast Charger in cart
INSERT INTO cart_items (user_id, product_id, quantity) VALUES
(2, 303, 1);

-- 5. SEED PRODUCT COMPLEMENTS (Domain Knowledge & Affinity Links)
INSERT INTO product_complements (product_id, complement_product_id, affinity_score, reason) VALUES
-- Complements for MacBook Air (102) & MacBook Pro (101)
(102, 304, 0.95, 'Essential USB-C Hub for ports expansion'),
(102, 306, 0.92, 'Custom 360 shockproof protective sleeve'),
(102, 303, 0.88, 'Fast 120W GaN compact travel charger'),
(102, 301, 0.85, 'Ergonomic high-precision workspace mouse'),
(101, 305, 0.94, 'High-speed 1TB rugged backup drive'),
(101, 307, 0.90, 'Ergonomic aluminum cooling riser stand'),

-- Complements for MX Master 3S Mouse (301)
(301, 302, 0.96, 'Perfect companion MX Keys S keyboard'),
(301, 307, 0.82, 'Aluminum stand for ergonomic desk posture'),
(301, 308, 0.75, 'Braided 100W USB-C charging cable'),

-- Complements for Sony WH-1000XM5 (201) & Bose QC45 (202)
(201, 303, 0.85, 'Fast GaN travel charger for rapid recharge'),
(201, 305, 0.78, 'Portable lossless music library SSD'),
(202, 308, 0.80, 'Extra durable charging cable'),

-- Complements for Gaming Headset (501)
(501, 502, 0.90, 'DualSense controller for complete setup'),
(501, 503, 0.88, 'Precision 25K DPI HERO gaming mouse');
