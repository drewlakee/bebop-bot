package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.objects.groups.GroupFull;

import java.util.List;

public interface VkGroupFullLoader {

    List<GroupFull> load();
}
