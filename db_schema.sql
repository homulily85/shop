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
    customer_id  bigint                                not null,
    total_amount bigint                                not null,
    created_at   timestamp default current_timestamp() null,
    updated_at   timestamp default current_timestamp() null on update current_timestamp()
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


