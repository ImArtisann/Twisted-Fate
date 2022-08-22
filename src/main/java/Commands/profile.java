package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.Document;

import java.awt.*;

public class profile extends Command {

    public mongo db = new mongo();

    public profile(){
        this.name = "profile";
        this.help = "User this command to display your stats on the bot and see your balance";
        this.aliases = new String[]{ "wallet","bal", "balance" };
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getAuthor().isBot()) return;

        Document doc = (Document) db.mongoCo.find(new Document("userID", event.getAuthor().getId())).first();

        if(doc == null){
            event.reply("You do not have an account registered with me please use the command +register first");
        }else{
            long balance = doc.getLong("balance");
            long wins = doc.getLong("wins");
            long loses = doc.getLong("loses");

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.cyan);
            eb.setTitle(event.getAuthor().getAsTag() +" Profile");
            eb.setDescription("Your current balance is:  " + balance + " LP");
            eb.addField("Number of successful bets: ", "" +wins, true);
            eb.addField("Number of failed bets: ", "" + loses, true);
            event.reply(eb.build());
        }
    }


}
