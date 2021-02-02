package github.drewlakee;

import github.drewlakee.telegram.BebopBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;


@SpringBootApplication
public class Application {

    public static final Logger log = LoggerFactory.getLogger(Application.class);

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
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(bebopBot);
        } catch (TelegramApiException e) {
            log.error(String.format("%s: Bot launch fail", this.getClass().getSimpleName()));
            e.printStackTrace();
        }
    }
}
