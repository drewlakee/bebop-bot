package github.drewlakee.telegram.commands;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
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

    public static final String COMMAND_NAME = "/my_groups";

    public MyGroupsCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setParseMode(ParseMode.HTML);
        response.setText(buildGroupsDesk());
        response.disableWebPagePreview();

        ResponseMessageDispatcher.send(sender, response);
    }

    private String buildGroupsDesk() {
        StringBuilder desk = new StringBuilder();
        int count = 0;
        desk.append("Your groups: \n\n");
        for (VkCustomGroup group : VkGroupPool.getConcreteGroups(VkGroupObjective.HOST)) {
            desk.append(count).append(": ")
                    .append("<a href=\"").append(group.getUrl()).append("\">").append(group.getName()).append("</a>")
                    .append(" (id: ").append(group.getId()).append(")").append("\n\n");
            count++;
        }
        desk.append("Total - ").append(count).append(".");
        return desk.toString();
    }
}
