package telegram.commands.dialog;

public abstract class Dialog {

    private final int messageId;

    protected Dialog(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
