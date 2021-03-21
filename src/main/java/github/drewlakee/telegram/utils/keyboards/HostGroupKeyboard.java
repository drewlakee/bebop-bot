package github.drewlakee.telegram.utils.keyboards;

import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class HostGroupKeyboard {

    private final VkGroupsCustodian custodian;

    private final InlineKeyboardBuilder keyboard;

    public HostGroupKeyboard(VkGroupsCustodian custodian) {
        this.keyboard = new InlineKeyboardBuilder();
        this.custodian = custodian;
    }

    public InlineKeyboardMarkup build(String callbackCommand, boolean withCancel) {
        List<VkGroupFullDecorator> groupsWithEditableRights = custodian.getGroupsWithEditableRights();

        for (VkGroupFullDecorator editableGroup : groupsWithEditableRights) {
            this.keyboard.addButton(new InlineKeyboardButton()
                    .setText(editableGroup.getGroupFull().getName())
                    .setCallbackData(callbackCommand + "_group_id" + editableGroup.getGroupFull().getId()))
                    .nextLine();
        }

        if (withCancel) {
            this.keyboard
                    .addButton(new InlineKeyboardButton()
                            .setText("Отменить запрос")
                            .setCallbackData("/deleteMessage"));
        }

        return this.keyboard.build();
    }
}
