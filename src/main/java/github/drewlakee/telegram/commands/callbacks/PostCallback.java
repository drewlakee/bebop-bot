package github.drewlakee.telegram.commands.callbacks;

public enum PostCallback {

    CHANGE_SET_CALLBACK,
    CHANGE_AUDIO_QUANTITY_CALLBACK, REFRESH_ONLY_AUDIO,
    CHANGE_PHOTO_QUANTITY_CALLBACK, REFRESH_ONLY_PHOTO,
    CONSTRUCT_CALLBACK,
    GROUP_CALLBACK,
    SEND_CALLBACK;

    public String toCallbackString(String commandCallback) {
        return commandCallback + "_" + this.name();
    }
}
