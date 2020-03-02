package vk.domain.groups;

import java.util.HashMap;
import java.util.Random;

public class VkGroupPool {

    private static HashMap<String, VkGroup> pool;

    public static void add(VkGroup vkGroup) {
        if (isEmpty()) {
            pool = new HashMap<>();
        }

        pool.put(vkGroup.getName(), vkGroup);
    }

    public static void add(HashMap<String, VkGroup> groups) {
        for (VkGroup group : groups.values())
            add(group);
    }

    public static HashMap<String, VkGroup> getPool() {
        return pool;
    }

    public static VkGroup getRandomAudioGroup() {
        Object[] audioGroups = pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getGroupObjective().equals(GroupObjective.AUDIO))
                .toArray();
        int randomIndex = new Random().nextInt(audioGroups.length);
        return (VkGroup) audioGroups[randomIndex];
    }

    public static VkGroup getRandomPhotoGroup() {
        Object[] photoGroups = pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getGroupObjective().equals(GroupObjective.PHOTO))
                .toArray();
        int randomIndex = new Random().nextInt(photoGroups.length);
        return (VkGroup) photoGroups[randomIndex];
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
