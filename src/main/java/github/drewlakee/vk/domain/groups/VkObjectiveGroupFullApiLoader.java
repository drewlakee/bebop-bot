package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VkObjectiveGroupFullApiLoader implements VkGroupFullLoader {

    private final Logger log = LoggerFactory.getLogger(VkObjectiveGroupFullApiLoader.class);

    private final List<String> groupIds;
    private final VkApiClient api;
    private final UserActor user;

    public VkObjectiveGroupFullApiLoader(List<String> groupIds, VkApiClient api, UserActor user) {
        this.groupIds = groupIds;
        this.api = api;
        this.user = user;
    }

    @Override
    public List<GroupFull> load() {
        List<GroupFull> items = new ArrayList<>();

        try {
            items = api.groups().getByIdLegacy(user).groupIds(groupIds).execute().stream()
            .map(legacy -> {
                GroupFull groupFull = new GroupFull();
                groupFull.setAdminLevel(legacy.getAdminLevel());
                groupFull.setName(legacy.getName());
                groupFull.setScreenName(legacy.getScreenName());
                groupFull.setId(legacy.getId());
                return groupFull;
            }).collect(Collectors.toList());
        } catch (ApiException | ClientException apiException) {
            log.error("VK API ERROR: Cause", apiException.getCause());
            apiException.printStackTrace();
        }

        return items;
    }
}
