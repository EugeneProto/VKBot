package bot.tasks;

import bot.Bot;
import bot.utils.Pair;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.video.Video;

import com.vk.api.sdk.objects.wall.WallpostFull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Random;

/**
 * Random item from vk`s public.
 */
public class RandomVkItem {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;

    /**
     * Publics ids.
     */
    private int[] resources;

    public RandomVkItem(UserActor user, VkApiClient vk, int[] resources){
        this.user=user;
        this.vk=vk;
        this.resources =resources;
        this.logger= Bot.logger;
    }

    /**
     * Random meme from vk public from resources.
     * @return text or photo identifier(s)or text with photo identifier(s)
     */
    public Pair<String,String[]> randomMeme(){
        Pair<String,String[]> result=new Pair<>("",new String[0]);
        try {
            Random random=new Random();
            WallpostFull post=vk.wall()
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
            result=new Pair<>(post.getText(),fields.toArray(new String[0]));
        } catch (ApiException e) {
            logger.error("Api Exception when getting meme.");
        } catch (ClientException e) {
            logger.error("Client Exception when getting meme.");
        } finally {
            return result;
        }
    }

    /**
     * Random video from vk public from resources.
     * @return video identifier
     */
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
