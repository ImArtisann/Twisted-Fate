package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public class pay extends Command {

    public mongo db = new mongo();

    /*
     * The pay command is used if a discord user wants to send another discord user some lp for any reason
     */
    public pay(){
        this.name = "pay";
        this.help = "Is your friend broke and you want to loan him some LP? +pay @Discord name(Must mention the user),[amount]";
        this.aliases = new String[]{ "send","give", "p", "loan" };
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(event.getArgs().isEmpty()){
            event.reply("You didnt use the command correctly +pay @Discord name(Must mention the user),[amount]");
        }else {
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();

            if (mentionedUsers.size() < 0) {
                event.reply("You did not choose a user you would like to pay");
            } else {
                String[] args = event.getArgs().split(",");
                long payAmount = 0;
                try {
                    payAmount = Long.parseLong(args[1].trim());
                } catch (Exception e) {
                    System.out.println("A user cause an error in pay command: " + e);
                }
                if (payAmount > 0) {
                    Document payee = (Document) db.mongoCo.find(new Document("userID", event.getAuthor().getId())).first();

                    if (payee == null) {
                        event.reply("You do not have an account register with the bot please do command +register");
                    } else {
                        long payeeBalance = payee.getLong("balance");

                        if (payeeBalance >= payAmount) {
                            try {
                                payUser(mentionedUsers.get(0).getId(), payAmount, event);
                            }catch (Exception e){
                                event.reply("You must @mention a user!");
                            }
                        } else {
                            event.reply("Sorry you do not have that much LP in your own balance");
                        }
                    }
                }
            }
        }
    }

    /*
     * payUser method pulls the users thats getting paid document and updates their balance with the amount they
     * are getting paid then calls the method updatePayeeBal to update the payee balance
     */
    public void payUser(String userID, long payAmount, CommandEvent event){

        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        if(doc == null){
            event.reply("The user that you are trying to pay does not have an account registered with the bot please have them do +register to get started");
        }else{
            long userBalance = doc.getLong("balance");

            Bson updatedvalue = new Document("balance", userBalance + payAmount);
            Bson updateOperation = new Document("$set", updatedvalue);
            db.mongoCo.updateOne(doc, updateOperation);
            updatePayeeBal(event.getAuthor().getId(), payAmount);
            event.reply("The user <@" + userID + "> has successfully received the lp you sent to them and we deducted the amount from your balance");
        }

    }
    /*
     * updatePayeeBal method is called to update the payee balance to subtract what hes paying to the other user
     */
    public void updatePayeeBal(String userID, long payAmount){

        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();

        long userBalance = doc.getLong("balance");

        Bson updatedvalue = new Document("balance", userBalance - payAmount);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);

    }


}
