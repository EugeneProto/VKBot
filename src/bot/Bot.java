package bot;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import bot.console.ConsoleHandler;
import bot.handler.LongPollHandler;
import bot.handler.MessageReplier;
import bot.tasks.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;


public class Bot {
    private static String CODE;
    private static int CLIENT_ID;
    private static String CLIENT_SECRET,AI_CLIENT,DISTANCE_MATRIX,
        REDIRECTED_URI="http://vk.com/blank.html";
    public static String APP_WEATHER_ID;
    private UserActor user;
    private VkApiClient vk;
    public static final Logger logger= LoggerFactory.getLogger(Bot.class);
    private BufferedReader reader;
    private Map<String,String> emojies;
    private AIDataService dataService;
    private LongPollHandler handler;

    private MessageSender sender;
    private LikesCounter counter;
    private BitcoinRate bitcoinRate;
    private WeatherForecast forecast;
    private DistanceCounter distanceCounter;
    private LinkedHashMap<Integer,GuessNumber> guessGame;

    private HashSet<Integer> ignored;

    public Bot() {
        ignored=new HashSet<>();
        guessGame=new LinkedHashMap<>();
        initConfig();
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
    private void initConfig(){
        try {
            Properties properties=new Properties();
            properties.load(Bot.class.getClassLoader().getResourceAsStream("config.properties"));
            CLIENT_ID=Integer.valueOf((String) properties.get("client-id"));
            CLIENT_SECRET=(String) properties.get("client-secret");
            AI_CLIENT=(String)properties.get("ai-client");
            APP_WEATHER_ID=(String)properties.get("app-weather-id");
            DISTANCE_MATRIX=(String)properties.get("distance-matrix");
        } catch (IOException e) {
            logger.error("Initialize error (can`t load properties)");
        }
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
        emojies.put("subway","&#9410;");
        emojies.put("watch","&#8986;");
    }
    private void initTasks(){
        sender=new MessageSender(user,vk);
        counter=new LikesCounter(user,vk);
        bitcoinRate =new BitcoinRate();
        forecast=new WeatherForecast();
        distanceCounter=new DistanceCounter(new GeoApiContext.Builder()
                .apiKey(DISTANCE_MATRIX)
                .build());
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
    public String calculateTimeInSubway(String city,String origin,String destination){
        return distanceCounter.calculateTimeInSubway(city, origin, destination);
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
                .fields(UserField.SEX)
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
    public GuessNumber getGuessGame(int id){
        return guessGame.get(id);
    }
    public boolean isPlaying(int id){
        return guessGame.containsKey(id);
    }
    public void startNewGame(int id){
        guessGame.put(id,new GuessNumber());
    }
    public void endGame(int id){
        guessGame.remove(id);
    }

}
