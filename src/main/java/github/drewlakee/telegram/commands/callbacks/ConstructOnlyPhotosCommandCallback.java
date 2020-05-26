package github.drewlakee.telegram.commands.callbacks;

import github.drewlakee.telegram.commands.statics.Commands;

public enum  ConstructOnlyPhotosCommandCallback {

    CHANGE_QUANTITY_CALLBACK,
    CHANGE_SET_CALLBACK,
    GROUP_CALLBACK,
    SEND_CALLBACK,
    UNKNOWN;

    public String toCallbackString() {
        return Commands.CONSTRUCT_ONLY_PHOTOS + "_" + this.name();
    }
}
