package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class MainApiInteracter {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;

    public MainApiInteracter(UserActor user, VkApiClient vk){
        this.user=user;
        this.vk=vk;
        this.logger= Bot.logger;
    }
    public void sendMessage(int id, String text){
        try {
            vk.messages()
                    .send(user)
                    .userId(id)
                    .message(text)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when sending message");
        } catch (ClientException e) {
            logger.error("Client Exception when sending message");
        }
    }
    public void sendMessageToOwner(String text){
        sendMessage(user.getId(),text);
    }
    public LongpollParams getLongpollParams(){
        LongpollParams params=new LongpollParams();
        try {
            params=vk.messages()
                    .getLongPollServer(user)
                    .execute();
        } catch (ApiException e) {
            logger.error("LongPoll error");
        } catch (ClientException e) {
            logger.error("LongPoll error");
        } finally {
            return params;
        }
    }
    public UserXtrCounters getAddressee(String id){
        UserXtrCounters counters=new UserXtrCounters();
        try {
          counters=vk.users().get(user)
                    .userIds(id)
                    .fields(UserField.SEX)
                    .execute()
                    .get(0);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } finally {
            return counters;
        }
    }
    public void setStatus(String text){
        try {
            vk.status()
                    .set(user)
                    .text(text)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when setting status.");
        } catch (ClientException e) {
            logger.error("Client Exception when setting status.");
        }
    }
    public String getStatus(){
        String status="";
        try {
           status=vk.status()
                    .get(user)
                    .execute()
                    .getText();
        } catch (ApiException e) {
            logger.error("Api Exception when setting status.");
        } catch (ClientException e) {
            logger.error("Client Exception when setting status.");
        } finally {
            return status;
        }
    }
    public void setOnline(boolean isOnline){
        try {
            if (isOnline)vk.account()
                    .setOnline(user)
                    .voip(false)
                    .execute();
            else vk.account()
                    .setOffline(user)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when setting line.");
        } catch (ClientException e) {
            logger.error("Client Exception when setting line.");
        }
    }
    public void sendMessageWithPhoto(int id,String text,File photo){
        try {
            String attachment=uploadPhoto(photo);
            vk.messages()
                    .send(user)
                    .userId(id)
                    .message(text)
                    .attachment(attachment)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when sending message");
        } catch (ClientException e) {
            logger.error("Client Exception when sending message");
        }
    }
    private String uploadPhoto(File photo){
        String attachment="";
        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpPost uploadPhoto=new HttpPost(vk.photos()
                    .getMessagesUploadServer(user)
                    .execute()
                    .getUploadUrl());
            uploadPhoto.addHeader("User-Agent", USER_AGENT);
            MultipartEntityBuilder builder=MultipartEntityBuilder.create();
            builder.addBinaryBody("photo",photo);
            uploadPhoto.setEntity(builder.build());
            HttpResponse response=client.execute(uploadPhoto);
            BufferedReader reader=new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
            StringBuffer buffer=new StringBuffer();
            String line;
            while ((line=reader.readLine())!=null) buffer.append(line);
            JSONObject result=new JSONObject(buffer.toString());
            Photo att=vk.photos()
                    .saveMessagesPhoto(user,result.getString("photo"))
                    .hash(result.getString("hash"))
                    .server(result.getInt("server"))
                    .execute()
                    .get(0);
            attachment="photo"+att.getOwnerId()+"_"+att.getId();
        } catch (ApiException e) {
            logger.error("Api Exception when uploading photo.");
        } catch (ClientException e) {
            logger.error("Client Exception when uploading photo.");
        } finally {
            return attachment;
        }
    }
}
