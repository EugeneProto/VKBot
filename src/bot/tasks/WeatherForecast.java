package bot.tasks;

import bot.Bot;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class WeatherForecast {
    private Logger logger;
    private String id;

    public WeatherForecast(String id) {
        this.logger = Bot.logger;
        this.id=id;
    }

    public String receiveWeatherForecast(String city,String countryCode){
        String result="";
        try {
            city=city.replaceAll(" ","+");
            HttpClient client=HttpClientBuilder.create().build();
            HttpGet request= new HttpGet("http://api.openweathermap.org/data/2.5/forecast?" +
                    "q="+city+","+countryCode+"&lang=ru&units=metric&appid="+id);
            request.addHeader("User-Agent",USER_AGENT);
            HttpResponse response=client.execute(request);
            BufferedReader reader=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer=new StringBuffer();
            String line;
            while((line=reader.readLine())!=null) buffer.append(line);
            result=parseJson(new JSONObject(buffer.toString()));
        } catch (IOException e) {
            logger.error("IO Exception when receiving weather");
        } finally {
            return result;
        }
    }
    private String parseJson(JSONObject object){
        StringBuffer result=new StringBuffer();
        JSONArray array=object.getJSONArray("list");
        for (int i = 0; i <(array.length()>16?16:array.length()); i++) {
            JSONObject item=array.getJSONObject(i);
            result.append(item.getString("dt_txt"))
                .append(" ")
                .append(item.getJSONObject("main").getDouble("temp")+"°C")
                .append(" ")
                .append("ветер "+item.getJSONObject("wind").getDouble("speed")+" м/с")
                .append(" ")
                .append(new String(item.getJSONArray("weather").getJSONObject(0).
                    getString("description").getBytes(), Charset.forName("UTF-8")))
                .append("\n"+"\n");
        }
        return result.toString();
    }
}
