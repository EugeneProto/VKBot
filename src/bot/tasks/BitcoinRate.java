package bot.tasks;


import bot.Bot;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class BitcoinRate {
    private Logger logger;

    public BitcoinRate() {
        this.logger = Bot.logger;
    }
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
            rate=parseJson(new JSONObject(result.toString()));
        } catch (IOException e) {
            logger.error("IO Exception when counting Bitcoin Rate");
        } finally {
            return rate;
        }

    }
    private String[] parseJson(JSONObject json){
        JSONObject object=json.getJSONObject("USD");
        return new String[]{""+object.getDouble("last")+"$",
                ""+object.getDouble("buy")+"$",""+object.getDouble("sell")+"$"};

    }
}
