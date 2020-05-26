package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.objects.groups.Group;
import lombok.Getter;

import java.util.Objects;

@Getter
public class VkCustomGroup extends Group {

    public static final VkCustomGroup EMPTY_INSTANCE = new VkCustomGroup(VkGroupObjective.EMPTY, "empty", 0, "empty_url");

    private VkGroupObjective vkGroupObjective;
    private String url;

    public VkCustomGroup(VkGroupObjective vkGroupObjective, String name, int groupId, String url) {
        setName(name);
        setId(groupId);
        this.vkGroupObjective = vkGroupObjective;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VkCustomGroup group = (VkCustomGroup) o;
        return vkGroupObjective == group.vkGroupObjective &&
                Objects.equals(url, group.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vkGroupObjective, url);
    }
}
