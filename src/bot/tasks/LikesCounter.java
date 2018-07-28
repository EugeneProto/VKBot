package bot.tasks;

import bot.Bot;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.PhotoFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import com.vk.api.sdk.objects.wall.WallPostFull;

import java.util.*;

import static bot.Bot.logger;

/**
 * Class for calculating likes using Vk Api.
 */
public class LikesCounter {
    private UserActor owner;
    private VkApiClient vk;

    public LikesCounter(UserActor owner, VkApiClient vk) {
        this.owner = owner;
        this.vk = vk;
    }

    /**
     * Calculating likes on photos in albums.
     * @param target user target
     * @param albumId "wall" or "profile" or in some cases "saved"
     * @return count of likes
     */
    public int calculateCountOfLikes(UserXtrCounters target, String albumId) {
        int likes = 0;
        try {
            List<PhotoFull> photos = vk.photos()
                    .getExtended(owner)
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

    /**
     * Calculate likes on posts on target wall.
     * @param target user target
     * @return count of likes
     */
    public int calculateContOfLikesOnPosts(UserXtrCounters target){
        int likes=0;
        try {
                List<WallPostFull> posts= vk.wall()
                        .get(owner)
                        .ownerId(target.getId())
                        .count(100)
                        .execute()
                        .getItems();
                for (WallPostFull post:posts) likes+=post.getLikes().getCount();

        } catch (ApiException e) {
            logger.error("Api Exception when counting likes.");
        } catch (ClientException e) {
            logger.error("Client Exception when counting likes.");
        } finally {
            return likes;
        }
    }
}
