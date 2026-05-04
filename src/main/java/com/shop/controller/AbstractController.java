package com.shop.controller;

import com.shop.webserver.HttpServer;

public abstract class AbstractController {
    protected final HttpServer server;

    public AbstractController(HttpServer server) {
        this.server = server;
    }

    /**
     * Registers the routes for this controller with the HTTP server.
     */
    public abstract void registerRoutes();
}

