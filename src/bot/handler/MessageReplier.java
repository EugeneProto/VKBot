package bot.handler;

import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import bot.Bot;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.slf4j.Logger;

public class MessageReplier {
    private Bot bot;
    private Logger logger;

    public MessageReplier(Bot bot) {
        this.bot = bot;
        this.logger=Bot.logger;
    }

    public void parse(String message, UserXtrCounters addressee){
        String data=message.toLowerCase();
        if(data.matches("привет.*")){
            String heart=bot.getEmojies().get("heart");
            bot.sendMessage(addressee.getId(),"Привет, я временно заменяю хозяина этой страницы.\n" +
                    "И не зови меня бот. Зови меня Юджин"+bot.getEmojies().get("coolEmoji")+"\n" +
                    "Вот что я пока могу:\n" +
                    "1.\"Юджин, лайки на стене\": пришлю суммарное количество лайков " +
                    "под последними 100 фото со стены" +heart+"\n"+
                    "2.\"Юджин, лайки в профиле\": пришлю суммарное количество лайков " +
                    "под последними 100 фото в профиле" +heart+"\n"+
                    "3.\"Юджин, всего лайков\": пришлю суммарное количество лайков " +
                    "под последними 100 записями" +heart+"\n"+
                    "4.\"Юджин, курс биткоина\": курс биткоина в долларах" +bot.getEmojies().get("dollar")+"\n" +
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода <город (в именительном падеже)> <код страны>\"."+
                    "Пример: \"Погода Москва ру\" или \"Погода Moscow ru\""+
                    bot.getEmojies().get("thermometer")+"\n" +
                    "6.\"Юджин, пошли меня\": могу послать"+bot.getEmojies().get("fuck")+"\n"+
                    "Или можем просто поболтать, но я еще учусь, и мои ответы могут быть не совсем точными.\n"+
                    "Знаю, пока это немного, но я развиваюсь:)");
        } else if (data.matches("юджин,? ?лайки на стене.*|1.*")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+bot.getEmojies().get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.matches("юджин,? ?лайки в профиле.*|2.*")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+bot.getEmojies().get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.matches("юджин,? ?всего лайков.*|3.*")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+bot.getEmojies().get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.matches("юджин,? ?курс биткоина.*|4.*")){
            String[] btrate=bot.bitcoinRate();
            String dollar=bot.getEmojies().get("dollar");
            bot.sendMessage(addressee.getId(),"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if (data.matches("юджин,? ?пошли меня.*|5.*")){
            bot.sendMessage(addressee.getId(),"Я, конечно, культурный бот, но раз ты просишь...\n" +
                    addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"+bot.getEmojies().get("fuck"));
        } else if(data.matches("погода.*")){
            String emojiLine=bot.getEmojies().get("thermometer")+bot.getEmojies().get("sun")
                    +bot.getEmojies().get("clWithLight")+bot.getEmojies().get("lightning")
                    +bot.getEmojies().get("snowflake")+bot.getEmojies().get("cloud")
                    +bot.getEmojies().get("clWithSnow")+bot.getEmojies().get("clWithRain")
                    +bot.getEmojies().get("clWithSun")+bot.getEmojies().get("sunWithCl")
                    +bot.getEmojies().get("sunWithClWithR");
            String[] input=data.split(" ");
            String result=input.length==3?bot.receiveWeatherForecast(input[1],input[2]):"";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода <город (в именительном падеже)> <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        } else{
            String response=aiAnswer(message);
            bot.sendMessage(addressee.getId(),response.equals("")?"Извини, я тебя не понял.":response);
        }
    }
    public void parseAdmin(String message, UserXtrCounters addressee){
        if(message.toCharArray()[0]!='/') return;
        String data=message.toLowerCase().substring(1);
        if(data.matches("greeting")){
            String heart=bot.getEmojies().get("heart");
            bot.sendMessage(addressee.getId(),"Привет, я временно заменяю хозяина этой страницы.\n" +
                    "И не зови меня \"Бот\". Зови меня Юджин"+bot.getEmojies().get("coolEmoji")+"\n" +
                    "Вот что я пока могу:\n" +
                    "1.\"Юджин, лайки на стене\": пришлю суммарное количество лайков " +
                    "под последними 100 фото со стены" +heart+"\n"+
                    "2.\"Юджин, лайки в профиле\": пришлю суммарное количество лайков " +
                    "под последними 100 фото в профиле" +heart+"\n"+
                    "3.\"Юджин, всего лайков\": пришлю суммарное количество лайков " +
                    "под последними 100 записями" +heart+"\n"+
                    "4.\"Юджин, курс биткоина\": курс биткоина в долларах" +bot.getEmojies().get("dollar")+"\n" +
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода <город (в именительном падеже)> <код страны>\"."+
                    "Пример: \"Погода Москва ру\" или \"Погода Moscow ru\""+
                    bot.getEmojies().get("thermometer")+"\n" +
                    "6.\"Юджин, пошли меня\": могу послать"+bot.getEmojies().get("fuck")+"\n"+
                    "Или можем просто поболтать, но я еще учусь, и мои ответы могут быть не совсем точными\n"+
                    "Знаю, пока это немного, но я развиваюсь:)");
        } else if (data.matches("likesonwall")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+bot.getEmojies().get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.matches("likesonprofile")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+bot.getEmojies().get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.matches("totallikes")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+bot.getEmojies().get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.matches("btrate")){
            String[] btrate=bot.bitcoinRate();
            String dollar=bot.getEmojies().get("dollar");
            bot.sendMessage(addressee.getId(),"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if (data.matches("fuckoff")){
            bot.sendMessage(addressee.getId(),addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"
                    +bot.getEmojies().get("fuck"));
        } else if(data.matches("forecast.*")){
            String emojiLine=bot.getEmojies().get("thermometer")+bot.getEmojies().get("sun")
                    +bot.getEmojies().get("clWithLight")+bot.getEmojies().get("lightning")
                    +bot.getEmojies().get("snowflake")+bot.getEmojies().get("cloud")
                    +bot.getEmojies().get("clWithSnow")+bot.getEmojies().get("clWithRain")
                    +bot.getEmojies().get("clWithSun")+bot.getEmojies().get("sunWithCl")
                    +bot.getEmojies().get("sunWithClWithR");
            String[] input=data.split(" ");
            String result=input.length==3?bot.receiveWeatherForecast(input[1],input[2]):"";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода <город (в именительном падеже)> <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        } else if(data.matches("ai.*")){
            String[] input=data.split(": ");
            String response=input.length==2?aiAnswer(input[1]):"Not correct command.";
            bot.sendMessage(addressee.getId(),response.equals("")?"Извини, я тебя не понял.":response);
        }else if (data.matches("ignore")){
            bot.ignore(addressee.getId());
        } else if (data.matches("unignore")){
            bot.unignore(addressee.getId());
        } else if (data.matches("list")){
            bot.sendMessage(addressee.getId(),"=============List=============\n"+
                    "/greeting\n/likesOnWall\n/likesOnProfile\n/totalLikes\n/btRate\n/fuckOff\n" +
                    "/forecast <city> <country code>\n/ai: <query>\n/ignore\n/unignore");
        }
    }
    private String aiAnswer(String input){
        String result="";
        try {
            AIRequest request = new AIRequest(input);
            AIResponse response = bot.getDataService().request(request);
            if(response.getStatus().getCode()==200) result=response.getResult().getFulfillment().getSpeech();
        } catch (AIServiceException e) {
            logger.info("AI Service Exception when ai answering");
        } finally {
            return result;
        }
    }
}
