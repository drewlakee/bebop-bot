package vk.domain.groups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

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
        int randomIndex = new Random().nextInt(pool.size());
        Collection<VkGroup> audioGroup = pool.values()
                .stream()
                .filter(group -> group.getGroupObjective().equals(GroupObjective.AUDIO))
                .collect(Collectors.toList());
        return (VkGroup) audioGroup.toArray()[randomIndex];
    }

    public static VkGroup getRandomPhotoGroup() {
        int randomIndex = new Random().nextInt(pool.size());
        Collection<VkGroup> photoGroup = pool.values()
                .stream()
                .filter(group -> group.getGroupObjective().equals(GroupObjective.PHOTO))
                .collect(Collectors.toList());
        return (VkGroup) pool.values().toArray()[randomIndex];
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
