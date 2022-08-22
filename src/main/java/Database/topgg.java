package Database;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import org.discordbots.api.client.DiscordBotListAPI;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class topgg {

    public static DiscordBotListAPI statsapi;


    public void topAPI() throws IOException {
         this.statsapi = new DiscordBotListAPI.Builder()
                .token("top gg api key")
                .botId("bot id")
                .build();
    }
}
