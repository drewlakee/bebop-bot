package telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.handlers.BotCommand;
import telegram.ResponseMessageDispatcher;
import telegram.commands.handlers.MessageHandler;
import telegram.commands.statics.Commands;
import vk.domain.groups.VkCustomGroup;
import vk.domain.groups.VkGroupPool;

public class MyGroupsCommand extends BotCommand implements MessageHandler {

    public MyGroupsCommand() {
        super(Commands.MY_GROUPS);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Your groups: " + buildGroupsDesk());

        ResponseMessageDispatcher dispatcher = new ResponseMessageDispatcher(sender);
        dispatcher.send(response);
    }

    private String buildGroupsDesk() {
        StringBuilder desk = new StringBuilder();
        int count = 0;

        for (VkCustomGroup group : VkGroupPool.getHostGroups()) {
            desk.append("\n\n")
                .append(count + ": " + group.getName() + " (" + group.getId() + ")")
                .append("\n")
                .append(group.getUrl());

            count++;
        }
        desk.append("\n\n").append("Total - ").append(count).append(".");

        return desk.toString();
    }
}
