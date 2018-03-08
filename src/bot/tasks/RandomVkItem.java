package bot.tasks;

import bot.Bot;
import bot.utils.Pair;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.wall.WallPostFull;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Random;

public class RandomVkItem {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;
    private int[] resources;

    public RandomVkItem(UserActor user, VkApiClient vk, int[] resources){
        this.user=user;
        this.vk=vk;
        this.resources =resources;
        this.logger= Bot.logger;
    }

    public Pair<String,String[]> randomMeme(){
        Pair<String,String[]> result=new Pair<>("",new String[]{});
        try {
            Random random=new Random();
            WallPostFull post=vk.wall()
                    .get(user)
                    .ownerId(resources[random.nextInt(resources.length)])
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
    public String randomVideo(){
        String result="";
        try {
            Random random=new Random();
            Video video=vk.videos()
                    .get(user)
                    .ownerId(resources[random.nextInt(resources.length)])
                    .count(1)
                    .offset(random.nextInt(100))
                    .execute()
                    .getItems()
                    .get(0);
            result="video"+video.getOwnerId()+"_"+video.getId();
        } catch (ApiException e) {
            logger.error("Api Exception when getting meme.");
        } catch (ClientException e) {
            logger.error("Client Exception when getting meme.");
        } finally {
            return result;
        }
    }
}
