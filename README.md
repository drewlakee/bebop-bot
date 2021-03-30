# :robot: BebopBot - для ленивых админов и редакторов VK сообществ

1. [Доступные команды](#доступные-команды) :joystick:
2. [Конфигурация](#конфигурация) :gear:
3. [Шоукейсы](#шоукейсы) :video_camera:
4. [Развертывание](#развертывание) :package:

### Доступные команды

Команда | Описание
------------ | -------------
/post | Бот собирает контент из указанных вами групп и формирует пост в ответ на команду в беседе с ботом. Ответ на команду имеет интерактивную клавиатуру, с помощью которой вы можете подобрать заинтересовавшую вас подборку. <br><br> Кратко о клавиатуре: <br> - Возможность выбора количества картинок в посте; <br> - Возможность выбора количества треков в посте; <br> - Можете попросить бота обновить контент, который он для вас собрал; <br> - Можете отправить собранный контент в группы, в которых вы обладаете правами администратора или редактора.
/groups | Бот выводит ваши группы. К этим группам относятся те группы, в которых вы имеете права администратора или редактора, а также группы, из которых бот подбирает для вас контент. <br> <br> На данный момент группы делятся следующим образом: <br> - [AUDIO]: Группы, из которых бот собирает треки; <br> - [PHOTO]: Группы, из которых бот собирает пикчи.

### Конфигурация

1. Обязательные переменные окружения

Параметр                 | Описание  |
------------            |---|  
bot_username            |  Информация находится в "Bot Info", значение Username.  |
bot_token               |  Токен узнается у @BotFather в разделе "API Token" у вашего телеграм-бота. |
vk_user_id              |  Уникальный идентификатор вашего аккаунта ВКонтакте, подробнее в [документации ВКонтакте](https://vk.com/dev/implicit_flow_user). |
vk_token                |  Уникальный токен вашего аккаунта ВКонтакте, подробнее в [документации ВКонтакте](https://vk.com/dev/implicit_flow_user). |
vk_audio_groups_ids     | Уникальные идентификаторы сообществ ВКонтакте, из которых бот будет собирать треки. Параметры указываются слитно с разделителем ",". Пример: dewmsc,melodicbearscrew,medoedsmr   |
vk_photo_groups_ids     | Уникальные идентификаторы сообществ ВКонтакте, из которых бот будет собирать пикчи. Параметры указываются слитно с разделителем ",". Пример: eternalclassic,tnull |

2. Необязательные переменные окружения

Параметр                 | Описание  |
------------            |---|
bot_threads             | Количество потоков, которые будут проинициализированы при старте. Потоки обрабатывают параллельно входящие запросы.  |
bot_proxy_type          | Протоколы для прокси. Доступные: HTTP, SOCKS4, SOCKS5. Подробнее в [документации TelegramBots](https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started).  |
bot_proxy_host          | IPv4 адрес хост-сервера  |
bot_proxy_port          | Порт хост-сервера  |

### Шоукейсы

[![IMAGE ALT TEXT HERE](https://i9.ytimg.com/vi/FxE_PlekTss/mq2.jpg?sqp=CLTFi4MG&rs=AOn4CLD-yuRIozlj482u9Cel0FbjMZiIdQ)](https://youtu.be/FxE_PlekTss)

### Развертывание

#### Способ с ручным сбором образа

1. Клонировать проект

```
git clone git@github.com:drewlakee/bebop-bot.git
```

2. Сконфигурировать необходимые параметры в файле [docker-compose.yaml](docker-compose.yaml)

```
    ...
  - bot_username=<bot_username>
  - bot_token=<bot_token>
  - vk_user_id=<vk_user_id>
  - vk_token=<vk_token>
  - vk_audio_groups_ids=<vk_audio_groups_ids>
  - vk_photo_groups_ids=<vk_photo_groups_ids>
    ...
```

3. Поднять docker-compose с необходимыми параметрами 

```
docker-compose up
```

#### Способ с взятием готового образа с регистра Docker Hub

1. Cоздать файл docker-compose.yaml

```
version: "3"

services:

  telegram-bebop-bot:
    container_name: bebop-bot
    image: drewlakee/bebop-bot:v1.0.1
    environment:
      - bot_username=<bot_username>
      - bot_token=<bot_token>
      - vk_user_id=<vk_user_id>
      - vk_token=<vk_token>
      - vk_audio_groups_ids=<vk_audio_groups_ids>
      - vk_photo_groups_ids=<vk_photo_groups_ids>
```

2. Поднять командой 'docker-compose up'

Можно обращаться к своему поднятому боту :rocket:

