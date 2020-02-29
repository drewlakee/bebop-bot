package vk.api;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

public class VkApi {

    private static VkApiClient vk;

    public static VkApiClient instance() {
        if (isEmpty())
            vk = new VkApiClient(new HttpTransportClient());

        return vk;
    }

    private static boolean isEmpty() {
        return vk == null;
    }
}
