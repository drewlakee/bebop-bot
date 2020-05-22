package vk.singletons;

import vk.domain.groups.VkCustomGroup;
import vk.domain.groups.VkGroupObjective;

import java.util.*;
import java.util.stream.Collectors;

public class VkGroupPool {

    private static Map<String, VkCustomGroup> pool;

    public static void add(VkCustomGroup vkCustomGroup) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(vkCustomGroup.getName(), vkCustomGroup);
    }

    public static void add(Map<String, VkCustomGroup> groups) {
        for (VkCustomGroup group : groups.values()) {
            add(group);
        }
    }

    public static List<VkCustomGroup> getConcreteGroups(VkGroupObjective objective) {
        return pool.values().stream()
                .filter(group -> group.getVkGroupObjective().equals(objective))
                .collect(Collectors.toUnmodifiableList());
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
