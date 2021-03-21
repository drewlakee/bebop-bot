package github.drewlakee.telegram.commands.users;

import com.vk.api.sdk.objects.groups.GroupAdminLevel;
import github.drewlakee.telegram.commands.BotCommand;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupsCommand extends BotCommand implements MessageHandler {

    private final Logger log = LoggerFactory.getLogger(GroupsCommand.class);

    private final VkGroupsCustodian custodian;

    @Autowired
    public GroupsCommand(VkGroupsCustodian custodian) {
        super("/groups");
        this.custodian = custodian;
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        String desk = toPrettyGroupsDesk();
        response.setText(desk);
        response.disableWebPagePreview();
        response.enableHtml(true);

        try {
            sender.execute(response);
        } catch (TelegramApiException e) {
            log.error(String.format("%s response send error: Cause %s", message.toString(), e.getCause()));
            e.printStackTrace();
        }
    }

    private String toPrettyGroupsDesk() {
        StringBuilder groupsDesk = new StringBuilder();
        List<VkGroupFullDecorator> allGroups = custodian.getAllGroups();
        groupsDesk.append("Группы (всего ").append(allGroups.size()).append(")").append("\n").append("\n");

        for (int index = 0; index < allGroups.size(); index++) {
            groupsDesk.append(index).append(". ");
            groupsDesk.append("<a href=\"").append("https://vk.com/").append(allGroups.get(index).getGroupFull().getScreenName()).append("\">");
            groupsDesk.append(allGroups.get(index).getGroupFull().getName()).append("</a>").append(" ");

            List<String> objectivesAndRights = new ArrayList<>();
            if (allGroups.get(index).getObjective() != VkGroupObjective.EMPTY) {
                objectivesAndRights.add(allGroups.get(index).getObjective().toString());
            }

            if (allGroups.get(index).getGroupFull().getAdminLevel() == GroupAdminLevel.ADMINISTRATOR) {
                objectivesAndRights.add("Администратор");
            }

            if (allGroups.get(index).getGroupFull().getAdminLevel() == GroupAdminLevel.EDITOR) {
                objectivesAndRights.add("Редактор");
            }

            groupsDesk.append("(").append(String.join(", ", objectivesAndRights)).append(")");

            groupsDesk.append("\n").append("\n");
        }

        return groupsDesk.toString();
    }
}
