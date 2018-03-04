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
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода: <город (в именительном падеже)>, <код страны>\"."+
                    "Пример: \"Погода: Москва, ру\" или \"Погода: Moscow, ru\""+
                    bot.getEmojies().get("thermometer")+"\n" +
                    "6.\"Юджин, поиграем\": я загадаю число от 0 до 100, а тебе нужно" +
                    " будет угадать, пользуясь тремя командами: \">(число)\" больше, \"<(число)\" меньше" +
                    ", \"(число)\".\n"+
                    "7. Покажу время пути в метро. Синтаксис запроса: \"Метро: <город>, <начальная станция>, " +
                    "<конечная станция>\""+bot.getEmojies().get("subway")+"\n"+
                    "8.\"Юджин, пошли меня\": могу послать"+bot.getEmojies().get("fuck")+"\n"+
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
        }else if(data.matches("юджин,? ?поиграем.*|6.*")){
            bot.startNewGame(addressee.getId());
            bot.sendMessage(addressee.getId(),"Число загадано и игра началась!");
        }else if (data.matches("юджин,? ?пошли меня.*|8.*")){
            bot.sendMessage(addressee.getId(),"Я, конечно, культурный бот, но раз ты просишь...\n" +
                    addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"+bot.getEmojies().get("fuck"));
        } else if(data.matches("погода.*")){
            String emojiLine=bot.getEmojies().get("thermometer")+bot.getEmojies().get("sun")
                    +bot.getEmojies().get("clWithLight")+bot.getEmojies().get("lightning")
                    +bot.getEmojies().get("snowflake")+bot.getEmojies().get("cloud")
                    +bot.getEmojies().get("clWithSnow")+bot.getEmojies().get("clWithRain")
                    +bot.getEmojies().get("clWithSun")+bot.getEmojies().get("sunWithCl")
                    +bot.getEmojies().get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=input.length==2?bot.receiveWeatherForecast(input[0],input[1]):"";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        } else if (data.matches("метро.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0],parameters[1],parameters[2]):
                    "";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+bot.getEmojies()
                    .get("watch"));
        }else{
            String response=aiAnswer(message);
            bot.sendMessage(addressee.getId(),response.equals("")?"Извини, я тебя не понял.":response);
        }
    }
    public void parseGame(String message, UserXtrCounters addressee){
        String data=message.toLowerCase();
        if (data.equals("хватит")) {
            bot.endGame(addressee.getId());
            bot.sendMessage(addressee.getId(),"Спасибо за игру!");
        } else if(data.equals("заново")){
            bot.startNewGame(addressee.getId());
            bot.sendMessage(addressee.getId(),"Число загадано и игра началась!");
        } else if(data.matches("&gt;\\d*")){
            try {
                boolean isCorrect=bot.getGuessGame(addressee.getId())
                        .checkStatement('>',Integer.valueOf(data.substring(4)));
                bot.sendMessage(addressee.getId(),isCorrect?"Да":"Нет");
            } catch (NumberFormatException e) {
                bot.sendMessage(addressee.getId(),"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        } else if(data.matches("&lt;\\d*")){
            try {
                boolean isCorrect=bot.getGuessGame(addressee.getId())
                        .checkStatement('<',Integer.valueOf(data.substring(4)));
                bot.sendMessage(addressee.getId(),isCorrect?"Да":"Нет");
            } catch (NumberFormatException e) {
                bot.sendMessage(addressee.getId(),"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        } else{
            try {
                boolean isFemale=addressee.getSex()!=null?addressee.getSex().getValue()==1:false;
                boolean isCorrect=bot.getGuessGame(addressee.getId())
                        .checkNumber(Integer.valueOf(data));
                bot.sendMessage(addressee.getId(),isCorrect?"Ура! Ты "+
                        (isFemale?"угадала":"угадал")+" за "
                        +bot.getGuessGame(addressee.getId()).getCountOfTryings()+" попыток!" +
                        " Спасибо за игру.":"Нет");
                if (isCorrect) bot.endGame(addressee.getId());
            } catch (NumberFormatException e) {
                bot.sendMessage(addressee.getId(),"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        }
    }
    public void parseUser(String message, UserXtrCounters addressee){
        if(message.toCharArray()[0]!='/') return;
        String data=message.toLowerCase().substring(1);
        if(data.equals("greeting")){
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
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода: <город (в именительном падеже)>, <код страны>\"."+
                    "Пример: \"Погода: Москва, ру\" или \"Погода: Moscow, ru\""+
                    bot.getEmojies().get("thermometer")+"\n" +
                    "6.\"Юджин, поиграем\": я загадаю число от 0 до 100, а тебе нужно" +
                    " будет угадать, пользуясь тремя командами: \">(число)\" больше, \"<(число)\" меньше" +
                    ", \"(число)\".\n"+
                    "7. Покажу время пути в метро. Синтаксис запроса: \"Метро: <город>, <начальная станция>, " +
                    "<конечная станция>\""+bot.getEmojies().get("subway")+"\n"+
                    "8.\"Юджин, пошли меня\": могу послать"+bot.getEmojies().get("fuck")+"\n"+
                    "Или можем просто поболтать, но я еще учусь, и мои ответы могут быть не совсем точными.\n"+
                    "Знаю, пока это немного, но я развиваюсь:)");
        } else if (data.equals("likesonwall")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+bot.getEmojies().get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.equals("likesonprofile")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+bot.getEmojies().get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.equals("totallikes")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+bot.getEmojies().get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.equals("btrate")){
            String[] btrate=bot.bitcoinRate();
            String dollar=bot.getEmojies().get("dollar");
            bot.sendMessage(addressee.getId(),"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if (data.equals("fuckoff")){
            bot.sendMessage(addressee.getId(),addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"
                    +bot.getEmojies().get("fuck"));
        } else if(data.matches("forecast.*")){
            String emojiLine=bot.getEmojies().get("thermometer")+bot.getEmojies().get("sun")
                    +bot.getEmojies().get("clWithLight")+bot.getEmojies().get("lightning")
                    +bot.getEmojies().get("snowflake")+bot.getEmojies().get("cloud")
                    +bot.getEmojies().get("clWithSnow")+bot.getEmojies().get("clWithRain")
                    +bot.getEmojies().get("clWithSun")+bot.getEmojies().get("sunWithCl")
                    +bot.getEmojies().get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=input.length==2?bot.receiveWeatherForecast(input[0],input[1]):"";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        }else if (data.matches("subway.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0],parameters[1],parameters[2]):
                    "";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+bot.getEmojies()
                    .get("watch"));
        }else if(data.matches("ai.*")){
            String[] input=data.split(": ");
            String response=input.length==2?aiAnswer(input[1]):"Not correct command.";
            bot.sendMessage(addressee.getId(),response.equals("")?"Извини, я тебя не понял.":response);
        }else if (data.equals("ignore")){
            bot.ignore(addressee.getId());
        } else if (data.equals("unignore")){
            bot.unignore(addressee.getId());
        } else if (data.equals("list")){
            bot.sendMessage(addressee.getId(),"=============List=============\n"+
                    "/greeting\n/likesOnWall\n/likesOnProfile\n/totalLikes\n/btRate\n/fuckOff\n" +
                    "/forecast: <city>, <country code>\n/ai: <query>\n/ignore\n/unignore\n" +
                    "/subway: <city>, <origin>, <destination>");
        }
    }
    public void parseAdmin(String message, UserXtrCounters addressee){
        if(message.toCharArray()[0]!='/') return;
        String data=message.toLowerCase().substring(1);
        if (data.equals("likesonwall")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+bot.getEmojies().get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.equals("likesonprofile")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+bot.getEmojies().get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.equals("totallikes")){
            bot.sendMessage(addressee.getId(),""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+bot.getEmojies().get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.equals("btrate")){
            String[] btrate=bot.bitcoinRate();
            String dollar=bot.getEmojies().get("dollar");
            bot.sendMessage(addressee.getId(),"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if (data.equals("fuckoff")){
            bot.sendMessage(addressee.getId(),addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"
                    +bot.getEmojies().get("fuck"));
        } else if(data.matches("forecast.*")){
            String emojiLine=bot.getEmojies().get("thermometer")+bot.getEmojies().get("sun")
                    +bot.getEmojies().get("clWithLight")+bot.getEmojies().get("lightning")
                    +bot.getEmojies().get("snowflake")+bot.getEmojies().get("cloud")
                    +bot.getEmojies().get("clWithSnow")+bot.getEmojies().get("clWithRain")
                    +bot.getEmojies().get("clWithSun")+bot.getEmojies().get("sunWithCl")
                    +bot.getEmojies().get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=input.length==2?bot.receiveWeatherForecast(input[0],input[1]):"";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        }else if (data.matches("subway.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[]{};
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0],parameters[1],parameters[2]):
                    "";
            bot.sendMessage(addressee.getId(),result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+bot.getEmojies()
                    .get("watch"));
        }else if(data.matches("ai.*")){
            String[] input=data.split(": ");
            String response=input.length==2?aiAnswer(input[1]):"Not correct command.";
            bot.sendMessage(addressee.getId(),response.equals("")?"Извини, я тебя не понял.":response);
        }else if(data.equals("stop")){
            bot.interruptLongPoll();
            bot.sendMessage(addressee.getId(),"VkBot has been stopped.");
        } else if(data.equals("start")){
            bot.startLongPoll();
            bot.sendMessage(addressee.getId(),"VkBot has been continued.");
        } else if(data.equals("exit")){
            bot.exit(0);
        } else if (data.equals("list")){
            bot.sendMessage(addressee.getId(),"=============List=============\n"+
                    "/likesOnWall\n/likesOnProfile\n/totalLikes\n/btRate\n/fuckOff\n" +
                    "/forecast: <city>, <country code>\n/ai: <query>\n" +
                    "/subway: <city>, <origin>, <destination>\n/stop\n/start\n/exit (deprecated)");
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
