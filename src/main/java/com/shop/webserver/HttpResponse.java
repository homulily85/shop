package com.shop.webserver;

public record HttpResponse(int statusCode, String statusMessage, String body) {
}