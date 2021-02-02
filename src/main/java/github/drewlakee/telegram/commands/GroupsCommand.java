package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class GroupsCommand extends BotCommand implements MessageHandler {

    public static final String COMMAND_NAME = "/groups";

    private final VkGroupsCustodian custodian;

    @Autowired
    public GroupsCommand(VkGroupsCustodian custodian) {
        super(COMMAND_NAME);
        this.custodian = custodian;
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setParseMode(ParseMode.HTML);
        response.setText(toPrettyGroups());
        response.disableWebPagePreview();

        ResponseMessageDispatcher.send(sender, response);
    }

    private String toPrettyGroups() {
        StringBuilder desk = new StringBuilder();
        int count = 0;
        desk.append("Your groups: \n\n");
        for (VkGroupFullDecorator group : custodian.getAllGroups()) {
            desk.append(count).append(": ")
                    .append("[").append(group.getObjective()).append("] ").append(group.getGroupFull().getName())
                    .append("\n");
            count++;
        }
        desk.append("Total - ").append(count).append(".");
        return desk.toString();
    }
}
