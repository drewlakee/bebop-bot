package github.drewlakee.telegram;

import github.drewlakee.telegram.utils.keyboards.NumpadKeyboardBuilder;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class NumpadKeyboardBuilderTest {

    @Test
    public void buildCorrectRandomNumpadTest() {
        int columns = RandomUtils.nextInt(1, 10);
        int nums = RandomUtils.nextInt(1, 10);
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(columns, nums);
        InlineKeyboardMarkup keyboard = numpad.build("test");
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        int correctRowsCount = nums / columns + 1;
        Assert.assertEquals(correctRowsCount, rows.size());

        for (List<InlineKeyboardButton> line : rows) {
            Assert.assertTrue(line.size() <= columns);
        }
    }
}
