create table products
(
    id                 bigint auto_increment
        primary key,
    title              varchar(100)                          not null,
    price              bigint                                not null,
    description        mediumtext                            null,
    available_quantity int                                   not null,
    category           varchar(100)                          null,
    status             varchar(50)                           null,
    image_link         text                                  null,
    created_at         timestamp default current_timestamp() null,
    updated_at         timestamp default current_timestamp() null on update current_timestamp()
);

create table orders
(
    id           bigint auto_increment
        primary key,
    customer_id  bigint                                   not null,
    total_amount bigint                                   not null,
    status       varchar(255) default 'pending'           null,
    created_at   timestamp    default current_timestamp() null,
    updated_at   timestamp    default current_timestamp() null on update current_timestamp()
);

create table order_items
(
    id               bigint auto_increment
        primary key,
    order_id         bigint                                not null,
    product_id       bigint                                not null,
    ordered_quantity int                                   not null,
    created_at       timestamp default current_timestamp() null,
    updated_at       timestamp default current_timestamp() null on update current_timestamp(),
    constraint `1`
        foreign key (order_id) references orders (id),
    constraint `2`
        foreign key (product_id) references products (id)
);

create index order_id
    on order_items (order_id);

create index product_id
    on order_items (product_id);

INSERT INTO shop.products (id, title, price, description, available_quantity, category, status, image_link, created_at,
                           updated_at)
VALUES (1, 'Sony WH-1000XM5 Wireless Headphones', 34800,
        'Industry-leading noise canceling headphones with exceptional sound quality.', 45, 'Electronics', 'POPULAR',
        'https://example.com/images/sony-wh1000xm5.jpg', '2026-05-06 03:44:29', '2026-05-06 03:44:50');
INSERT INTO shop.products (id, title, price, description, available_quantity, category, status, image_link, created_at,
                           updated_at)
VALUES (2, 'Men\'s Classic Cotton Crewneck T-Shirt', 1999, '100% organic cotton everyday basic t-shirt in navy blue.',
        120, 'Apparel', 'POPULAR', 'https://example.com/images/mens-navy-tee.jpg', '2026-05-06 03:44:29',
        '2026-05-06 03:44:50');
INSERT INTO shop.products (id, title, price, description, available_quantity, category, status, image_link, created_at,
                           updated_at)
VALUES (3, 'Bonavita Connoisseur 8-Cup Coffee Maker', 18999,
        'One-touch pour-over style coffee maker with thermal carafe.', 0, 'Home & Kitchen', 'OUT_OF_STOCK',
        'https://example.com/images/bonavita-coffee.jpg', '2026-05-06 03:44:29', '2026-05-06 03:44:29');
INSERT INTO shop.products (id, title, price, description, available_quantity, category, status, image_link, created_at,
                           updated_at)
VALUES (4, 'The Pragmatic Programmer: 20th Anniversary', 3995,
        'A definitive guide to software development and best practices.', 12, 'Books', 'POPULAR',
        'https://example.com/images/pragmatic-programmer.jpg', '2026-05-06 03:44:29', '2026-05-06 03:44:50');
INSERT INTO shop.products (id, title, price, description, available_quantity, category, status, image_link, created_at,
                           updated_at)
VALUES (5, 'Minimalist Leather Bifold Wallet', 4500, null, 85, 'Accessories', 'POPULAR', null, '2026-05-06 03:44:29',
        '2026-05-06 03:44:50');



