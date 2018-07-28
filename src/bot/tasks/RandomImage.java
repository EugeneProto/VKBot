package bot.tasks;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static bot.Bot.logger;

/**
 * Random image from Lorem Picsum site.
 */
public class RandomImage {

    public File randomImage(){
        File photo=null;
        try{
            photo=File.createTempFile("tmp",".jpg");
            photo.deleteOnExit();
            photo.createNewFile();
            HttpClient client= HttpClientBuilder.create().build();
            HttpGet request=new HttpGet("https://picsum.photos/800/800/?random");
            request.addHeader("User-Agent", USER_AGENT);
            HttpResponse response=client.execute(request);
            InputStream is=response.getEntity().getContent();
            FileOutputStream os=new FileOutputStream(photo);
            IOUtils.copy(is,os);
            os.close();
            is.close();
        }catch (IOException e){
            logger.error("IO Exception when getting random image.");
        }finally {
            return photo;
        }
    }
}
