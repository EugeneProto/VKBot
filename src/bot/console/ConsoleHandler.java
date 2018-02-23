package bot.console;

import bot.Bot;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;

public class ConsoleHandler {
    private Bot bot;
    private Logger logger;
    private BufferedReader reader;

    public ConsoleHandler(Bot bot,BufferedReader reader) {
        this.bot = bot;
        this.logger=Bot.logger;
        this.reader=reader;
    }

    public void monitor(){
        try{
            String line;
            String[] input;
            while (!(line=reader.readLine()).equals("exit")){
                input=line.split("&");
                switch (input[0]){
                    case "inter":
                        bot.interruptLongPoll();
                        break;
                    case "start":
                        bot.startLongPoll();
                        break;
                    case "btRate":
                        String[] btrate=bot.bitcoinRate();
                        System.out.println("Курс: "+btrate[0]+" Покупка: "+btrate[1]+" Продажа: "+btrate[2]);
                        break;
                    case "forecast":
                        if (input.length==3)System.out.println(bot.receiveWeatherForecast(input[1],input[2]));
                        break;

                }
            }
            System.exit(0);
        } catch (IOException e) {
            logger.error("IO Exception in ConsoleHandler "+e.getStackTrace());
        }
    }
}
