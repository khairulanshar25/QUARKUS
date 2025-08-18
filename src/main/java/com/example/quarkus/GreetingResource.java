package com.example.quarkus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class GreetingResource {

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello(@QueryParam("name") String name) {
        Map<String, Object> response = new HashMap<>();

        String greeting = name != null ? "Hello, " + name + "!" : "Hello, World!";

        response.put("message", greeting);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("application", "Simple Quarkus App");
        response.put("version", "1.0.0");

        return Response.ok(response).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        Map<String, Object> status = new HashMap<>();

        status.put("status", "UP");
        status.put("service", "Simple Quarkus Application");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("java_version", System.getProperty("java.version"));
        status.put("quarkus_version", "3.2.4.Final");

        return Response.ok(status).build();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info() {
        Map<String, Object> info = new HashMap<>();

        info.put("application_name", "Simple Quarkus Application");
        info.put("description", "A simple Quarkus REST API with Docker support");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("framework", "Quarkus");
        info.put("java_version", System.getProperty("java.version"));
        info.put("build_timestamp", LocalDateTime.now().toString());

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/hello", "Returns a greeting message");
        endpoints.put("GET /api/hello?name=<name>", "Returns a personalized greeting");
        endpoints.put("GET /api/status", "Returns application status");
        endpoints.put("GET /api/info", "Returns application information");
        endpoints.put("GET /q/health", "Health check endpoint");
        endpoints.put("GET /q/metrics", "Metrics endpoint");

        info.put("available_endpoints", endpoints);

        return Response.ok(info).build();
    }
}
