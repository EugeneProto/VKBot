package bot;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static bot.Bot.logger;

/**
 * Here is the main interactions with Vk Api.
 */
public class MainApiInteracter {
    private GroupActor group;
    private UserActor owner;
    private VkApiClient vk;

    public MainApiInteracter(GroupActor group,UserActor owner, VkApiClient vk){
        this.group =group;
        this.owner=owner;
        this.vk=vk;
    }

    /**
     * Send message with text or emoji.
     * @param id receiver id
     * @param text message text
     */
    public void sendMessage(int id, String text){
        try {
            vk.messages()
                    .send(group)
                    .userId(id)
                    .message(text)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when sending message.");
        } catch (ClientException e) {
            logger.error("Client Exception when sending message.");
        }
    }

    /**
     * @see MainApiInteracter#sendMessage(int, String)
     */
    public void sendMessageToOwner(String text){
        sendMessage(owner.getId(),text);
    }



    /**
     * Send message with video attachment.
     * @param id receiver id
     * @param text text (not necessary)
     * @param video video identifier
     */
    public void sendMessageWithVideo(int id,String text,String video){
        try {
            if (text!=null&&!text.equals("")&&video!=null&&!video.equals(""))
                vk.messages()
                        .send(group)
                        .userId(id)
                        .message(text)
                        .attachment(video)
                        .execute();
            else if (video!=null&&!video.equals(""))
                vk.messages()
                        .send(group)
                        .userId(id)
                        .attachment(video)
                        .execute();
            else vk.messages()
                        .send(group)
                        .userId(id)
                        .message("Упс...Ошибочка вышла. Попробуй снова.")
                        .execute();

        } catch (ApiException e) {
            logger.error("Api Exception when sending message.");
        } catch (ClientException e) {
            logger.error("Client Exception when sending message.");
        }
    }

    /**
     * Send message with photo attachment(s).
     * @param id receiver id
     * @param text text (not necessary)
     * @param photo photo identifiers
     */
    public void sendMessageWithPhoto(int id,String text,String...photo){
        try {
            if (text!=null&&!text.equals("")&&photo!=null&&photo.length>0) {
                vk.messages()
                        .send(group)
                        .userId(id)
                        .message(text)
                        .attachment(photo)
                        .execute();
            } else if (photo!=null&&photo.length>0){
                vk.messages()
                        .send(group)
                        .userId(id)
                        .attachment(photo)
                        .execute();
            } else {
                vk.messages()
                        .send(group)
                        .userId(id)
                        .message("Упс...Ошибочка вышла. Попробуй снова.")
                        .execute();
            }
        } catch (ApiException e) {
            logger.error("Api Exception when sending message.");
        } catch (ClientException e) {
            logger.error("Client Exception when sending message.");
        }
    }

    /**
     * @see MainApiInteracter#uploadPhoto(File, String)
     * @see MainApiInteracter#sendMessageWithPhoto(int, String, String...)
     */
    public void sendMessageWithPhoto(int id,String text,File photo){
        sendMessageWithPhoto(id, text, uploadPhoto(photo,"message"));
    }

    /**
     * Post photo to group wall.
     * @see MainApiInteracter#uploadPhoto(File, String)
     */
    public void postPhotoToWall(File photo){
        try {
            vk.wall()
                    .post(owner)
                    .attachments(uploadPhoto(photo,"wall"))
                    .ownerId(group.getId())
                    .fromGroup(true)
                    .execute();
            System.out.println();
        } catch (ApiException e) {
            logger.error("Api Exception when posting.");
        } catch (ClientException e) {
            logger.error("Client Exception when posting.");
        }
    }

    /**
     * Upload photo to Vk server.
     * @param photo photo file
     * @return photo identifier
     */
    private String uploadPhoto(File photo, String target){
        String attachment="";
        try {

            HttpClient client= HttpClientBuilder.create().build();
            HttpPost uploadPhoto;

            if (target.equals("wall"))
                uploadPhoto=new HttpPost(vk.photos()
                        .getWallUploadServer(owner)
                        .groupId(group.getGroupId())
                        .execute()
                        .getUploadUrl());
            else
                uploadPhoto=new HttpPost(vk.photos()
                    .getMessagesUploadServer(group)
                    .execute()
                    .getUploadUrl());

            uploadPhoto.addHeader("User-Agent", USER_AGENT);
            uploadPhoto.setEntity(MultipartEntityBuilder.create()
                    .addBinaryBody("photo",photo)
                    .build());
            HttpResponse response=client.execute(uploadPhoto);


            BufferedReader reader=new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
            StringBuffer buffer=new StringBuffer();
            String line;
            while ((line=reader.readLine())!=null) buffer.append(line);
            reader.close();
            JSONObject result=new JSONObject(buffer.toString());

            Photo att;
            if (target.equals("wall"))
                att=vk.photos()
                    .saveWallPhoto(owner,result.getString("photo"))
                    .hash(result.getString("hash"))
                    .server(result.getInt("server"))
                    .groupId(group.getGroupId())
                    .execute()
                    .get(0);
            else
                att=vk.photos()
                        .saveMessagesPhoto(group,result.getString("photo"))
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

    /**
     * Check user membership in group.
     * @param id user id
     * @return is member
     */
    public boolean isMember(int id){
        boolean isMember=false;
        try {
            isMember=vk.groups()
                    .isMember(group,group.getGroupId().toString(),id)
                    .execute()
                    .get(0)
                    .isMember();
        } catch (ApiException e) {
            logger.error("Api Exception when checking membership.");
        } catch (ClientException e) {
            logger.error("Client Exception when checking membership.");
        } finally {
            return isMember;
        }
    }
    public LongpollParams getLongpollParams(){
        LongpollParams params=new LongpollParams();
        try {
            params=vk.messages()
                    .getLongPollServer(group)
                    .execute();
        } catch (ApiException e) {
            logger.error("Api Exception when getting longpoll params.");
        } catch (ClientException e) {
            logger.error("Client Exception when getting longpoll params.");
        } finally {
            return params;
        }
    }
    public UserXtrCounters getSender(String id){
        UserXtrCounters counters=new UserXtrCounters();
        try {
            counters=vk.users().get(group)
                    .userIds(id)
                    .fields(UserField.SEX)
                    .execute()
                    .get(0);
        } catch (ApiException e) {
            logger.error("Api Exception when getting addressee.");
        } catch (ClientException e) {
            logger.error("Client Exception when getting addressee.");
        } finally {
            return counters;
        }
    }
}
