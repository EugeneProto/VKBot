package bot.tasks;

import bot.Bot;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for autoposting random photos to group`s wall.
 */
public class AutopostRandomPhotos extends TimerTask {
    private Bot bot;

    public AutopostRandomPhotos(Bot bot) {
        this.bot = bot;
        Timer timer=new Timer(true);
        timer.schedule(this,5*60*1000,2*60*60*1000);
    }

    @Override
    public void run() {
        bot.postPhotoToWall(bot.randomImage());
    }
}
