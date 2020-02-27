package vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

public class VkApi {

    private static VkApiClient vk;


    private static boolean isNull() {
        return vk == null;
    }

    public static VkApiClient instance() {
        if (isNull())
            vk = new VkApiClient(new HttpTransportClient());

        return vk;
    }
}
