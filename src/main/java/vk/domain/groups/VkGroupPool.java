package vk.domain.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class VkGroupPool {

    private static Map<String, VkCustomGroup> pool;

    public static void add(VkCustomGroup vkCustomGroup) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(vkCustomGroup.getName(), vkCustomGroup);
    }

    public static void add(Map<String, VkCustomGroup> groups) {
        for (VkCustomGroup group : groups.values())
            add(group);
    }

    public static VkCustomGroup getRandomAudioGroup() {
        Object[] audioGroups = pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getVkGroupObjective().equals(VkGroupObjective.AUDIO))
                .toArray();
        int randomIndex = new Random().nextInt(audioGroups.length);
        return (VkCustomGroup) audioGroups[randomIndex];
    }

    public static VkCustomGroup getRandomPhotoGroup() {
        Object[] photoGroups = pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getVkGroupObjective().equals(VkGroupObjective.PHOTO))
                .toArray();
        int randomIndex = new Random().nextInt(photoGroups.length);
        return (VkCustomGroup) photoGroups[randomIndex];
    }

    public static List<VkCustomGroup> getHostGroups() {
        return pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getVkGroupObjective().equals(VkGroupObjective.HOST))
                .collect(Collectors.toList());
    }

    public static VkCustomGroup getHostGroup(int id) {
        return pool.values()
                .stream()
                .filter(group -> group.getId() == id)
                .findAny()
                .orElseThrow();
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
