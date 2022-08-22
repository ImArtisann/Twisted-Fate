package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.rithms.riot.api.RiotApiException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Timer;
import java.util.TimerTask;

public class broke extends Command {

    public mongo db = new mongo();
    public activeBets bets = new activeBets();

    public broke(){
        this.name = "broke";
        this.help = "This command is only use if your LP balance goes below 0";
        this.aliases = new String[]{ "poor","zero", "change" };
    }
    /*
     * Command to get 100 lp if you have 0 and no active bets going on this command is only a thing since theres no other
     * way to earn lp outside of betting on games
     */
    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(bets.map.containsKey(event.getAuthor().getId())) {
            event.reply("You currently have active bets going on you cant use this command right now do +ab to see active bets the bot will notify you once games removed please give 5mins after game ends");
        }else {

            Document doc = (Document) db.mongoCo.find(new Document("userID", event.getAuthor().getId())).first();

            long balance = doc.getLong("balance");

            if (balance > 0) {
                event.reply("Sorry you are not completely broke yet you have to have 0 balance to use this command");
            } else {
                Bson updatedvalue = new Document("balance", 100L);
                Bson updateOperation = new Document("$set", updatedvalue);
                db.mongoCo.updateOne(doc, updateOperation);
                event.reply("Someones not good at gambling I guess ill give you 100 Lp good luck if you want alittle more use +vote and vote for the bot to earn 1000LP");
            }
        }
    }
}
