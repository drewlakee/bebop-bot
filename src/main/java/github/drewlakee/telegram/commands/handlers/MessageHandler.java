package github.drewlakee.telegram.commands.handlers;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface MessageHandler {

    void handle(AbsSender sender, Message message);
}
