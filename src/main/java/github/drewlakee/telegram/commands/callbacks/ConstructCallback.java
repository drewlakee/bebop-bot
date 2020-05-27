package github.drewlakee.telegram.commands.callbacks;

public enum ConstructCallback {

    CHANGE_QUANTITY_CALLBACK,
    CHANGE_SET_CALLBACK,
    CHANGE_AUDIO_QUANTITY_CALLBACK,
    CHANGE_PHOTO_QUANTITY_CALLBACK,
    CONSTRUCT_CALLBACK,
    GROUP_CALLBACK,
    SEND_CALLBACK,
    DEFAULT;

    public String toCallbackString(String commandCallback) {
        return commandCallback + "_" + this.name();
    }
}
