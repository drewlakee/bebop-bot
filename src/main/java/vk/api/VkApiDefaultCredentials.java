package vk.api;

public class VkApiDefaultCredentials implements VkApiCredentials {
    @Override
    public int getUserId() {
        return Integer.parseInt(System.getenv("vk_user_id"));
    }

    @Override
    public String getUserToken() {
        return System.getenv("vk_token");
    }
}
