-- Active: 1778237870132@@127.0.0.1@33061@shop
USE shop;

DROP TABLE if exists order_items;

DROP TABLE if exists orders;

DROP TABLE if exists cart_items;

DROP TABLE if exists products;

DROP TABLE if exists carts;

create table if not exists products (
    id bigint auto_increment primary key,
    title varchar(100) not null,
    price bigint not null,
    description mediumtext null,
    available_quantity int not null,
    category varchar(100) null,
    status varchar(50) null,
    image_link text null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null on update current_timestamp()
);

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Sony WH-1000XM5 Wireless Headphones',
        1,
        'Industry-leading noise canceling headphones with exceptional sound quality.',
        99999999,
        'Electronics',
        'POPULAR',
        'https://example.com/images/sony-wh1000xm5.jpg',
        '2026-05-06 03:44:29',
        '2026-05-06 09:09:03'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        2,
        'Men\'s Classic Cotton Crewneck T-Shirt',
        1,
        '100% organic cotton everyday basic t-shirt in navy blue.',
        99999999,
        'Apparel',
        'POPULAR',
        'https://example.com/images/mens-navy-tee.jpg',
        '2026-05-06 03:44:29',
        '2026-05-08 01:37:53'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        3,
        'Bonavita Connoisseur 8-Cup Coffee Maker',
        1,
        'One-touch pour-over style coffee maker with thermal carafe.',
        99999999,
        'Home & Kitchen',
        'NEW',
        'https://example.com/images/bonavita-coffee.jpg',
        '2026-05-06 03:44:29',
        '2026-05-06 07:31:43'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        4,
        'The Pragmatic Programmer: 20th Anniversary',
        1,
        'A definitive guide to software development and best practices.',
        99999999,
        'Books',
        'POPULAR',
        'https://example.com/images/pragmatic-programmer.jpg',
        '2026-05-06 03:44:29',
        '2026-05-08 01:37:53'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        5,
        'Minimalist Leather Bifold Wallet',
        1,
        null,
        99999999,
        'Accessories',
        'POPULAR',
        null,
        '2026-05-06 03:44:29',
        '2026-05-08 01:37:53'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        6,
        'Apple iPad Air (5th Generation)',
        599,
        '10.9-inch Liquid Retina display, Apple M1 chip, 64GB storage, Wi-Fi.',
        450,
        'Electronics',
        'POPULAR',
        'https://example.com/images/ipad-air-5.jpg',
        '2026-05-22 08:15:00',
        '2026-05-22 08:15:00'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        7,
        'Lodge Cast Iron Skillet, 10.25-inch',
        29,
        'Pre-seasoned cast iron skillet for everyday cooking on the stove, in the oven, or over a campfire.',
        1200,
        'Home & Kitchen',
        'POPULAR',
        'https://example.com/images/lodge-skillet.jpg',
        '2026-05-22 08:20:15',
        '2026-05-22 08:20:15'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        8,
        'Women''s High-Waist Yoga Leggings',
        45,
        'Breathable, moisture-wicking fabric with a four-way stretch and hidden pocket.',
        850,
        'Apparel',
        'NEW',
        'https://example.com/images/womens-yoga-leggings.jpg',
        '2026-05-22 09:05:33',
        '2026-05-22 09:05:33'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        9,
        'Hydro Flask Standard Mouth Water Bottle',
        34,
        '21 oz double-wall vacuum insulated stainless steel water bottle.',
        2500,
        'Sports & Outdoors',
        'POPULAR',
        'https://example.com/images/hydroflask-21oz.jpg',
        '2026-05-22 10:12:45',
        '2026-05-22 10:12:45'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        10,
        'Atomic Habits by James Clear',
        16,
        'An easy and proven way to build good habits and break bad ones.',
        300,
        'Books',
        'POPULAR',
        'https://example.com/images/atomic-habits.jpg',
        '2026-05-22 11:30:00',
        '2026-05-22 11:30:00'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        11,
        'Anker USB-C Power Bank, 10000mAh',
        39,
        null,
        1500,
        'Electronics',
        'NEW',
        null,
        '2026-05-22 12:45:10',
        '2026-05-22 12:45:10'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        12,
        'Nintendo Switch OLED Model',
        349,
        '7-inch OLED screen, 64GB internal storage, and enhanced audio in handheld and tabletop modes.',
        720,
        'Electronics',
        'POPULAR',
        'https://example.com/images/switch-oled.jpg',
        '2026-05-22 13:10:05',
        '2026-05-22 13:10:05'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        13,
        'Catan Board Game',
        44,
        'A strategy game where players collect resources and use them to build roads, settlements, and cities.',
        350,
        'Toys & Games',
        'POPULAR',
        'https://example.com/images/catan-board-game.jpg',
        '2026-05-22 13:45:22',
        '2026-05-22 13:45:22'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        14,
        'Samsonite Winfield 2 Hardside Luggage',
        159,
        '20-inch carry-on spinner suitcase made from 100% polycarbonate with brushed pattern.',
        125,
        'Travel',
        'NEW',
        'https://example.com/images/samsonite-winfield2.jpg',
        '2026-05-22 14:02:18',
        '2026-05-22 14:02:18'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        15,
        'Ray-Ban Classic Aviator Sunglasses',
        150,
        null,
        410,
        'Accessories',
        'POPULAR',
        null,
        '2026-05-22 14:30:55',
        '2026-05-22 14:30:55'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        16,
        'Logitech MX Master 3S Wireless Mouse',
        99,
        'Ergonomic performance mouse with 8K DPI track-on-glass sensor and quiet clicks.',
        890,
        'Electronics',
        'POPULAR',
        'https://example.com/images/logitech-mx3s.jpg',
        '2026-05-22 15:15:40',
        '2026-05-22 15:15:40'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        17,
        'Philips Sonicare ProtectiveClean 4100',
        79,
        'Rechargeable electric toothbrush with pressure sensor to protect teeth and gums.',
        600,
        'Personal Care',
        'NEW',
        'https://example.com/images/sonicare-4100.jpg',
        '2026-05-22 16:05:12',
        '2026-05-22 16:05:12'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        18,
        'Purina Pro Plan Adult Dog Food, 30 lb',
        55,
        'High-protein dry dog food featuring real chicken as the first ingredient.',
        300,
        'Pet Supplies',
        'POPULAR',
        'https://example.com/images/purina-pro-plan-30lb.jpg',
        '2026-05-22 16:15:00',
        '2026-05-22 16:15:00'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        19,
        'DeWalt 20V MAX Cordless Drill Combo Kit',
        199,
        'Compact drill/driver and impact driver combo kit with 2 batteries and charger.',
        150,
        'Tools & Home Improvement',
        'POPULAR',
        'https://example.com/images/dewalt-20v-combo.jpg',
        '2026-05-22 16:22:30',
        '2026-05-22 16:22:30'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        20,
        'Moleskine Classic Notebook, Hard Cover',
        22,
        'Large 5x8.25 inch notebook with ruled pages, elastic closure, and bookmark ribbon.',
        1000,
        'Office Supplies',
        'NEW',
        'https://example.com/images/moleskine-classic.jpg',
        '2026-05-22 16:28:45',
        '2026-05-22 16:28:45'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        21,
        'Lavazza Super Crema Whole Bean Coffee',
        24,
        '2.2 lb bag of mild and creamy medium espresso roast with notes of hazelnut and brown sugar.',
        650,
        'Grocery',
        'POPULAR',
        'https://example.com/images/lavazza-super-crema.jpg',
        '2026-05-22 16:35:10',
        '2026-05-22 16:35:10'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        22,
        'Dyson V15 Detect Cordless Vacuum',
        749,
        'Powerful, intelligent cordless vacuum with laser illumination to reveal microscopic dust.',
        80,
        'Home & Kitchen',
        'NEW',
        'https://example.com/images/dyson-v15.jpg',
        '2026-05-22 16:40:05',
        '2026-05-22 16:40:05'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        23,
        'Patagonia Men\'s Better Sweater Fleece Jacket',
        149,
        null,
        220,
        'Apparel',
        'POPULAR',
        null,
        '2026-05-22 16:42:20',
        '2026-05-22 16:42:20'
    );    

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        24,
        'CeraVe Hydrating Facial Cleanser',
        15,
        'Daily face wash for normal to dry skin formulated with hyaluronic acid and ceramides.',
        1200,
        'Beauty & Personal Care',
        'POPULAR',
        'https://example.com/images/cerave-cleanser.jpg',
        '2026-05-22 16:45:00',
        '2026-05-22 16:45:00'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        25,
        'Bowflex SelectTech 552 Adjustable Dumbbells',
        429,
        'Replaces 15 sets of weights. Adjusts from 5 to 52.5 lbs in 2.5 lb increments.',
        45,
        'Sports & Outdoors',
        'POPULAR',
        'https://example.com/images/bowflex-552.jpg',
        '2026-05-22 16:45:30',
        '2026-05-22 16:45:30'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        26,
        'Sonos Roam Portable Speaker',
        179,
        'Waterproof, drop-resistant portable smart speaker with Bluetooth, Wi-Fi, and voice control.',
        320,
        'Electronics',
        'NEW',
        'https://example.com/images/sonos-roam.jpg',
        '2026-05-22 16:46:15',
        '2026-05-22 16:46:15'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        27,
        'LEGO Star Wars Millennium Falcon 75257',
        160,
        'Features opening top panels, spring-loaded shooters, and 7 LEGO Star Wars characters.',
        150,
        'Toys & Games',
        'POPULAR',
        'https://example.com/images/lego-falcon.jpg',
        '2026-05-22 16:47:05',
        '2026-05-22 16:47:05'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        28,
        'YETI Tundra 45 Cooler',
        325,
        null,
        85,
        'Sports & Outdoors',
        'POPULAR',
        null,
        '2026-05-22 16:48:22',
        '2026-05-22 16:48:22'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        29,
        'Ninja AF101 Air Fryer, 4 Qt',
        99,
        'Air fry, roast, reheat, and dehydrate. 4-quart ceramic-coated basket fits 2 lbs of french fries.',
        600,
        'Home & Kitchen',
        'POPULAR',
        'https://example.com/images/ninja-air-fryer.jpg',
        '2026-05-22 16:50:10',
        '2026-05-22 16:50:10'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        30,
        'ErgoChair Pro Ergonomic Office Chair',
        499,
        'Fully adjustable ergonomic chair with lumbar support, breathable mesh back, and 3D armrests.',
        120,
        'Furniture',
        'POPULAR',
        'https://example.com/images/ergochair-pro.jpg',
        '2026-05-22 16:51:00',
        '2026-05-22 16:51:00'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        31,
        'Vitamix 5200 Professional-Grade Blender',
        399,
        '64 oz container, variable speed control, and aircraft-grade stainless steel blades.',
        200,
        'Home & Kitchen',
        'POPULAR',
        'https://example.com/images/vitamix-5200.jpg',
        '2026-05-22 16:52:15',
        '2026-05-22 16:52:15'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        32,
        'Levi''s Men''s 501 Original Fit Jeans',
        79,
        'The original blue jean since 1873. Classic straight fit with a signature button fly.',
        550,
        'Apparel',
        'POPULAR',
        'https://example.com/images/levis-501.jpg',
        '2026-05-22 16:53:30',
        '2026-05-22 16:53:30'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        33,
        'Laneige Lip Sleeping Mask',
        24,
        'Leave-on lip mask that delivers intense moisture and antioxidants while you sleep.',
        900,
        'Beauty & Personal Care',
        'NEW',
        'https://example.com/images/laneige-lip-mask.jpg',
        '2026-05-22 16:54:45',
        '2026-05-22 16:54:45'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        34,
        'Osprey Atmos AG 65 Men''s Backpacking Backpack',
        300,
        null,
        60,
        'Sports & Outdoors',
        'POPULAR',
        null,
        '2026-05-22 16:55:10',
        '2026-05-22 16:55:10'
    );

INSERT INTO
    shop.products (
        id,
        title,
        price,
        description,
        available_quantity,
        category,
        status,
        image_link,
        created_at,
        updated_at
    )
VALUES (
        35,
        'Dune by Frank Herbert (Paperback)',
        18,
        'A stunning blend of adventure and mysticism, environmentalism and politics. A sci-fi masterpiece.',
        450,
        'Books',
        'POPULAR',
        'https://example.com/images/dune-book.jpg',
        '2026-05-22 16:56:00',
        '2026-05-22 16:56:00'
    );

create table orders (
    id bigint auto_increment primary key,
    customer_id bigint not null,
    total_amount bigint not null,
    status varchar(255) default 'pending' null,
    transaction_id bigint null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null on update current_timestamp()
);

create table order_items (
    id bigint auto_increment primary key,
    order_id bigint not null,
    product_id bigint not null,
    ordered_quantity int not null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null on update current_timestamp(),
    constraint `1` foreign key (order_id) references orders (id),
    constraint `2` foreign key (product_id) references products (id)
);

create index order_id on order_items (order_id);

create index product_id on order_items (product_id);

create table carts (
    id bigint auto_increment primary key,
    customer_id int not null,
    status varchar(30) not null,
    created_at datetime default current_timestamp() null,
    updated_at datetime default current_timestamp() null
);

create table cart_items (
    id bigint auto_increment primary key,
    cart_id bigint null,
    product_id bigint null,
    quantity int not null,
    created_at datetime default current_timestamp() null,
    updated_at datetime default current_timestamp() null,
    constraint `1` foreign key (cart_id) references carts (id),
    constraint `2` foreign key (product_id) references products (id)
);