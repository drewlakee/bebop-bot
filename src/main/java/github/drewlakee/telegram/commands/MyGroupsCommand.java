package github.drewlakee.telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.singletons.VkGroupPool;

public class MyGroupsCommand extends BotCommand implements MessageHandler {

    public static final String COMMAND_NAME = "/myGroups";

    public MyGroupsCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Your groups: " + buildGroupsDesk());
        response.disableWebPagePreview();

        ResponseMessageDispatcher.send(sender, response);
    }

    private String buildGroupsDesk() {
        StringBuilder desk = new StringBuilder();
        int count = 0;

        for (VkCustomGroup group : VkGroupPool.getConcreteGroups(VkGroupObjective.HOST)) {
            desk.append(String.format("\n\n%d: %s (%d)\n%s", count, group.getName(), group.getId(), group.getUrl()));
            count++;
        }
        desk.append("\n\n").append("Total - ").append(count).append(".");

        return desk.toString();
    }
}
