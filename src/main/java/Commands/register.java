package Commands;

import Database.mongo;
import Listeners.onReady;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.bson.Document;

public class register extends Command {

    public mongo db = new mongo();

    public register(){
        this.name = "register";
        this.help = "Register your account to get your starting balance to start gambling on friends games";

    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getAuthor().isBot()) return;

        String userID = event.getAuthor().getId();

        if(isUserAlreadyRegistered(userID)){
            event.reply("You already have an account registered with the bot");
        }else{
            System.out.println("Adding user to the database");
            Document doc = (Document) new Document("userID", userID);
            doc.append("balance", 1000L);
            doc.append("wins", 0L);
            doc.append("loses", 0L);
            db.mongoCo.insertOne(doc);
            event.reply("Your account was successfully created and your balance is now set to 1000 LP");
        }

    }

    public boolean isUserAlreadyRegistered(String userID){

        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        if (doc == null){
            return false;
        }else return true;

    }
}
