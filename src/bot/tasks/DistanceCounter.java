package bot.tasks;

import bot.Bot;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;
import org.slf4j.Logger;

import java.io.IOException;

public class DistanceCounter {
    private Logger logger;
    private GeoApiContext context;

    public DistanceCounter(GeoApiContext context) {
        this.context = context;
        this.logger= Bot.logger;
    }

    public String calculateTimeInSubway(String city,String origin, String destination){
        String result="";
        try {
            DistanceMatrix matrix=new DistanceMatrixApiRequest(context)
                    .language("ru")
                    .mode(TravelMode.TRANSIT)
                    .transitModes(TransitMode.SUBWAY)
                    .origins(city+",станция метро "+origin)
                    .destinations(city+",станция метро "+destination)
                    .await();
            if (matrix.rows.length>0&&matrix.rows[0].elements.length>0) {
                result=matrix.rows[0].elements[0].duration.humanReadable;
            }
        } catch (ApiException e) {
            logger.error("Api Exception when calculating time");
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception when calculating time");
        } catch (IOException e) {
            logger.error("IO Exception when calculating time");
        } finally {
            return result;
        }
    }
}
