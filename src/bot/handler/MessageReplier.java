package bot.handler;


import bot.Bot;
import bot.utils.Pair;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.io.File;
import java.util.Map;

public class MessageReplier {
    private Bot bot;
    private Map<String, String> emojies;

    public MessageReplier(Bot bot, Map<String,String> emojies) {
        this.bot = bot;
        this.emojies=emojies;
    }

    /**
     * Parse and reply to message from user.
     * @param message user message
     * @param addressee user who send message
     * Functions:
     * @see Bot#sendMessage(int, String)
     * @see Bot#sendMessageWithPhoto(int, String, File)
     * @see Bot#sendMessageWithPhoto(int, String, String...)
     * @see Bot#sendMessageWithVideo(int, String, String)
     * @see Bot#calculateCountOfLikes(UserXtrCounters, String)
     * @see Bot#calculateContOfLikesOnPosts(UserXtrCounters)
     * @see Bot#bitcoinRate()
     * @see Bot#startNewGame(int)
     * @see Bot#randomImage()
     * @see Bot#randomMeme()
     * @see Bot#randomVideo()
     * @see Bot#textToEmoji(char[], String, String)
     * @see Bot#receiveWeatherForecast(String, String)
     * @see Bot#calculateTimeInSubway(String, String, String)
     * @see Bot#aiAnswer(String)
     */
    public void parse(String message, UserXtrCounters addressee){
        String data=message.toLowerCase();
        int id = addressee.getId();
        if(data.matches("здравствуй.*|привет.*")){
            String heart= emojies.get("heart");
            bot.sendMessage(id,"Привет, я временно заменяю хозяина этой страницы.\n" +
                    "И не зови меня бот. Зови меня Юджин"+ emojies.get("coolEmoji")+"\n\n" +
                    "Вот что я пока могу:\n\n" +
                    "1.\"Лайки на стене\": пришлю суммарное количество лайков " +
                    "под последними 100 фото со стены" +heart+"\n\n"+
                    "2.\"Лайки в профиле\": пришлю суммарное количество лайков " +
                    "под последними 100 фото в профиле" +heart+"\n\n"+
                    "3.\"Всего лайков\": пришлю суммарное количество лайков " +
                    "под последними 100 записями" +heart+"\n\n"+
                    "4.\"Курс биткоина\": курс биткоина в долларах" + emojies.get("dollar")+"\n\n" +
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода: <город (в именительном падеже)>, <код страны>\"."+
                    "Пример: \"Погода: Москва, ру\" или \"Погода: Moscow, ru\""+
                    emojies.get("thermometer")+"\n\n" +
                    "6.\"Поиграем\": я загадаю число от 0 до 100, а тебе нужно" +
                    " будет угадать, пользуясь тремя командами: \">(число)\" больше, \"<(число)\" меньше" +
                    ", \"(число)\".\n\n"+
                    "7. Покажу время пути в метро. Синтаксис запроса: \"Метро: <город>, <начальная станция>, " +
                    "<конечная станция>\""+ emojies.get("subway")+"\n\n"+
                    "8.\"Случайное фото\": пришлю случайное фото"+ emojies.get("photo")+"\n\n"+
                    "9.\"Скинь мем\": скину случайный мем (из новых)"+ emojies.get("mail")+"\n\n"+
                    "10.\"Скинь видео\": скину случайное видео"+ emojies.get("camera")+"\n\n"+
                    "11.Напишу текст эмодзи. Синтаксис запроса:\"Эмодзи: <текст>, <фоновый эмодзи>, " +
                    "<основной эмодзи>\".\n"+emojies.get("exclamation")+emojies.get("exclamation")+
                    emojies.get("exclamation")+"Двойные эмодзи могут вызывать проблемы. Также стоит обратить" +
                    " внимание на длину сообщения, т.к. в ВК есть ограничение на нее"
                    +emojies.get("exclamation") +emojies.get("exclamation")+emojies.get("exclamation")+"\n\n"+
                    "12.\"Пошли меня\": могу послать"+ emojies.get("fuck")+"\n\n"+
                    "Или можем просто поболтать, но я еще учусь, и мои ответы могут быть не совсем точными.\n"+
                    "Знаю, пока это немного, но я развиваюсь"+ emojies.get("cuteSmile"));
        } else if (data.matches("лайки на стене.*|1")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+ emojies.get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.matches("лайки в профиле.*|2")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+ emojies.get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.matches("всего лайков.*|3")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+ emojies.get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.matches("курс биткоина.*|4")){
            String[] btrate=bot.bitcoinRate();
            String dollar= emojies.get("dollar");
            bot.sendMessage(id,"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if(data.matches("поиграем.*|6")){
            bot.startNewGame(id);
            bot.sendMessage(id,"Число загадано и игра началась!");
        }else if(data.matches("случайное фото.*|8")){
            bot.sendMessage(id,"Минутку...");
            bot.sendMessageWithPhoto(id,"Держи!",bot.randomImage());
        }else if(data.matches("скинь мем.*|9")){
            bot.sendMessage(id,"Минутку...");
            Pair<String,String[]> meme=bot.randomMeme();
            bot.sendMessageWithPhoto(id,meme.getKey(),meme.getValue());
        }else if(data.matches("скинь видео.*|10")){
            bot.sendMessage(id,"Минутку...");
            bot.sendMessageWithVideo(id,"",bot.randomVideo());
        }else if(data.matches("эмодзи.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=parameters.length==3?bot.textToEmoji(parameters[0].toCharArray(),
                    parameters[1].trim(),parameters[2].trim()):"";
            bot.sendMessage(id,result.equals("")?"Я не могу написать это с помощью эмодзи. " +
                    "Попробуй что-нибудь другое.":result);
        }else if (data.matches("пошли меня.*|12")){
            bot.sendMessage(id,"Я, конечно, культурный бот, но раз ты просишь...\n" +
                    addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"+ emojies.get("fuck"));
        } else if(data.matches("погода.*")){
            String emojiLine= emojies.get("thermometer")+ emojies.get("sun")
                    + emojies.get("clWithLight")+ emojies.get("lightning")
                    + emojies.get("snowflake")+ emojies.get("cloud")
                    + emojies.get("clWithSnow")+ emojies.get("clWithRain")
                    + emojies.get("clWithSun")+ emojies.get("sunWithCl")
                    + emojies.get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=input.length==2?bot.receiveWeatherForecast(input[0].trim(),input[1].trim()):"";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        } else if (data.matches("метро.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0].trim(),
                    parameters[1].trim(),parameters[2].trim()): "";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+ emojies
                    .get("watch"));
        }else{
            String response=bot.aiAnswer(message);
            bot.sendMessage(id,response.equals("")?"Извини, я тебя не понял.":response);
        }
    }

    /**
     * If user is playing game for parse use this method.
     * @param message user message
     * @param addressee user who send message
     * Functions:
     * @see Bot#startNewGame(int)
     * @see Bot#endGame(int)
     */
    public void parseGame(String message, UserXtrCounters addressee){
        String data=message.toLowerCase();
        int id = addressee.getId();
        if (data.equals("хватит")) {
            bot.endGame(id);
            bot.sendMessage(id,"Спасибо за игру!");
        } else if(data.equals("заново")){
            bot.startNewGame(id);
            bot.sendMessage(id,"Число загадано и игра началась!");
        } else if(data.matches("&gt;\\d*")){
            try {
                boolean isCorrect=bot.checkStatement(id, '>',Integer.valueOf(data.substring(4)));
                bot.sendMessage(id,isCorrect?"Да":"Нет");
            } catch (NumberFormatException e) {
                bot.sendMessage(id,"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        } else if(data.matches("&lt;\\d*")){
            try {
                boolean isCorrect=bot.checkStatement(id, '<',Integer.valueOf(data.substring(4)));
                bot.sendMessage(id,isCorrect?"Да":"Нет");
            } catch (NumberFormatException e) {
                bot.sendMessage(id,"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        } else{
            try {
                boolean isFemale= addressee.getSex() != null && addressee.getSex().getValue() == 1;
                boolean isCorrect=bot.checkNumber(id,Integer.valueOf(data));
                bot.sendMessage(id,isCorrect?"Ура! Ты "+
                        (isFemale?"угадала":"угадал")+" за "
                        +bot.countOfTryings(id)+" попыток!" +
                        " Спасибо за игру.":"Нет");
                if (isCorrect) bot.endGame(id);
            } catch (NumberFormatException e) {
                bot.sendMessage(id,"Некорректный ввод. Ввод должен быть" +
                        " в формате \"<операция><число>\" или \"<число>\"");
            }
        }
    }

    /**
     * Parse and reply to output message in chat
     * (which send user who have launched the app).
     * @param message user message
     * @param addressee user who send message
     * Functions:
     * @see Bot#sendMessage(int, String)
     * @see Bot#sendMessageWithPhoto(int, String, File)
     * @see Bot#sendMessageWithPhoto(int, String, String...)
     * @see Bot#sendMessageWithVideo(int, String, String)
     * @see Bot#calculateCountOfLikes(UserXtrCounters, String)
     * @see Bot#calculateContOfLikesOnPosts(UserXtrCounters)
     * @see Bot#bitcoinRate()
     * @see Bot#startNewGame(int)
     * @see Bot#randomImage()
     * @see Bot#randomMeme()
     * @see Bot#randomVideo()
     * @see Bot#textToEmoji(char[], String, String)
     * @see Bot#receiveWeatherForecast(String, String)
     * @see Bot#calculateTimeInSubway(String, String, String)
     * @see Bot#aiAnswer(String)
     */
    public void parseUser(String message, UserXtrCounters addressee){
        if(message.length()<=0||message.toCharArray()[0]!='/') return;
        String data=message.toLowerCase().substring(1);
        int id = addressee.getId();
        if(data.equals("greeting")){
            String heart= emojies.get("heart");
            bot.sendMessage(id,"Привет, я временно заменяю хозяина этой страницы.\n" +
                    "И не зови меня бот. Зови меня Юджин"+ emojies.get("coolEmoji")+"\n\n" +
                    "Вот что я пока могу:\n\n" +
                    "1.\"Лайки на стене\": пришлю суммарное количество лайков " +
                    "под последними 100 фото со стены" +heart+"\n\n"+
                    "2.\"Лайки в профиле\": пришлю суммарное количество лайков " +
                    "под последними 100 фото в профиле" +heart+"\n\n"+
                    "3.\"Всего лайков\": пришлю суммарное количество лайков " +
                    "под последними 100 записями" +heart+"\n\n"+
                    "4.\"Курс биткоина\": курс биткоина в долларах" + emojies.get("dollar")+"\n\n" +
                    "5.Прогноз погоды. Синтаксис запроса: \"Погода: <город (в именительном падеже)>, <код страны>\"."+
                    "Пример: \"Погода: Москва, ру\" или \"Погода: Moscow, ru\""+
                    emojies.get("thermometer")+"\n\n" +
                    "6.\"Поиграем\": я загадаю число от 0 до 100, а тебе нужно" +
                    " будет угадать, пользуясь тремя командами: \">(число)\" больше, \"<(число)\" меньше" +
                    ", \"(число)\".\n\n"+
                    "7. Покажу время пути в метро. Синтаксис запроса: \"Метро: <город>, <начальная станция>, " +
                    "<конечная станция>\""+ emojies.get("subway")+"\n\n"+
                    "8.\"Случайное фото\": пришлю случайное фото"+ emojies.get("photo")+"\n\n"+
                    "9.\"Скинь мем\": скину случайный мем (из новых)"+ emojies.get("mail")+"\n\n"+
                    "10.\"Скинь видео\": скину случайное видео"+ emojies.get("camera")+"\n\n"+
                    "11.Напишу текст эмодзи. Синтаксис запроса:\"Эмодзи: <текст>, <фоновый эмодзи>, " +
                    "<основной эмодзи>\".\n"+emojies.get("exclamation")+emojies.get("exclamation")+
                    emojies.get("exclamation")+"Двойные эмодзи могут вызывать проблемы. Также стоит обратить" +
                    " внимание на длину сообщения, т.к. в ВК есть ограничение на нее"
                    +emojies.get("exclamation") +emojies.get("exclamation")+emojies.get("exclamation")+"\n\n"+
                    "12.\"Пошли меня\": могу послать"+ emojies.get("fuck")+"\n\n"+
                    "Или можем просто поболтать, но я еще учусь, и мои ответы могут быть не совсем точными.\n"+
                    "Знаю, пока это немного, но я развиваюсь"+ emojies.get("cuteSmile"));
        } else if (data.equals("likesonwall")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+ emojies.get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.equals("likesonprofile")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+ emojies.get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.equals("totallikes")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+ emojies.get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.equals("btrate")){
            String[] btrate=bot.bitcoinRate();
            String dollar= emojies.get("dollar");
            bot.sendMessage(id,"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if(data.equals("randomphoto")){
            bot.sendMessage(id,"Минутку...");
            bot.sendMessageWithPhoto(id,"Держи!",bot.randomImage());
        }else if(data.equals("randommeme")){
            bot.sendMessage(id,"Минутку...");
            Pair<String,String[]> meme=bot.randomMeme();
            bot.sendMessageWithPhoto(id,meme.getKey(),meme.getValue());
        }else if(data.equals("randomvideo")){
            bot.sendMessage(id,"Минутку...");
            bot.sendMessageWithVideo(id,"",bot.randomVideo());
        }else if (data.equals("fuckoff")){
            bot.sendMessage(id,addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"
                    + emojies.get("fuck"));
        } else if(data.matches("forecast.*")){
            String emojiLine= emojies.get("thermometer")+ emojies.get("sun")
                    + emojies.get("clWithLight")+ emojies.get("lightning")
                    + emojies.get("snowflake")+ emojies.get("cloud")
                    + emojies.get("clWithSnow")+ emojies.get("clWithRain")
                    + emojies.get("clWithSun")+ emojies.get("sunWithCl")
                    + emojies.get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=input.length==2?bot.receiveWeatherForecast(input[0].trim(),input[1].trim()):"";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        }else if(data.matches("emoji.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=parameters.length==3?bot.textToEmoji(parameters[0].toCharArray(),
                    parameters[1].trim(),parameters[2].trim()):"";
            bot.sendMessage(id,result.equals("")?"Я не могу написать это с помощью эмодзи. " +
                    "Попробуй что-нибудь другое.":result);
        }else if (data.matches("subway.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0].trim(),
                    parameters[1].trim(),parameters[2].trim()):"";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+ emojies
                    .get("watch"));
        }else if(data.matches("ai.*")){
            String[] input=data.split(": ");
            String response=input.length==2?bot.aiAnswer(input[1]):"Not correct command.";
            bot.sendMessage(id,response.equals("")?"Извини, я тебя не понял.":response);
        }else if (data.equals("ignore")){
            bot.ignore(id);
        } else if (data.equals("unignore")){
            bot.unignore(id);
        } else if (data.equals("list")){
            bot.sendMessage(id,"=============List=============\n"+
                    "/greeting\n/likesOnWall\n/likesOnProfile\n/totalLikes\n/btRate\n/fuckOff\n" +
                    "/forecast: <city>, <country code>\n/ai: <query>\n/ignore\n/unignore\n" +
                    "/subway: <city>, <origin>, <destination>\n/randomPhoto\n/randomMeme\n" +
                    "/randomVideo");
        }
    }

    /**
     * Parse user message to himself.
     * @param message user message
     * @param addressee user who send message
     * Functions:
     * @see Bot#sendMessage(int, String)
     * @see Bot#calculateCountOfLikes(UserXtrCounters, String)
     * @see Bot#calculateContOfLikesOnPosts(UserXtrCounters)
     * @see Bot#bitcoinRate()
     * @see Bot#receiveWeatherForecast(String, String)
     * @see Bot#calculateTimeInSubway(String, String, String)
     * @see Bot#aiAnswer(String)
     * @see Bot#interruptLongPoll()
     * @see Bot#startLongPoll()
     * @see Bot#exit(int)
     */
    public void parseAdmin(String message, UserXtrCounters addressee){
        if(message.length()<=1||message.toCharArray()[0]!='/') return;
        String data=message.toLowerCase().substring(1);
        int id = addressee.getId();
        if (data.equals("likesonwall")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"wall")+ emojies.get("heart")+" лайков под " +
                    "фотографиями на стене.");
        } else if (data.equals("likesonprofile")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateCountOfLikes(addressee,"profile")+ emojies.get("heart")+" лайков " +
                    "под фотографиями в профиле.");
        } else if (data.equals("totallikes")){
            bot.sendMessage(id,""+addressee.getFirstName()+" "+addressee.getLastName()+", " +
                    "у тебя "+bot.calculateContOfLikesOnPosts(addressee)+ emojies.get("heart")+" лайков " +
                    "под записями на стене.");
        } else if(data.equals("btrate")){
            String[] btrate=bot.bitcoinRate();
            String dollar= emojies.get("dollar");
            bot.sendMessage(id,"Курс: "+btrate[0]+dollar+"\n"+
                    "Покупка: "+btrate[1]+dollar+"\n"+
                    "Продажа: "+btrate[2]+dollar);
        }else if (data.equals("fuckoff")){
            bot.sendMessage(id,addressee.getFirstName()+" "+addressee.getLastName()+", иди нахер"
                    + emojies.get("fuck"));
        } else if(data.matches("forecast.*")){
            String emojiLine= emojies.get("thermometer")+ emojies.get("sun")
                    + emojies.get("clWithLight")+ emojies.get("lightning")
                    + emojies.get("snowflake")+ emojies.get("cloud")
                    + emojies.get("clWithSnow")+ emojies.get("clWithRain")
                    + emojies.get("clWithSun")+ emojies.get("sunWithCl")
                    + emojies.get("sunWithClWithR");
            String[] mas=data.split(": ?");
            String[] input=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=input.length==2?bot.receiveWeatherForecast(input[0].trim(),input[1].trim()):"";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод.\nВвод должен быть" +
                    " в формате: \"Погода: <город (в именительном падеже)>, <код страны>\"":emojiLine+
                    "\n"+"\n"+result+emojiLine);
        }else if (data.matches("subway.*")){
            String[] mas=data.split(": ?");
            String[] parameters=mas.length==2?mas[1].split(", ?"):new String[0];
            String result=parameters.length==3?bot.calculateTimeInSubway(parameters[0].trim(),
                    parameters[1].trim(),parameters[2].trim()):"";
            bot.sendMessage(id,result.equals("")?"Некорректный ввод. Ввод должен быть в" +
                    " формате: \"Метро: <город>, <начальная станция>, <конечная станция>\"":result+ emojies
                    .get("watch"));
        }else if(data.matches("ai.*")){
            String[] input=data.split(": ");
            String response=input.length==2?bot.aiAnswer(input[1]):"Not correct command.";
            bot.sendMessage(id,response.equals("")?"Извини, я тебя не понял.":response);
        }else if(data.equals("stop")){
            bot.interruptLongPoll();
            bot.sendMessage(id,"VkBot has been stopped.");
        } else if(data.equals("start")){
            bot.startLongPoll();
            bot.sendMessage(id,"VkBot has been continued.");
        } else if(data.equals("exit")){
            bot.exit(0);
        } else if (data.equals("list")){
            bot.sendMessage(id,"=============List=============\n"+
                    "/likesOnWall\n/likesOnProfile\n/totalLikes\n/btRate\n/fuckOff\n" +
                    "/forecast: <city>, <country code>\n/ai: <query>\n" +
                    "/subway: <city>, <origin>, <destination>\n/stop\n/start\n/exit (deprecated)");
        }
    }
}
