package Commands;


import Database.mongo;
import Database.topgg;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.rithms.riot.api.RiotApiException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class vote extends Command {

    public topgg stats = new topgg();
    public mongo db = new mongo();

    public HashMap<String, String> voted = new HashMap<>();



    public vote (){
        this.name = "vote";
        this.help = "this command you can call every 12 hours to vote for the bot and youll earn 1000 LP for voting for the bot";
    }


    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        checkIfUserVoted(event.getAuthor().getId(), event);


    }

    public void checkIfUserVoted(String userID, CommandEvent event){
        stats.statsapi.hasVoted(userID).whenComplete((hasVoted, e) -> {
            if(hasVoted) {
                secondCheck(userID, event);
            }
            else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Vote for the bot and get an extra 1000lp");
                eb.setDescription("Vote for the bot here: https://top.gg/bot/804091938361573447 after you vote use the command again and ill reward you with 1000LP");
                eb.setColor(Color.cyan);
                event.reply(eb.build());
            }
        });

    }

    public void secondCheck(String userID, CommandEvent event) {

        if (voted.containsKey(userID)) {
            event.reply("Please wait the 12 hours to vote for the bot again thank you!");
        } else {
            addBalance(userID);
            event.reply("Thank you for voting for the bot I have added 1000LP to your acount");
            try {
                int MINUTES = 720;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() { // Function runs every MINUTES minutes.

                        if (!voted.containsKey(userID)) {
                            voted.put(userID, "yes");
                        } else {
                            voted.remove(userID);
                            timer.cancel();
                            timer.purge();
                        }
                    }
                }, 0, 1000 * 60 * MINUTES);
            } catch (Exception e) {
                System.out.println("Error in the second check method: " + e);
            }

        }
    }
    public void addBalance(String userID){

        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long balance = doc.getLong("balance");

        Bson updatedvalue = new Document("balance", balance+1000l);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);


    }

}
