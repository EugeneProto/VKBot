package bot.handler;


import bot.Bot;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * Long Poll logic.
 */
public class LongPollHandler extends Thread {
    private Logger logger;
    private MessageReplier replier;
    private Bot bot;

    /**
     * Pull of handlers for high loads.
     * @see LongPollHandler#handleResponse(JSONArray)
     */
    private ExecutorService service;

    /**
     * Should react to users messages.
     * @see LongPollHandler#handleResponse(JSONArray)
     * @see LongPollHandler#setShouldReact(boolean)
     */
    private boolean shouldReact;

    /**
     * Person who have launched the app id.
     * @see LongPollHandler#handleResponse(JSONArray)
     */
    private Integer userId;

    public LongPollHandler(Bot bot,int userId,MessageReplier replier){
        this.logger=Bot.logger;
        this.replier=replier;
        this.bot=bot;
        this.userId=userId;
        service= Executors.newFixedThreadPool(3);
        shouldReact=true;
    }

    /**
     * The main logic of Long Poll.
     * Connect to the vk server, wait for answer,
     * parse the answer and then connect again.
     */
    @Override
    public void run() {
        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpGet request;
            HttpResponse response;

            LongpollParams params=bot.getLongpollParams();
            int ts=params.getTs();
            String key = params.getKey(),server = params.getServer();
            while (this.isAlive()){
                request = new HttpGet("https://"+ server +"?act=a_check&" +
                            "key="+ key +"&ts="+ts+"&wait=25&mode=2&version=2");
                    request.addHeader("User-Agent", USER_AGENT);
                    response = client.execute(request);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer result = new StringBuffer();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    JSONObject object=new JSONObject(result.toString());
                if (object.has("ts")&&object.has("updates")) {
                    ts=object.getInt("ts");
                    handleResponse(object.getJSONArray("updates"));
                } else {
                    params=bot.getLongpollParams();
                    server=params.getServer();
                    key=params.getKey();
                    ts=params.getTs();
                    System.out.println("Update Long Poll.");
                }
            }

        } catch (ClientProtocolException e) {
        logger.error("Client Protocol Exception in LongPollHandler.");
        } catch (IOException e) {
        logger.error("IO Exception in LongPollHandler.");
        }
    }

    /**
     * Handle server response.
     * @param array "updates" array from JSON
     */
    private void handleResponse(JSONArray array){
            for (int i = 0; i <array.length() ; i++) {
                JSONArray update=array.getJSONArray(i);
                if (update.getInt(0)!=4) continue;

                int flag = update.getInt(2), id = update.getInt(3);
                boolean isIgnored = bot.isIgnored(id), isPlaying = bot.isPlaying(id);
                if(shouldReact&&((flag&2)!=2)&&id<2000000000&&!isIgnored&&!isPlaying&&id!=userId){
                    service.submit(()->{
                        UserXtrCounters sender=bot.getSender(update.get(3).toString());
                        replier.parse(new String(update.getString(5).getBytes(), Charset.forName("UTF-8")),sender);
                    });
                }else if(shouldReact&&((flag&2)!=2)&&id<2000000000&&!isIgnored&&isPlaying&& id!=userId){
                    service.submit(()->{
                        UserXtrCounters sender=bot.getSender(update.get(3).toString());
                        replier.parseGame(new String(update.getString(5).getBytes(),Charset.forName("UTF-8")),sender);
                    });
                } else if(shouldReact&&((flag &2)==2)&&id <2000000000&&id!=userId){
                    service.submit(()->{
                        UserXtrCounters sender=bot.getSender(update.get(3).toString());
                        replier.parseUser(new String(update.getString(5).getBytes(),Charset.forName("UTF-8")),sender);
                    });

                } else if(id==userId){
                    service.submit(()->{
                        UserXtrCounters sender=bot.getSender(update.get(3).toString());
                        replier.parseAdmin(new String(update.getString(5).getBytes(),Charset.forName("UTF-8")),sender);
                    });

                }
            }

    }

    public void setShouldReact(boolean shouldReact) {
        this.shouldReact = shouldReact;
    }
}
