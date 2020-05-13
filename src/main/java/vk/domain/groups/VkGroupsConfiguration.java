package vk.domain.groups;

import java.util.Map;

public interface VkGroupsConfiguration {

    Map<String, VkCustomGroup> loadGroups();
}
