package github.drewlakee;

import github.drewlakee.telegram.BebopBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {

    private final BebopBot bebopBot;

    @Autowired
    public Application(BebopBot bebopBot) {
        this.bebopBot = bebopBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void telegramLaunch() {
        bebopBot.run();
    }
}
