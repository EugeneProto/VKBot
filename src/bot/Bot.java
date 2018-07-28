package bot;

import bot.handler.LongPollHandler;
import bot.handler.MessageReplier;
import bot.tasks.*;
import bot.utils.Pair;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.*;
import com.google.maps.GeoApiContext;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private String GOOGLE_KEY, GROUP_ACCESS_TOKEN,OWNER_ACCESS_TOKEN, WEATHER_KEY, PROJECT_ID;
    private int OWNER_ID,GROUP_ID;
    private int[] MEME_RESOURCES;

    /**
     * Service fields.
     * @see Bot#initEmojies()
     * @see Bot#initAi()
     * @see Bot#initLongPollHandler()
     */
    public static final Logger logger= LoggerFactory.getLogger(Bot.class);
    private Map<String,String> emojies;
    private SessionsClient sessionsClient;
    private SessionName session;
    private LongPollHandler handler;

    /**
     * Main functions, such as send message or post
     * something on wall.
     */
    private MainApiInteracter interacter;

    /**
     * Tasks for bot.
     * @see Bot#initMainInteracterAndTasks(GroupActor, UserActor, VkApiClient)
     */
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
        disableWarning();
        new Bot();
    }

    /**
     * Hack to hide Netty`s warn message (only for Java 9).
     */
    private static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Load configurations from file.
     */
    private void loadConfig(){
        try {
            Properties properties=new Properties();
            properties.load(Bot.class.getClassLoader().getResourceAsStream("config.properties"));

            PROJECT_ID=properties.getProperty("project-id");
            WEATHER_KEY =properties.getProperty("weather-key");
            GOOGLE_KEY =properties.getProperty("google-key");

            GROUP_ACCESS_TOKEN =properties.getProperty("group-access-token");
            GROUP_ID=Integer.valueOf(properties.getProperty("group-id"));

            OWNER_ACCESS_TOKEN=properties.getProperty("owner-access-token");
            OWNER_ID =Integer.valueOf(properties.getProperty("owner-id"));

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
        GroupActor group=new GroupActor(GROUP_ID, GROUP_ACCESS_TOKEN);
        UserActor owner=new UserActor(OWNER_ID,OWNER_ACCESS_TOKEN);
        initMainInteracterAndTasks(group,owner,vk);
        initAi();
        initEmojies();
        initLongPollHandler();
        addShutdownHook();
        startCheckThread();
        sendMessageToOwner("VkBot has been started on:\nserverTime["+new Date().toString()+"]\nHello!");
        System.out.println(
                "╔╗╔╦═══╦╗─╔╗─╔══╦╗\n" +
                "║║║║╔══╣║─║║─║╔╗║║\n" +
                "║╚╝║╚══╣║─║║─║║║║║\n" +
                "║╔╗║╔══╣║─║║─║║║╠╝\n" +
                "║║║║╚══╣╚═╣╚═╣╚╝╠╗\n" +
                "╚╝╚╩═══╩══╩══╩══╩╝\n");
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
    private void initMainInteracterAndTasks(GroupActor group, UserActor owner, VkApiClient vk){
        interacter =new MainApiInteracter(group, owner, vk);
        counter=new LikesCounter(owner,vk);
        bitcoinRate =new BitcoinRate();
        forecast=new WeatherForecast(WEATHER_KEY);
        distanceCounter=new DistanceCounter(new GeoApiContext.Builder()
                .apiKey(GOOGLE_KEY)
                .build());
        randomImage=new RandomImage();
        randomItem =new RandomVkItem(owner,vk,MEME_RESOURCES);
        converter=new TextConverter();
        new AutopostRandomPhotos(this);
    }
    private void initAi(){
        try {
            session=SessionName.of(PROJECT_ID,UUID.randomUUID().toString());
            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(ServiceAccountCredentials
                            .fromStream(Bot.class.getClassLoader().getResourceAsStream("VkBotCredentials.json"))))
                            .build();
            sessionsClient=SessionsClient.create(sessionsSettings);
        } catch (IOException e) {
            logger.error("IO Exception when initializing Sessions AI Client.");
        }

    }
    private void initLongPollHandler(){
        handler=new LongPollHandler(this,OWNER_ID,new MessageReplier(this,emojies));
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
    private void sendMessageToOwner(String text){
        check();
        interacter.sendMessageToOwner(text);
    }

    /**
     * Posting method.
     */
    public void postPhotoToWall(File photo){
        check();
        interacter.postPhotoToWall(photo);
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
    public boolean isPlaying(int id){
        synchronized (guessGame) {
            return guessGame.containsKey(id);
        }
    }
    public void startNewGame(int id){
        synchronized (guessGame) {
            guessGame.put(id,new GuessNumber());
        }
    }
    public void endGame(int id){
        synchronized (guessGame) {
            guessGame.remove(id);
        }
    }
    public boolean checkStatement(int id,char operation,int input){
        synchronized (guessGame) {
            return guessGame.get(id).checkStatement(operation, input);
        }
    }
    public boolean checkNumber(int id,int input){
        synchronized (guessGame) {
            return guessGame.get(id).checkNumber(input);
        }
    }
    public int countOfTryings(int id){
        synchronized (guessGame) {
            return guessGame.get(id).countOfTryings();
        }
    }

    /**
     * Chat-bot answer.
     * @param input user says
     * @return chat-bot answer
     */
    public String aiAnswer(String input){
        TextInput.Builder textInput = TextInput.newBuilder().setText(input).setLanguageCode("ru");
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
        DetectIntentResponse response = sessionsClient.detectIntent(session,queryInput);
        return response.getQueryResult().getFulfillmentText();
    }

    /**
     * Service functions.
     */
    public void interruptLongPoll(){
        synchronized (handler) {
            handler.setShouldReact(false);
            System.out.println("Long Poll interrupted.");
        }
    }
    public void startLongPoll(){
        synchronized (handler) {
            handler.setShouldReact(true);
            System.out.println("Long Poll started.");
        }
    }
    public void ignore(int id){
        synchronized (ignored) {
            ignored.add(id);
        }
    }
    public void unignore(int id){
        synchronized (ignored) {
            ignored.remove(id);
        }
    }
    public boolean isIgnored(int id){
        synchronized (ignored) {
            return ignored.contains(id);
        }
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
     * Membership in group checking.
     */
    public boolean isMember(int id){
        check();
        return interacter.isMember(id);
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

            sendMessageToOwner("VkBot has been exited on:\nserverTime["
                    + new Date().toString() + "]\nBye-bye!");
            System.out.println(
                    "╔══╗╔╗╔╦═══╗──╔══╗╔╗╔╦═══╦╗\n" +
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
