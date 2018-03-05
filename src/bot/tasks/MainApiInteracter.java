package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import org.slf4j.Logger;

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
}
