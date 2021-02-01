package github.drewlakee.vk.configs;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class VkApiConfiguration {

    private final Environment env;

    @Autowired
    public VkApiConfiguration(Environment environment) {
        this.env = environment;
    }

    @Bean
    public VkApiClient configureVkApiClient() {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        return api;
    }

    @Bean
    public UserActor configureUserActor() {
        if (!env.containsProperty("vk_user_id") || !env.containsProperty("vk_token")) {
            throw new NullPointerException("VK credentials not founded: Please configure vk properties");
        }

        return new UserActor(
                env.getRequiredProperty("vk_user_id", Integer.class),
                env.getRequiredProperty("vk_token")
        );
    }
}
