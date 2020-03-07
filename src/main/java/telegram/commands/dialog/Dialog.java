package telegram.commands.dialog;

public abstract class Dialog {

    private final int messageId;
    private final int timeToLiveMinutes;
    private long timeWhenDialogStartMillis;

    protected Dialog(int messageId, int timeToLiveMinutes) {
        this.messageId = messageId;
        this.timeToLiveMinutes = timeToLiveMinutes;
        this.timeWhenDialogStartMillis = System.currentTimeMillis();
    }

    public void updateLiveTime() {
        this.timeWhenDialogStartMillis = System.currentTimeMillis();
    }

    public int getMessageId() {
        return messageId;
    }

    public int getTimeToLiveMinutes() {
        return timeToLiveMinutes;
    }

    public long getTimeWhenDialogStartMillis() {
        return timeWhenDialogStartMillis;
    }
}
