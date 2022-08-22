package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class lookUp extends Command {

    public mongo db = new mongo();

    public lookUp(){
        this.name = "lookup";
        this.help = "Use this command to look up a discord user in or database to see their balance and the amount of wins/loses they have";
    }

    //lookup command to see a discord users stats like econ, wins and loses
    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        try {
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();

            Document doc = (Document) db.mongoCo.find(new Document("userID", mentionedUsers.get(0).getId())).first();
            long balance = doc.getLong("balance");
            long wins = doc.getLong("wins");
            long loses = doc.getLong("loses");

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.cyan);
            eb.setTitle(mentionedUsers.get(0).getAsTag() + " Profile");
            eb.setDescription(mentionedUsers.get(0).getAsTag() + " current balance is:  " + balance + " LP");
            eb.addField("Number of successful bets: ", "" + wins, true);
            eb.addField("Number of failed bets: ", "" + loses, true);
            event.reply(eb.build());
        }catch (Exception e){
            event.reply("You must mention a user to look them up on the bot or they didnt register with the bot yet");
        }

    }
}
