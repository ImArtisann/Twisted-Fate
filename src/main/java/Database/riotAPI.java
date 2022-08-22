package Database;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class riotAPI {


        public static ApiConfig config ;
        public static RiotApi api;


    public void riotAPI() throws IOException {

        this.config = new ApiConfig().setKey("RIOT API KEY");
        this.api  = new RiotApi(config);
    }
}
