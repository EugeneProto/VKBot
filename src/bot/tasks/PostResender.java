package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.WallPostFull;
import javafx.util.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Random;

public class PostResender {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;
    private int[] resourses;

    public PostResender(UserActor user, VkApiClient vk, int[] resourses){
        this.user=user;
        this.vk=vk;
        this.resourses=resourses;
        this.logger= Bot.logger;
    }

    public Pair<String,String[]> randomMeme(){
        Pair<String,String[]> result=new Pair<>("",new String[]{});
        try {
            Random random=new Random();
            WallPostFull post=vk.wall()
                    .getExtended(user)
                    .ownerId(resourses[random.nextInt(resourses.length)])
                    .count(1)
                    .offset(random.nextInt(100))
                    .execute()
                    .getItems()
                    .get(0);
            ArrayList<String> fields=new ArrayList<>();
            post.getAttachments().forEach(e->fields.add("photo"+e.getPhoto().getOwnerId()+"_"+
            e.getPhoto().getId()));
            result=fields.size()>0?new Pair<>(post.getText(),fields.toArray(new String[]{})):randomMeme();
        } catch (ApiException e) {
            logger.error("Api Exception when getting meme.");
        } catch (ClientException e) {
            logger.error("Client Exception when getting meme.");
        } finally {
            return result;
        }
    }
}
