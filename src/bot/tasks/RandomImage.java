package bot.tasks;

import bot.Bot;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;

import java.io.*;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class RandomImage {
    private Logger logger;

    public RandomImage(){
        logger= Bot.logger;
    }

    public File randomImage(){
        File photo=null;
        try{
            photo=File.createTempFile("tmp",".jpg");
            photo.deleteOnExit();
            HttpClient client= HttpClientBuilder.create().build();
            HttpGet request=new HttpGet("https://picsum.photos/800/800/?random");
            request.addHeader("User-Agent", USER_AGENT);
            HttpResponse response=client.execute(request);
            InputStream is=response.getEntity().getContent();
            photo.createNewFile();
            FileOutputStream os=new FileOutputStream(photo);
            IOUtils.copy(is,os);
        }catch (IOException e){
            logger.error("IO Exception when getting random image.");
        }finally {
            return photo;
        }
    }
}
