package github.drewlakee.telegram.commands.callbacks;

import github.drewlakee.telegram.commands.ConstructOnlyPhotosCommand;

public enum  ConstructOnlyPhotosCommandCallback {

    CHANGE_QUANTITY_CALLBACK,
    CHANGE_SET_CALLBACK,
    GROUP_CALLBACK,
    SEND_CALLBACK,
    UNKNOWN;

    public String toCallbackString() {
        return ConstructOnlyPhotosCommand.COMMAND_NAME + "_" + this.name();
    }
}
