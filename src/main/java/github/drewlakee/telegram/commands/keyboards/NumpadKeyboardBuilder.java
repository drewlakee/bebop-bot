package github.drewlakee.telegram.commands.keyboards;

import github.drewlakee.telegram.commands.callbacks.HandlerBotCallback;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NumpadKeyboardBuilder {

    private final InlineKeyboardBuilder keyboardMarkup;
    private final int maxColumns;
    private final int numsQuantity;

    public NumpadKeyboardBuilder(int maxColumns, int numsQuantity) {
        this.keyboardMarkup = new InlineKeyboardBuilder();

        if (maxColumns > 10 || maxColumns < 1 || numsQuantity < 1) {
            throw new IllegalArgumentException("Incorrect arguments for numpad keyboard");
        } else {
            this.maxColumns = maxColumns;
            this.numsQuantity = numsQuantity;
        }
    }

    public InlineKeyboardMarkup build(String commandCallback) {
        return build(commandCallback, false);
    }

    public InlineKeyboardMarkup build(String commandCallback, boolean withCancel) {
        int maxRows = (numsQuantity % this.maxColumns == 0) ? numsQuantity / this.maxColumns : numsQuantity / this.maxColumns + 1;
        int count = 0;
        for (int rows = 0; rows < maxRows; rows++) {
            for (int columns = 0; count <= numsQuantity && columns < this.maxColumns; columns++) {
                this.keyboardMarkup.addButton(new InlineKeyboardButton().setText(String.valueOf(count)).setCallbackData(commandCallback + "_numpad" + count));
                count++;
            }

            if (rows < maxRows - 1) {
                this.keyboardMarkup.nextLine();
            }
        }

        if (withCancel) {
            this.keyboardMarkup
                    .nextLine()
                    .addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(HandlerBotCallback.DELETE_MESSAGE.name()));
        }

        return this.keyboardMarkup.build();
    }
}
