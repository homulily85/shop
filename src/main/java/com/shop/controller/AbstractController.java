package com.shop.controller;

import com.shop.webserver.HttpServer;

public abstract class AbstractController {
    protected final HttpServer server;

    public AbstractController(HttpServer server) {
        this.server = server;
    }

    public abstract void registerRoutes();
}

