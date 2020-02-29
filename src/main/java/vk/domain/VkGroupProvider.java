package vk.domain;

import vk.domain.groups.VkAudioGroupPool;
import vk.domain.groups.VkPhotoGroupPool;

public class VkGroupProvider {

    private static VkAudioGroupPool audioGroupPool;
    private static VkPhotoGroupPool photoGroupPool;

    public static void init(VkAudioGroupPool agp, VkPhotoGroupPool pgp) {
        audioGroupPool = agp;
        photoGroupPool = pgp;
    }

    public static void init(VkAudioGroupPool agp) {
        audioGroupPool = agp;
    }

    public static void init(VkPhotoGroupPool pgp) {
        photoGroupPool = pgp;
    }

    public static VkAudioGroupPool getAudioGroupPool() {
        return audioGroupPool;
    }

    public static VkPhotoGroupPool getPhotoGroupPool() {
        return photoGroupPool;
    }
}
