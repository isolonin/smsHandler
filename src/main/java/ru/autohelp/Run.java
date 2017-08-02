package ru.autohelp;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.autohelp.smpp.Client;

@SpringBootApplication
public class Run {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Run.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext context = app.run(args);
        Client client = context.getBean(Client.class);
        client.run();
    }
}
