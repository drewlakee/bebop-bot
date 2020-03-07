package telegram.commands.dialog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DialogsCleanerThread extends Thread {

    private final ConcurrentHashMap<Integer, ? extends Dialog> dialogs;

    public DialogsCleanerThread(ConcurrentHashMap<Integer, ? extends Dialog> dialogs) {
            this.dialogs = dialogs;
    }

    @Override
    public void run() {
            coolDown();

            dialogs.forEach((messageId, dialog) -> {
            long timeSpend = System.currentTimeMillis() - dialog.getTimeWhenDialogStartMillis();
            if (TimeUnit.MILLISECONDS.toMinutes(timeSpend) < dialog.getTimeToLiveMinutes())
                dialogs.remove(messageId);
            });

            run();
    }

    private void coolDown() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(Dialogs.DEFAULT_CLEANER_THREAD_COOLDOWN_MINUTES));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
