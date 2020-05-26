package github.drewlakee.telegram.commands.callbacks;

import github.drewlakee.telegram.commands.statics.Commands;

public enum ConstructCommandCallback {

    SEND_CONSTRUCTED_POST_CALLBACK,
    GROUP_CALLBACK,
    CHANGE_PHOTO_CALLBACK,
    CHANGE_AUDIO_CALLBACK,
    ONLY_MUSIC_MODE_CALLBACK,
    ONLY_PHOTO_MODE_CALLBACK,
    MULTI_MEDIA_CALLBACK;

    public String toCallbackString() {
        return Commands.CONSTRUCT_ONLY_PHOTOS + "_" + this.name();
    }
}
