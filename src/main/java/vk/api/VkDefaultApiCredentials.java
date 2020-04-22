package vk.api;

public class VkDefaultApiCredentials {

    public static final int userId = Integer.parseInt(System.getenv("vk_user_id"));
    public static final String token = System.getenv("vk_token");
}
