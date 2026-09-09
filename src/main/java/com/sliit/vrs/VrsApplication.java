package com.sliit.vrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This is the entry point of the whole application.
// Running this class starts the embedded web server (Tomcat) on port 8080.
@SpringBootApplication
public class VrsApplication {
    public static void main(String[] args) {
        SpringApplication.run(VrsApplication.class, args);
    }
}
