package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.Document;
import org.bson.conversions.Bson;

public class admin extends Command {

    public activeBets ab = new activeBets();
    public mongo db = new mongo();

    public admin(){
        this.name = "admin";
        this.help = "Command only for the bot owner";
        this.aliases = new String[]{ "a" };
    }

    @Override
    protected void execute(CommandEvent event) {


        if(event.getAuthor().isBot()) return;

        if(event.getAuthor().getId().equalsIgnoreCase("176215532377210880")){
            String[] args = event.getArgs().split(" ");

            if(args[0].equalsIgnoreCase("setBal")){
                setBal(event.getMessage().getMentionedMembers().get(0).getId(),Long.parseLong(args[2]));
                event.reply("Set the users balance to " + args[2]);
            }else if(args[0].equalsIgnoreCase("clear")){
                clearActiveBets(event.getMessage().getMentionedMembers().get(0).getId());
                event.reply("Cleared the users active bets");
            }else if(args[0].equalsIgnoreCase("give")){
                giveBalance(event.getMessage().getMentionedMembers().get(0).getId(),Long.parseLong(args[2]));
            }else if(args[0].equalsIgnoreCase("addWins")){
                addWins(event.getMessage().getMentionedMembers().get(0).getId(),Long.parseLong(args[2]));
            }else if(args[0].equalsIgnoreCase("addLoses")){
                addLoses(event.getMessage().getMentionedMembers().get(0).getId(),Long.parseLong(args[2]));
            } else if(args[0].equalsIgnoreCase("help")){
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Admin commands");
                eb.setDescription("");
                eb.addField("setBal : " , "use this to set the users balances with the amount I want", true);
                eb.addField("clear : ", "used when a users active bets doesnt clear", true);
                eb.addField("give : ", "This command is used to add on to a users balance", true);
                eb.addField("addwins : ", "uses to add to the users total amount of wins", true);
                eb.addField("addloses : ", "uses to add to the users total amount of loses", true);
                event.reply(eb.build());

            }
                else{
                event.reply("You didnt give a correct command ");
            }
        }else{
            event.reply("NO");
        }
    }

    public void addLoses(String userID, long loses){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        long current = doc.getLong("wins");

        Bson updatedvalue = new Document("balance", current+loses);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
    }

    public void addWins(String userID, long wins){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        long current = doc.getLong("wins");

        Bson updatedvalue = new Document("balance", current+wins);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
    }

    public void giveBalance(String userID, long bal){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        long current = doc.getLong("balance");

        Bson updatedvalue = new Document("balance", current+bal);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
    }

    public void clearActiveBets(String userID){
        ab.map.remove(userID);
        ab.drag.remove(userID);
        ab.baron.remove(userID);
    }

    public void setBal(String userID, long bal){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        Bson updatedvalue = new Document("balance", bal);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
    }
}
