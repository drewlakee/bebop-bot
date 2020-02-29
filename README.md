###### VM arguments:

```` 
telegram bot:
-Dhost_chat_id=id 
-Dbot_username=username
-Dbot_token=token
[-Dbot_proxy_type=type]type=SOCKS5, SOCKS4, HTTP
[-Dbot_proxy_host=ip]
[-Dbot_proxy_port=port]
[-Dbot_threads=count]

[-D...] - опционально
host_chat_id - id чата пользователя в telegram, от лица которого бот будет взаимодействоать с VK API
bot_proxy_type - тип протокола: HTTP, SOCKS4, SOCKS5
bot_threads - количество потоков, которые обрабатывают запросы к боту

vk api:
-Dvk_token=token
-Dvk_user_id=user_id

Получаются через Implicit Flow для доступа к VK от лица пользователя.

Подробнее в [документации](https://vk.com/dev/manuals).
````