package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.slf4j.Logger;

public class MessageSender {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;

    public MessageSender(UserActor user,VkApiClient vk){
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
}
