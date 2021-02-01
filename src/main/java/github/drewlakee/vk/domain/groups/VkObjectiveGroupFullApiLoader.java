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
            items = api.groups().getById(user).groupIds(groupIds).execute();
        } catch (ApiException | ClientException apiException) {
            log.error("VK API ERROR: Cause", apiException.getCause());
            apiException.printStackTrace();
        }

        return items;
    }
}
