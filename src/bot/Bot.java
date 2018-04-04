package bot;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import bot.handler.LongPollHandler;
import bot.handler.MessageReplier;
import bot.tasks.*;
import bot.utils.Pair;
import com.google.maps.GeoApiContext;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class of the program.
 */
public class Bot {

    /**
     * Configuration fields.
     * @see Bot#loadConfig()
     * @see Bot#initBot()
     */
    private String AI_CLIENT_KEY, GOOGLE_KEY,ACCESS_TOKEN, WEATHER_KEY;
    private int USER_ID_MAIN;
    private int[] MEME_RESOURCES;

    /**
     * Service fields.
     * @see Bot#initEmojies()
     * @see Bot#initAi()
     * @see Bot#initLongPollHandler(int)
     */
    public static final Logger logger= LoggerFactory.getLogger(Bot.class);
    private Map<String,String> emojies;
    private AIDataService dataService;
    private LongPollHandler handler;

    /**
     * Tasks for bot.
     * @see Bot#initTasks(VkApiClient, UserActor)
     */
    private MainApiInteracter interacter;
    private LikesCounter counter;
    private BitcoinRate bitcoinRate;
    private WeatherForecast forecast;
    private DistanceCounter distanceCounter;
    private RandomImage randomImage;
    private RandomVkItem randomItem;
    private TextConverter converter;
    private LinkedHashMap<Integer,GuessNumber> guessGame;

    /**
     * Set of ignored users.
     * @see Bot#ignore(int)
     * @see Bot#unignore(int)
     * @see Bot#isIgnored(int)
     */
    private HashSet<Integer> ignored;

    /**
     * User status before bot`s launch.
     * @see Bot#initBot()
     * @see Bot#addShutdownHook()
     */
    private String userStatus;

    /**
     * Control count of interactions with Vk Api.
     * @see Bot#startCheckThread()
     * @see Bot#check()
     */
    private AtomicInteger countOfInteractions;

    public Bot() {
        loadConfig();
        initBot();
    }

    public static void main(String[] args) {
        new Bot();
    }

    /**
     * Load configurations from file.
     */
    private void loadConfig(){
        try {
            Properties properties=new Properties();
            properties.load(Bot.class.getClassLoader().getResourceAsStream("config.properties"));
            AI_CLIENT_KEY =properties.getProperty("ai-client-key");
            WEATHER_KEY =properties.getProperty("weather-key");
            GOOGLE_KEY =properties.getProperty("google-key");
            ACCESS_TOKEN=properties.getProperty("access-token");
            USER_ID_MAIN=Integer.valueOf(properties.getProperty("user-id-main"));
            MEME_RESOURCES=new int[]{Integer.valueOf(properties.getProperty("meme-4ch")),
                    Integer.valueOf(properties.getProperty("meme-mdk")),
                    Integer.valueOf(properties.getProperty("meme-9gag"))};
        } catch (IOException e) {
            logger.error("Initialize error (can`t load properties).");
        }
    }


    /**
     * Init methods on start of application.
     */
    private void initBot(){
        ignored=new HashSet<>();
        guessGame=new LinkedHashMap<>();
        countOfInteractions=new AtomicInteger(0);
        VkApiClient vk=new VkApiClient(new HttpTransportClient());
        UserActor user =new UserActor(USER_ID_MAIN,ACCESS_TOKEN);
        initTasks(vk,user);
        initAi();
        initEmojies();
        initLongPollHandler(user.getId());
        userStatus=interacter.getStatus();
        addShutdownHook();
        startCheckThread();
        interacter.setStatus("VkBot is working now (there is no human user).");
        interacter.setOnline(true);
        interacter.sendMessageToOwner("VkBot has been started on:\nserverTime["+new Date().toString()+"]\nHello!");
        System.out.println("\n╔╗╔╦═══╦╗─╔╗─╔══╦╗\n" +
                "║║║║╔══╣║─║║─║╔╗║║\n" +
                "║╚╝║╚══╣║─║║─║║║║║\n" +
                "║╔╗║╔══╣║─║║─║║║╠╝\n" +
                "║║║║╚══╣╚═╣╚═╣╚╝╠╗\n" +
                "╚╝╚╩═══╩══╩══╩══╩╝");
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
        emojies.put("photo","&#127750;");
        emojies.put("mail","&#128236;");
        emojies.put("camera","&#128249;");
        emojies.put("exclamation","&#10071;");
    }
    private void initTasks(VkApiClient vk,UserActor user){
        interacter =new MainApiInteracter(user,vk);
        counter=new LikesCounter(user,vk);
        bitcoinRate =new BitcoinRate();
        forecast=new WeatherForecast(WEATHER_KEY);
        distanceCounter=new DistanceCounter(new GeoApiContext.Builder()
                .apiKey(GOOGLE_KEY)
                .build());
        randomImage=new RandomImage();
        randomItem =new RandomVkItem(user,vk,MEME_RESOURCES);
        converter=new TextConverter();
    }
    private void initAi(){
        dataService=new AIDataService(new AIConfiguration(AI_CLIENT_KEY));
    }


    private void initLongPollHandler(int userId){
        handler=new LongPollHandler(this,userId,new MessageReplier(this,emojies));
        handler.start();
    }


    /**
     * Message send methods.
     */
    public void sendMessage(int id, String text){
       check();
       interacter.sendMessage(id, text);
    }
    public void sendMessageWithPhoto(int id, String text, File photo){
        check();
        interacter.sendMessageWithPhoto(id, text, photo);
    }
    public void sendMessageWithPhoto(int id, String text,String...photo){
        check();
        interacter.sendMessageWithPhoto(id, text, photo);
    }
    public void sendMessageWithVideo(int id,String text,String video){
        check();
        interacter.sendMessageWithVideo(id, text, video);
    }

    /**
     * Tasks methods.
     */
    public int calculateCountOfLikes(UserXtrCounters target,String albumId){
        check();
        return counter.calculateCountOfLikes(target, albumId);
    }
    public int calculateContOfLikesOnPosts(UserXtrCounters target){
        check();
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
    public File randomImage() {
        return randomImage.randomImage();
    }
    public Pair<String, String[]> randomMeme(){
        check();
        Pair<String,String[]> pair=randomItem.randomMeme();
        pair=pair.getValue().length>0?pair:randomMeme();
        return pair;
    }
    public String randomVideo(){
        check();
        return randomItem.randomVideo();
    }
    public String textToEmoji(char[] text,String background,String foreground){
        return converter.textToEmoji(text, background, foreground);
    }

    /**
     * Game methods.
     */
    public synchronized boolean isPlaying(int id){
        return guessGame.containsKey(id);
    }
    public synchronized void startNewGame(int id){
        guessGame.put(id,new GuessNumber());
    }
    public synchronized void endGame(int id){
        guessGame.remove(id);
    }
    public synchronized boolean checkStatement(int id,char operation,int input){
        return guessGame.get(id).checkStatement(operation, input);
    }
    public synchronized boolean checkNumber(int id,int input){
        return guessGame.get(id).checkNumber(input);
    }
    public synchronized int countOfTryings(int id){
        return guessGame.get(id).countOfTryings();
    }

    /**
     * Chat-bot answer.
     * @param input user says
     * @return chat-bot answer
     */
    public String aiAnswer(String input){
        String result="";
        try {
            AIRequest request = new AIRequest(input);
            AIResponse response = dataService.request(request);
            if(response.getStatus().getCode()==200) result=response.getResult().getFulfillment().getSpeech();
        } catch (AIServiceException e) {
            logger.info("AI Service Exception when ai answering.");
        } finally {
            return result;
        }
    }

    /**
     * Service functions.
     */
    public synchronized void interruptLongPoll(){
        handler.setShouldReact(false);
        System.out.println("Long Poll interrupted.");
    }
    public synchronized void startLongPoll(){
        handler.setShouldReact(true);
        System.out.println("Long Poll started.");
    }
    public synchronized void ignore(int id){
        ignored.add(id);
    }
    public synchronized void unignore(int id){
        ignored.remove(id);
    }
    public synchronized boolean isIgnored(int id){
        return ignored.contains(id);
    }

    /**
     * Get methods.
     */
    public LongpollParams getLongpollParams(){
        check();
        return interacter.getLongpollParams();
    }
    public UserXtrCounters getSender(String id){
        check();
        return interacter.getSender(id);
    }

    /**
     * Shutdown program.
     * @param status exit status
     */
    public void exit(int status){
        System.exit(status);
    }

    /**
     * Operations on shutdown.
     */
    private void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(()->{

            interacter.sendMessageToOwner("VkBot has been exited on:\nserverTime["
                    + new Date().toString() + "]\nBye-bye!");
            interacter.setStatus(userStatus);
            interacter.setOnline(false);
            System.out.println("╔══╗╔╗╔╦═══╗──╔══╗╔╗╔╦═══╦╗\n" +
                    "║╔╗║║║║║╔══╝──║╔╗║║║║║╔══╣║\n" +
                    "║╚╝╚╣╚╝║╚══╦══╣╚╝╚╣╚╝║╚══╣║\n" +
                    "║╔═╗╠═╗║╔══╩══╣╔═╗╠═╗║╔══╩╝\n" +
                    "║╚═╝║╔╝║╚══╗──║╚═╝║╔╝║╚══╦╗\n" +
                    "╚═══╝╚═╩═══╝──╚═══╝╚═╩═══╩╝\n");

        }));
    }

    /**
     * Thread for control count of interactions with Vk Api.
     */
    private void startCheckThread(){
        Thread thread=new Thread(()->{
            while (Thread.currentThread().isAlive()) {
                try {
                    Thread.sleep(1000);
                    countOfInteractions.set(0);
                } catch (InterruptedException ignore) {}
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Check count of interactions with Vk Api.
     */
    private void check(){
        while (countOfInteractions.get()>=3) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        countOfInteractions.getAndIncrement();
    }

}
