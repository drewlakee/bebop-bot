package github.drewlakee.telegram.commands.keyboards;

import github.drewlakee.telegram.commands.callbacks.HandlerBotCallback;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NumpadKeyboardBuilder {

    private final InlineKeyboardBuilder keyboardMarkup;
    private final int maxColumns;
    private final int toNumber;

    public NumpadKeyboardBuilder(int maxColumns, int toNumber) {
        this.keyboardMarkup = new InlineKeyboardBuilder();

        if (maxColumns > 10 || maxColumns < 1 || toNumber < 0) {
            throw new IllegalArgumentException("Incorrect arguments for numpad keyboard");
        } else {
            this.maxColumns = maxColumns;
            this.toNumber = toNumber;
        }
    }

    public InlineKeyboardMarkup build(String commandCallback) {
        return build(commandCallback, false);
    }

    public InlineKeyboardMarkup build(String commandCallback, boolean withCancel) {
        int maxRows = (toNumber % this.maxColumns == 0) ? toNumber / this.maxColumns : toNumber / this.maxColumns + 1;
        int count = 0;
        for (int rows = 0; rows <= maxRows; rows++) {
            for (int columns = 0; count <= toNumber && columns < this.maxColumns; columns++) {
                this.keyboardMarkup.addButton(new InlineKeyboardButton().setText(String.valueOf(count)).setCallbackData(commandCallback + "_numpad" + count));
                count++;
            }

            if (rows < maxRows) {
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
