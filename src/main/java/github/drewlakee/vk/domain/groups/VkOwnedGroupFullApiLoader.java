package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.Filter;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.groups.responses.GetExtendedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VkOwnedGroupFullApiLoader implements VkGroupFullLoader {

    private final Logger log = LoggerFactory.getLogger(VkOwnedGroupFullApiLoader.class);

    private final VkApiClient api;
    private final UserActor user;

    public VkOwnedGroupFullApiLoader(VkApiClient api, UserActor user) {
        this.api = api;
        this.user = user;
    }

    @Override
    public List<GroupFull> load() {
        List<GroupFull> items = new ArrayList<>();

        GetExtendedResponse execute = null;
        try {
            execute = api.groups()
                    .getExtended(user)
                    .filter(Filter.ADMIN, Filter.EDITOR)
                    .execute();
        } catch (ApiException | ClientException apiException) {
            log.error("VK API ERROR: Cause {}", apiException.getCause());
            apiException.printStackTrace();
        }

        if (execute != null) {
            items = execute.getItems();
        }

        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VkOwnedGroupFullApiLoader that = (VkOwnedGroupFullApiLoader) o;
        return Objects.equals(log, that.log) && Objects.equals(api, that.api) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(log, api, user);
    }
}
