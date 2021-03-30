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

import java.util.List;
import java.util.stream.Collectors;

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
        response.setText(prettyGroupsDesk());
        response.disableWebPagePreview();
        response.enableHtml(true);

        try {
            sender.execute(response);
        } catch (TelegramApiException e) {
            log.error(String.format("%s response send error: Cause %s", message.toString(), e.getCause()));
            e.printStackTrace();
        }
    }

    private String prettyGroupsDesk() {
        StringBuilder groupsDesk = new StringBuilder();
        List<VkGroupFullDecorator> allGroups = custodian.getAllGroups();
        groupsDesk.append("Всего уникальных групп ").append(allGroups.size()).append("\n").append("\n");
        constructCountedGroupListOnDesk(groupsDesk, allGroups);
        constructGroupListWithObjectives(groupsDesk, allGroups);
        constructGroupListWithAdminRoles(groupsDesk, allGroups);
        return groupsDesk.toString();
    }

    private void constructGroupListWithAdminRoles(StringBuilder groupsDesk, List<VkGroupFullDecorator> allGroups) {
        GroupAdminLevel[] groupRoles = GroupAdminLevel.values();
        for (final GroupAdminLevel currentGroupRole : groupRoles) {
            List<VkGroupFullDecorator> groupsWithCurrentGroupRole = allGroups.stream()
                    .filter(group -> group.getGroupFull().getAdminLevel() == currentGroupRole)
                    .collect(Collectors.toUnmodifiableList());

            if (groupsWithCurrentGroupRole.size() > 0) {
                groupsDesk.append("Подгруппа с правами ").append(currentGroupRole.name()).append(":").append("\n");
                constructCountedGroupListOnDesk(groupsDesk, groupsWithCurrentGroupRole);
            }
        }
    }

    private void constructGroupListWithObjectives(StringBuilder groupsDesk, List<VkGroupFullDecorator> allGroups) {
        VkGroupObjective[] groupObjectives = VkGroupObjective.values();
        for (final VkGroupObjective currentGroupObjective : groupObjectives) {
            if (currentGroupObjective != VkGroupObjective.EMPTY) {
                List<VkGroupFullDecorator> groupsWithCurrentObjective = allGroups.stream()
                        .filter(group -> group.getObjective() == currentGroupObjective)
                        .collect(Collectors.toUnmodifiableList());

                groupsDesk.append("Подгруппа с назначением ").append(currentGroupObjective).append(":").append("\n");
                constructCountedGroupListOnDesk(groupsDesk, groupsWithCurrentObjective);
            }
        }
    }

    private void constructCountedGroupListOnDesk(StringBuilder groupsDesk, List<VkGroupFullDecorator> concreteGroups) {
        for (int count = 0; count < concreteGroups.size(); count++) {
            groupsDesk.append(count + 1).append(". ");
            groupsDesk.append("<a href=\"").append("https://vk.com/").append(concreteGroups.get(count).getGroupFull().getScreenName()).append("\">");
            groupsDesk.append(concreteGroups.get(count).getGroupFull().getName()).append("</a>").append(" ");
            groupsDesk.append("\n");
        }

        groupsDesk.append("\n");
    }
}
