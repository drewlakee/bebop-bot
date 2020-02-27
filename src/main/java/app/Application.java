package app;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import telegram.TelegramBot;

public class Application {

    public static void main(String[] args) throws Exception {
        TelegramBot.run();

        int port = Integer.parseInt(System.getProperty("port"));
        String host = System.getProperty("host");

        int clientId = Integer.parseInt(System.getProperty("client_id"));
        String clientSecret = System.getProperty("client_secret");

        HandlerCollection handlers = new HandlerCollection();

        VkApiClient vk = new VkApiClient(new HttpTransportClient());
        handlers.setHandlers(new Handler[]{new RequestHandler(vk, clientId, clientSecret, host)});

        Server server = new Server(port);
        server.setHandler(handlers);

        server.start();
        server.join();

    }
}
