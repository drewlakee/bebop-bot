package vk.domain.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VkGroupPool {

    private static HashMap<String, VkGroup> pool;

    public static void add(VkGroup vkGroup) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(vkGroup.getName(), vkGroup);
    }

    public static void add(HashMap<String, VkGroup> groups) {
        for (VkGroup group : groups.values())
            add(group);
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

    public static List<VkGroup> getHostGroups() {
        return pool.values()
                .stream()
                .filter(vkGroup -> vkGroup.getGroupObjective().equals(GroupObjective.HOST))
                .collect(Collectors.toList());
    }

    public static VkGroup getHostGroup(int id) {
        return pool.values()
                .stream()
                .filter(group -> group.getGroupId() == id)
                .findAny()
                .orElseThrow();
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
