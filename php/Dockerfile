FROM php:8.2-fpm

RUN pecl install redis && docker-php-ext-enable redis

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp