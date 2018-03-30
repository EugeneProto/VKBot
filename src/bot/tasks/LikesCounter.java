package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.PhotoFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import com.vk.api.sdk.objects.wall.WallpostFull;
import org.slf4j.Logger;

import java.util.*;

public class LikesCounter {
    private UserActor user;
    private VkApiClient vk;
    private Logger logger;

    public LikesCounter(UserActor user, VkApiClient vk) {
        this.user = user;
        this.vk = vk;
        this.logger = Bot.logger;
    }

    public int calculateCountOfLikes(UserXtrCounters target, String albumId) {
        int likes = 0;
        try {
            List<PhotoFull> photos = vk.photos()
                    .getExtended(user)
                    .ownerId(target.getId())
                    .albumId(albumId)
                    .count(100)
                    .execute()
                    .getItems();
            for (PhotoFull photo:photos) likes +=photo.getLikes().getCount();

        } catch (ApiException e) {
            logger.error("Api Exception when counting likes.");
        } catch (ClientException e) {
            logger.error("Client Exception when counting likes.");
        } finally {
            return likes;
        }
    }
    public int calculateContOfLikesOnPosts(UserXtrCounters target){
        int likes=0;
        try {
                List<WallpostFull> posts= vk.wall()
                        .get(user)
                        .ownerId(target.getId())
                        .count(100)
                        .execute()
                        .getItems();
                for (WallpostFull post:posts) likes+=post.getLikes().getCount();

        } catch (ApiException e) {
            logger.error("Api Exception when counting likes.");
        } catch (ClientException e) {
            logger.error("Client Exception when counting likes.");
        } finally {
            return likes;
        }
    }
}
