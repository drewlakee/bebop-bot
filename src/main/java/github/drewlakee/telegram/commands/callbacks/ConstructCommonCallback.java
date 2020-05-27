package github.drewlakee.telegram.commands.callbacks;

public enum ConstructCommonCallback {

    CHANGE_QUANTITY_CALLBACK,
    CHANGE_SET_CALLBACK,
    GROUP_CALLBACK,
    SEND_CALLBACK,
    UNKNOWN;

    public String toCallbackString(String commandCallback) {
        return commandCallback + "_" + this.name();
    }
}
