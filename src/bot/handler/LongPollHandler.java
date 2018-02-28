package bot.handler;


import bot.Bot;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
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
import java.io.UnsupportedEncodingException;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class LongPollHandler extends Thread {
    private Logger logger;
    private MessageReplier replier;
    private Bot bot;
    private UserActor user;
    private boolean shouldReact;

    public LongPollHandler(Bot bot,UserActor user,MessageReplier replier ){
        this.logger=Bot.logger;
        this.replier =replier;
        this.bot=bot;
        this.user=user;
        shouldReact=true;
        setDaemon(true);
    }
    @Override
    public void run() {
        try {
            HttpClient client= HttpClientBuilder.create().build();
            LongpollParams params=bot.getLongpollParams();
            int ts=params.getTs();
            HttpGet request;
            HttpResponse response;
            while (this.isAlive()){
                    request = new HttpGet("https://"+params.getServer()+"?act=a_check&" +
                            "key="+params.getKey()+"&ts="+ts+"&wait=25&mode=2&version=2");
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
                    ts=getTs(object);
                    handleResponse(object);
            }

        } catch (ApiException e) {
        logger.error("Api Exception in LongPollHandler");
        } catch (ClientException e) {
        logger.error("Client Exception in LongPollHandler");
        } catch (ClientProtocolException e) {
        logger.error("Client Protocol Exception in LongPollHandler");
        } catch (IOException e) {
        logger.error("IO Exception in LongPollHandler");
        }
    }
    private int getTs(JSONObject object){
        return object.getInt("ts");
    }
    private void handleResponse(JSONObject object) throws UnsupportedEncodingException {
        try {
            JSONArray array=object.getJSONArray("updates");
            for (int i = 0; i <array.length() ; i++) {
                JSONArray event=array.getJSONArray(i);
                if(shouldReact&&event.getInt(0)==4&&((event.getInt(2)&2)!=2)&&event.getInt(3)<2000000000&&
                        !bot.isIgnored(event.getInt(3))&&!bot.isPlaying(event.getInt(3))&&
                        event.getInt(3)!=user.getId()){
                  UserXtrCounters addressee=bot.getAddressee(event.get(3).toString());
                  replier.parse(new String(event.getString(5).getBytes(),"UTF-8"),addressee);
                }else if(shouldReact&&event.getInt(0)==4&&((event.getInt(2)&2)!=2)&&event.getInt(3)<2000000000&&
                        !bot.isIgnored(event.getInt(3))&&bot.isPlaying(event.getInt(3))&&
                        event.getInt(3)!=user.getId()){
                    UserXtrCounters addressee=bot.getAddressee(event.get(3).toString());
                    replier.parseGame(new String(event.getString(5).getBytes(),"UTF-8"),addressee);
                } else if(shouldReact&&event.getInt(0)==4&&((event.getInt(2)&2)==2)&&event.getInt(3)<2000000000&&
                        event.getInt(3)!=user.getId()){
                    UserXtrCounters addressee=bot.getAddressee(event.get(3).toString());
                    replier.parseUser(new String(event.getString(5).getBytes(),"UTF-8"),addressee);
                } else if(event.getInt(0)==4&&event.getInt(3)==user.getId()){
                    UserXtrCounters addressee=bot.getAddressee(event.get(3).toString());
                    replier.parseAdmin(new String(event.getString(5).getBytes(),"UTF-8"),addressee);
                }
            }
        } catch (ApiException e) {
            logger.error("Api Exception in LongPollHandler when handle event");
        } catch (ClientException e) {
            logger.error("Client Exception in LongPollHandler when handle event");
        }
    }

    public void setShouldReact(boolean shouldReact) {
        this.shouldReact = shouldReact;
    }
}
