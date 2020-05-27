# Telegram бот-помощник в VK сообществе

- [Команды](#команды)
- [Пользовательские переменные окружения](#пользовательские-переменные-окружения)

#### Команды:

- /constructOnlyPhotos

Команда подборки фотографий из групп типа *PHOTO*

- /constructOnlyAudios

Команда подборки аудиозаписей из групп типа *AUDIO*

- /myGroups

Команда вывода текущих групп типа *HOST*

###### Список групп формируется в файле [groups](src/main/resources/groups), модель группы - [VkCustomGroup](src/main/java/github/drewlakee/vk/domain/groups/VkCustomGroup.java).      

#### Пользовательские переменные окружения:

Telegram bot:

```` 
bot_username=username
bot_token=token
[bot_proxy_type=type]
[bot_proxy_host=ip]
[bot_proxy_port=port]
[bot_threads=count]
````

[...] - опционально

bot_proxy_type - тип протокола: HTTP, SOCKS4, SOCKS5

bot_threads - количество потоков, которые обрабатывают запросы к боту

VK API:

````
vk_token=token
vk_user_id=user_id
````

Ключ доступа получается через Implicit Flow для доступа к VK от лица пользователя.

Подробнее в [документации ВКонтакте](https://vk.com/dev/manuals).