package com.zenora;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ZenoraApplication {

    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {

        // Jalankan Spring Boot
        springContext = new SpringApplicationBuilder(ZenoraApplication.class)
                .headless(false)
                .run(args);

        System.out.println("[Zenora] Spring Boot started");
        System.out.println("[Zenora] H2 Console: http://localhost:8080/h2-console");

        // Jalankan JavaFX
        Application.launch(com.zenora.app.MainApp.class, args);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return springContext.getBean(beanClass);
    }

    public static ConfigurableApplicationContext getContext() {
        return springContext;
    }
}