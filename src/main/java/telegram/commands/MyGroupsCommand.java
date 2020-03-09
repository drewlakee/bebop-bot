package telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.MessageHandler;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;

public class MyGroupsCommand extends BotCommand implements MessageHandler {

    public MyGroupsCommand() {
        super(Commands.MY_GROUPS);
    }


    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText("Твои группы: " + buildGroupsDesk());
        answer.disableWebPagePreview();
        send(sender, answer);
    }

    private String buildGroupsDesk() {
        StringBuilder desk = new StringBuilder();
        int count = 0;

        for (VkGroup group : VkGroupPool.getHostGroups()) {
            desk.append("\n\n")
                .append(count + ": " + group.getName())
                .append("\n")
                .append(group.getUrl());
            count++;
        }

        desk.append("\n\n")
            .append("Всего - " + count + ".");
        return desk.toString();
    }
}
