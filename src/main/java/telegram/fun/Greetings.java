package telegram.fun;

import java.util.List;
import java.util.Random;

public class Greetings {

    private static Random random = new Random();

    private static List<String> greetings = List.of(
            "Ave %s! Ave — «здравствуй» на латыни.",
            "Да благословят (хранят) вас боги, амон-ши! Амон-ши — \"друг\" по-анкариански (Анкария — мир игры \"Sacred Underworld\")",
            "Чёрт бы меня побрал! Это же сам %s!",
            "Рад видеть Его Величество (Её Величество) в добром здравии!",
            "Мир вашему шалашу (дому/хате/селу/городу/стране/племени)",
            "Слава <страна/имя/политическое движение>!",
            "Приветствую своего верного товарища/собутыльника!",
            "Всегда рад встрече с милейшим/добрейшим человеком!",
            "Давно не виделись! Как твои старые кости, друг?",
            "Привет! Вижу, ты ещё не успел познакомиться со смертью?"
    );

    public static String getRandomGreeting(String username) {
        return String.format(greetings.get(random.nextInt(greetings.size())), username);
    }
}
