package bot.tasks;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static bot.Bot.logger;

/**
 * Class for receiving bitcoin rate.
 */
public class BitcoinRate {

    /**
     * Connect to server and receive rate.
     * @return string array which contains three value of bitcoin rate.
     */
    public String[] bitcoinRate(){
        String[] rate={};
        try {
            HttpClient client= HttpClientBuilder.create().build();
            HttpGet request=new HttpGet("https://blockchain.info/ru/ticker");
            request.addHeader("User-Agent",USER_AGENT);
            HttpResponse response=client.execute(request);
            BufferedReader reader=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result=new StringBuffer();
            String line;
            while ((line=reader.readLine())!=null) result.append(line);
            reader.close();
            rate=parseJson(new JSONObject(result.toString()));
        } catch (IOException e) {
            logger.error("IO Exception when counting Bitcoin Rate");
        } finally {
            return rate;
        }

    }

    /**
     * Parse server response.
     * @param json server response
     * @return array of values
     * @see BitcoinRate#bitcoinRate()
     */
    private String[] parseJson(JSONObject json){
        JSONObject object=json.getJSONObject("USD");
        return new String[]{""+object.getDouble("last")+"$",
                ""+object.getDouble("buy")+"$",""+object.getDouble("sell")+"$"};

    }
}
