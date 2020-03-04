package telegram.commands.dialog;

public abstract class Dialog {

    private final Integer messageId;

    protected Dialog(Integer messageId) {
        this.messageId = messageId;
    }

    public synchronized Integer getMessageId() {
        return messageId;
    }
}
