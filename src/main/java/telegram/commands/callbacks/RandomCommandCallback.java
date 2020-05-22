package telegram.commands.callbacks;

import telegram.commands.statics.Commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RandomCommandCallback {

    RC_CANCEL_REQUEST_CALLBACK,
    RC_SEND_CONSTRUCTED_POST_CALLBACK,
    RC_GROUP_CALLBACK,
    RC_CHANGE_PHOTO_CALLBACK,
    RC_CHANGE_AUDIO_CALLBACK,
    RC_RANDOM_MODE_CALLBACK,
    RC_MANUAL_MODE_CALLBACK;

    public static List<Callback> callbacks = Arrays.stream(RandomCommandCallback.values())
            .map(x -> new Callback(x.name(), Commands.RANDOM))
            .collect(Collectors.toUnmodifiableList());
}
