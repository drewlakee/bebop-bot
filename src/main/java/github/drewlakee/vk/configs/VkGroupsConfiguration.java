package github.drewlakee.vk.configs;

import com.google.common.collect.Sets;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.groups.GroupFull;
import github.drewlakee.vk.domain.groups.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
public class VkGroupsConfiguration {

    private final Environment env;

    private final VkApiClient api;
    private final UserActor user;

    @Autowired
    public VkGroupsConfiguration(Environment env, VkApiClient api, UserActor user) {
        this.env = env;
        this.api = api;
        this.user = user;
    }

    @Autowired
    public void configureOwnedGroups(VkGroupsCustodian custodian) {
        VkGroupFullLoader groups = new VkOwnedGroupFullApiLoader(api, user);
        List<GroupFull> ownedGroups = groups.load();

        Predicate<GroupFull> filter = groupFull -> true;
        if (env.containsProperty("wall_post_groups_scope")) {
            if (!env.getProperty("wall_post_groups_scope").isBlank()) {
                HashSet<String> wallPostGroups = Sets.newHashSet(env.getProperty("wall_post_groups_scope").split(","));
                filter = (group) -> wallPostGroups.contains(group.getScreenName());
            }
        }

        ownedGroups.stream().filter(filter).forEach(group -> custodian.add(group.getScreenName(), new VkGroupFullWrapper(group)));
    }

    @Autowired
    public void configureObjectiveGroups(VkGroupsCustodian custodian) {
        if (!env.containsProperty("vk_audio_groups_ids") || !env.containsProperty("vk_photo_groups_ids")) {
            throw new NullPointerException("Objective groups configuration error: Please configure objective groups ids");
        }

        String vkAudioGroupsIds = env.getProperty("vk_audio_groups_ids");
        String vkPhotoGroupsIds = env.getProperty("vk_photo_groups_ids");

        if (vkAudioGroupsIds.trim().length() < 1 || vkPhotoGroupsIds.trim().length() < 1) {
            throw new NullPointerException("Objective groups parsing error: Please check correctness of your objective group ids");
        }

        Function<String, List<String>> splitAndInt = str -> Arrays.stream(str.split(",")).collect(Collectors.toList());

        List<String> audioGroupIds = splitAndInt.apply(vkAudioGroupsIds);
        VkObjectiveGroupFullApiLoader audioGroupsLoader = new VkObjectiveGroupFullApiLoader(audioGroupIds, api, user);

        for (GroupFull groupFull : audioGroupsLoader.load()) {
            groupFull.setId(groupFull.getId() * -1);
            custodian.add(groupFull.getScreenName(), new VkGroupFullWrapper(VkGroupObjective.AUDIO, groupFull));
        }

        List<String> photoGroupIds = splitAndInt.apply(vkPhotoGroupsIds);
        VkObjectiveGroupFullApiLoader photoGroupsLoader = new VkObjectiveGroupFullApiLoader(photoGroupIds, api, user);

        for (GroupFull groupFull : photoGroupsLoader.load()) {
            groupFull.setId(groupFull.getId() * -1);
            custodian.add(groupFull.getScreenName(), new VkGroupFullWrapper(VkGroupObjective.PHOTO, groupFull));
        }
    }
}
