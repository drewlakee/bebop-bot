package vk;

public class VkAccessToken {

    private static String token;
    private static int userId;

    public static void init(String rToken, int rUserId) {
        if (isEmpty()) {
            token = rToken;
            userId = rUserId;
        }
    }

    public static boolean isEmpty() {
        return token == null || userId == 0;
    }

    public static String getToken() {
        return token;
    }

    public static int getUserId() {
        return userId;
    }
}
