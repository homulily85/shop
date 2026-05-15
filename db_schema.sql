CREATE DATABASE if NOT EXISTS shop;

USE shop;

DROP TABLE if exists products;
DROP TABLE if exists orders;
DROP TABLE if exists order_items;
DROP TABLE if exists carts;
DROP TABLE if exists cart_items;

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
        336636,
        'Industry-leading noise canceling headphones with exceptional sound quality.',
        50,
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
        1999,
        '100% organic cotton everyday basic t-shirt in navy blue.',
        200,
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
        18999,
        'One-touch pour-over style coffee maker with thermal carafe.',
        50,
        'Home & Kitchen',
        'OUT_OF_STOCK',
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
        3995,
        'A definitive guide to software development and best practices.',
        50,
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
        4500,
        null,
        100,
        'Accessories',
        'POPULAR',
        null,
        '2026-05-06 03:44:29',
        '2026-05-08 01:37:53'
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