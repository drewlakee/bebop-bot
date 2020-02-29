package vk.domain;

import vk.domain.groups.AudioGroupPool;
import vk.domain.groups.PhotoGroupPool;

public class GroupProvider {

    private static AudioGroupPool audioGroupPool;
    private static PhotoGroupPool photoGroupPool;

    public static void init(AudioGroupPool agp, PhotoGroupPool pgp) {
        audioGroupPool = agp;
        photoGroupPool = pgp;
    }

    public static void init(AudioGroupPool agp) {
        audioGroupPool = agp;
    }

    public static void init(PhotoGroupPool pgp) {
        photoGroupPool = pgp;
    }

    public static AudioGroupPool getAudioGroupPool() {
        return audioGroupPool;
    }

    public static PhotoGroupPool getPhotoGroupPool() {
        return photoGroupPool;
    }
}
