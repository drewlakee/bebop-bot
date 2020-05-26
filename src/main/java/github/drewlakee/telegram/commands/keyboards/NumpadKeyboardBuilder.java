package github.drewlakee.telegram.commands.keyboards;

import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NumpadKeyboardBuilder {

    private final InlineKeyboardBuilder keyboardMarkup;
    private final int maxColumns;

    public NumpadKeyboardBuilder(int maxColumns) {
        this.keyboardMarkup = new InlineKeyboardBuilder();

        if (maxColumns > 10) {
            throw new IllegalArgumentException("Incorrect arguments for numpad keyboard");
        } else {
            this.maxColumns = maxColumns;
        }
    }

    public InlineKeyboardMarkup build(int numsQuantity, String commandCallback, boolean withCancel) {
        int maxRows = (numsQuantity % this.maxColumns == 0) ? numsQuantity / this.maxColumns : numsQuantity / this.maxColumns + 1;
        int count = 1;
        for (int rows = 0; rows < maxRows; rows++) {
            for (int columns = 0; count <= numsQuantity && columns < this.maxColumns; columns++) {
                this.keyboardMarkup.addButton(new InlineKeyboardButton().setText(String.valueOf(count)).setCallbackData(commandCallback + "_numpad" + count));
                count++;
            }
            this.keyboardMarkup.nextLine();
        }

        if (withCancel) {
            this.keyboardMarkup
                    .addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(GlobalCallback.DELETE_MESSAGE.name()));
        }

        return this.keyboardMarkup.build();
    }
}
