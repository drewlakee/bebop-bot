package github.drewlakee.telegram.commands.callbacks;

public enum PostCallback {

    CHANGE_SET,
    CHANGE_AUDIO_QUANTITY, REFRESH_ONLY_AUDIO,
    CHANGE_PHOTO_QUANTITY, REFRESH_ONLY_PHOTO,
    CONSTRUCT,
    GROUP,
    SEND, SEND_AGAIN;

    public String toCallbackString(String commandCallback) {
        return commandCallback + "_" + this.name();
    }
}
