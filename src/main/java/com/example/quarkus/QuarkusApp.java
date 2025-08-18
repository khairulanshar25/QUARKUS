package com.example.quarkus;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class QuarkusApp implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(QuarkusApp.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        System.out.println("Simple Quarkus Application is starting...");
        Quarkus.waitForExit();
        return 0;
    }
}
