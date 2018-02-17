package bot;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import bot.console.ConsoleHandler;
import bot.handler.LongPollHandler;
import bot.handler.MessageReplier;
import bot.tasks.BitcoinRate;
import bot.tasks.LikesCounter;
import bot.tasks.MessageSender;
import bot.tasks.WeatherForecast;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;



public class Bot {
    private static String CODE;
    private static final int CLIENT_ID =6358381;
    private static final String CLIENT_SECRET="iXDTP9oKQ36hzTUEFdT0",AI_CLIENT="77d5f2c00a21490b897ad42fd6307903",
        REDIRECTED_URI="http://vk.com/blank.html";
    public static final String APP_WEATHER_ID="544cde73017001c30cab9b8b7d18a416";
    private UserActor user;
    private VkApiClient vk;
    public static final Logger logger= LoggerFactory.getLogger(Bot.class);
    private BufferedReader reader;
    private Map<String,String> emojies;
    private MessageSender sender;
    private LikesCounter counter;
    private AIDataService dataService;
    private LongPollHandler handler;
    private BitcoinRate bitcoinRate;
    private WeatherForecast forecast;
    private HashSet<Integer> ignored;

    public Bot() {
        ignored=new HashSet<>();
        initBot();
        initLongPollServer();
        initAi();
        initEmojies();
        initTasks();
        initConsoleHandler();
    }

    public static void main(String[] args) {
        new Bot();
    }
    private void initEmojies(){
        emojies=new HashMap<>();
        emojies.put("cuteSmile","&#128522;");
        emojies.put("laughingSmile","&#128514;");
        emojies.put("coolEmoji","&#128526;");
        emojies.put("angryEmoji","&#128545;");
        emojies.put("heart","&#10084;");
        emojies.put("dollar","&#128181;");
        emojies.put("fuck","&#128405;");
        emojies.put("sun","&#9728;");
        emojies.put("clWithLight","&#9928;");
        emojies.put("snowflake","&#10052;");
        emojies.put("cloud","&#9729;");
        emojies.put("clWithSnow","&#127784;");
        emojies.put("clWithRain","&#127783;");
        emojies.put("clWithSun","&#9925;");
        emojies.put("thermometer","&#127777;");
        emojies.put("sunWithCl","&#127780;");
        emojies.put("sunWithClWithR","&#127782;");
        emojies.put("lightning","&#127785;");
    }
    private void initTasks(){
        sender=new MessageSender(user,vk);
        counter=new LikesCounter(user,vk);
        bitcoinRate =new BitcoinRate();
        forecast=new WeatherForecast();
    }
    private void initAi(){
        dataService=new AIDataService(new AIConfiguration(AI_CLIENT));
    }
    private void initBot(){
        vk=new VkApiClient(new HttpTransportClient());
        reader=new BufferedReader(new InputStreamReader(System.in));
        try {
            Desktop.getDesktop().browse(URI.create("https://oauth.vk.com/authorize?client_id="+ CLIENT_ID +"&" +
                    "display=page&" +
                    "redirect_uri="+REDIRECTED_URI+"&scope=friends,messages&response_type=code&v=5.11"));
            CODE =reader.readLine();
            UserAuthResponse response=vk.oauth()
                    .userAuthorizationCodeFlow(CLIENT_ID,CLIENT_SECRET,REDIRECTED_URI, CODE)
                    .execute();
            user =new UserActor(response.getUserId(),response.getAccessToken());
            System.out.println("============VKBot version 1.0 has been started============\n" +
                    "                Print 'exit' to exit" + "\n==========================================================");

        } catch (ApiException e) {
            logger.error("Api Exception on start");
        } catch (ClientException e) {
            logger.error("Client Exception on start");
        } catch (IOException e) {
            logger.error("IO Exception on start");
        }
    }
    private void initLongPollServer(){
        handler=new LongPollHandler(this,new MessageReplier(this));
        handler.start();
    }
    private void initConsoleHandler(){
        new ConsoleHandler(this,reader).monitor();
    }

    public synchronized void sendMessage(int id, String text){
       sender.sendMessage(id, text);
    }
    public int calculateCountOfLikes(UserXtrCounters target,String albumId){
        return counter.calculateCountOfLikes(target, albumId);
    }
    public int calculateContOfLikesOnPosts(UserXtrCounters target){
        return counter.calculateContOfLikesOnPosts(target);
    }
    public String[] bitcoinRate(){
        return bitcoinRate.bitcoinRate();
    }
    public String receiveWeatherForecast(String city,String countryCode){
        return forecast.receiveWeatherForecast(city, countryCode);
    }
    public void interruptLongPoll(){
        handler.setShouldReact(false);
        System.out.println("Long Poll interrupted");
    }
    public void startLongPoll(){
        handler.setShouldReact(true);
        System.out.println("Long Poll started");
    }
    public LongpollParams getLongpollParams() throws ClientException, ApiException {
        return vk.messages()
                .getLongPollServer(user)
                .execute();
    }
    public UserXtrCounters getAddressee(String id) throws ClientException, ApiException {
        return  vk.users().get(user)
                .userIds(id)
                .execute()
                .get(0);
    }

    public void ignore(int id){
        ignored.add(id);
    }
    public void unignore(int id){
        ignored.remove(id);
    }
    public boolean isIgnored(int id){
        return ignored.contains(id);
    }
    public Map<String, String> getEmojies() {
        return emojies;
    }

    public AIDataService getDataService() {
        return dataService;
    }
}
