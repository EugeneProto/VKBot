# VKBot
Bot for VK social network with some usefull and funny functions.
***
## Dependencies
This project use these libraries:

* `com.vk.api:sdk:0.5.7`
* `org.json:json:20180130`
* `com.google.maps:google-maps-services:0.2.6`
* `ai.api:libai:1.1.0`

You can find all of this libraries in Maven. Downlad it before start working.

## APIs

### Vk API
First of all, create standalone app at [Vk Developers](https://vk.com/apps?act=manage). Read about [authorization](https://vk.com/dev/implicit_flow_user) in Vk<br>
(for my app, parameter `scope=friends,messages,offline,status,photos,video`<br>
and parameter `response_type=token`). At `config.properties` file `access-token`<br>
is access token, `user-id-main` is your id in Vk.

### Google APIs
Create project on [Google APIs Console](https://console.developers.google.com). Then add [Google Distance Matrix Api](https://console.developers.google.com/apis/library/distance-matrix-backend.googleapis.com)<br>
and [Dialogflow Api](https://console.developers.google.com/apis/library/dialogflow.googleapis.com) to your project. Create agent at Dialogflow (learn more [here](https://dialogflow.com/docs/getting-started/basics)).<br>
At `config.properties` file `google-key` is your google app API key,<br>
`ai-client-key` is Dialogflow access token.

### OpenWeatherMap API
Create account at [OpenWeatherMap](https://home.openweathermap.org). At `config.properties` file<br>
`weather-key` is OpenWeatherMap API key.

## Other information
Now you have all API keys and tokens for work. If you have some problems, contact<br>
me on [Twitter](https://twitter.com/EugeneTheDev) or [Telegram](https://t.me/EugeneTheDev).
