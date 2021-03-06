package github.drewlakee.telegram.utils.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NumpadKeyboardBuilder {

    private final InlineKeyboardBuilder keyboardMarkup;
    private final int maxColumns;
    private final int toInclusiveNumber;

    public NumpadKeyboardBuilder(int maxColumns, int toInclusiveNumber) {
        this.keyboardMarkup = new InlineKeyboardBuilder();

        // (toInclusiveNumber + 1) -> start from zero num and to toInclusiveNumber
        if (maxColumns > 10 || maxColumns < 1 || (toInclusiveNumber + 1) < 0) {
            throw new IllegalArgumentException("Incorrect arguments for numpad keyboard");
        } else {
            this.maxColumns = maxColumns;
            this.toInclusiveNumber = toInclusiveNumber + 1;
        }
    }

    public InlineKeyboardMarkup build(String commandCallback) {
        return build(commandCallback, false);
    }

    public InlineKeyboardMarkup build(String commandCallback, boolean withCancel) {
        int maxRows = (toInclusiveNumber % this.maxColumns == 0) ? toInclusiveNumber / this.maxColumns : toInclusiveNumber / this.maxColumns + 1;
        int count = 0;
        for (int rows = 0; rows < maxRows; rows++) {
            for (int columns = 0; count < toInclusiveNumber && columns < this.maxColumns; columns++) {
                this.keyboardMarkup.addButton(new InlineKeyboardButton().setText(String.valueOf(count)).setCallbackData(commandCallback + "_numpad" + count));
                count++;
            }

            if (rows != (maxRows - 1)) {
                this.keyboardMarkup.nextLine();
            }
        }

        if (withCancel) {
            this.keyboardMarkup
                    .nextLine()
                    .addButton(new InlineKeyboardButton().setText("Отменить запрос").setCallbackData("/deleteMessage"));
        }

        return this.keyboardMarkup.build();
    }
}
