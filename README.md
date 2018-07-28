# VKBot
Bot for VK social network with some usefull and funny functions.<br/>
[Here](https://vk.com/eugenebot) is working group with bot.
***
## Dependencies
This project use these libraries:

* `com.vk.api:sdk:0.5.12`
* `org.json:json:20180130`
* `com.google.maps:google-maps-services:0.2.6`
* `com.google.cloud:google-cloud-dialogflow:0.46.0-alpha`
* `org.slf4j:slf4j-nop:1.7.24`

You can find all of this libraries in Maven. Downlad it before start working.

## APIs

### Vk API
First of all, create standalone app at [Vk Developers](https://vk.com/editapp?act=create). Read about authorization [for users](https://vk.com/dev/implicit_flow_user)<br/>
(for my app, parameter `scope=offline,photos,video,wall` and parameter `response_type=token`)<br/> 
and [for groups](https://vk.com/dev/bots_docs) in VK. At `config.properties` file `group-access-token`<br/>
is access token for your group, `group-id` is your group id in VK,<br/> 
`owner-access-token` is your access token,`owner-id` is your id in VK.

### Google APIs
Create project on [Google APIs Console](https://console.developers.google.com). Then add [Google Distance Matrix API](https://console.developers.google.com/apis/library/distance-matrix-backend.googleapis.com)<br>
and [Dialogflow API](https://console.developers.google.com/apis/library/dialogflow.googleapis.com) to your project. Create agent at Dialogflow (learn more [here](https://dialogflow.com/docs/getting-started/basics)).<br>
At `config.properties` file `google-key` is your google app API key.<br>
`VkBotCredentials.json` is google credentials for Dialogflow API integration.

### OpenWeatherMap API
Create account at [OpenWeatherMap](https://home.openweathermap.org). At `config.properties` file<br>
`weather-key` is OpenWeatherMap API key.

## Other information
Now you have all for work. If you have some problems, contact me on [Twitter](https://twitter.com/EugeneTheDev) or [Telegram](https://t.me/EugeneTheDev).
