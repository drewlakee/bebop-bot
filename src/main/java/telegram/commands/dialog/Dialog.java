package telegram.commands.dialog;

public abstract class Dialog {

    private final Integer messageId;

    protected Dialog(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getMessageId() {
        return messageId;
    }
}
