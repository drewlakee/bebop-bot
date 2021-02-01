package github.drewlakee;

import github.drewlakee.telegram.ContentDeliveryBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {

    private final ContentDeliveryBot contentDeliveryBot;

    @Autowired
    public Application(ContentDeliveryBot contentDeliveryBot) {
        this.contentDeliveryBot = contentDeliveryBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void telegramLaunch() {
        contentDeliveryBot.run();
    }
}
