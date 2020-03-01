package vk.domain;

import vk.domain.groups.VkGroupPool;

public class VkGroupProvider {

    private static VkGroupPool audioGroupPool;
    private static VkGroupPool photoGroupPool;
    private static VkGroupPool hostGroupPool;

    public static void init(VkGroupPool audioGroupsPool, VkGroupPool photoGroupsPool, VkGroupPool hostGroupsPool) {
        audioGroupPool = audioGroupsPool;
        photoGroupPool = photoGroupsPool;
        hostGroupPool = hostGroupsPool;
    }

    public static VkGroupPool getAudioGroupPool() {
        return audioGroupPool;
    }

    public static VkGroupPool getPhotoGroupPool() {
        return photoGroupPool;
    }

    public static VkGroupPool getHostGroupPool() {
        return hostGroupPool;
    }
}
