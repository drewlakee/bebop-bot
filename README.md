###### VM arguments:

```` 
vk:
-Dclient_id={app_vk_id}
-Dclient_secret={app_vk_secret}

telegram bot:
-Dbot_username={bonUsername}
-Dbot_token={botToken}

server:
-Dport={port}
-Dhost={host} : like http://...:{port}
[-DproxySet=true]
[-DsocksProxyHost=ip]
[-DsocksProxyPort=port]
````