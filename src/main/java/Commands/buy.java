package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;
import org.bson.conversions.Bson;

public class buy extends Command {

    public mongo db = new mongo();

    public buy(){
        this.name = "buy";
        this.help = "this command is use to buy things in your server shop";
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(event.getArgs().isEmpty()){
            event.reply("You did not provide valid arguments make sure you are doing it like so +buy [option]");
        }else{
            Document doc = (Document) db.shopCo.find(new Document("serverID", event.getGuild().getId())).first();

            String[] roles = doc.getString("roles").split(",");

            String userBuying = event.getArgs();

            Boolean userGaveCorrect = false;
            long cost = 100000;

            for(int i = 0; i < roles.length; i++){
                String[] test = roles[i].split(":");

                if(test[0].equalsIgnoreCase(userBuying)){
                    userGaveCorrect = true;
                    cost = Long.parseLong(test[1]);
                    break;
                }
            }

            if (userGaveCorrect){
                Document doc2 = (Document) db.mongoCo.find(new Document("userID", event.getAuthor().getId())).first();
                if(doc2 == null){
                    event.reply("You do not have an account registered with the bot please use command +register");
                }else{
                    long balance = doc2.getLong("balance");

                    if(balance >= cost){
                        addRole(event.getAuthor().getId(), userBuying, event);
                        updateBal(cost, event.getAuthor().getId());
                        event.reply("You have successfully bought that role!");
                    }else{
                        event.reply("sorry you do not have enough to buy this role");
                    }
                }
            }else{
                event.reply("You did not provide a correct option to buy");
            }
        }

    }

    public void addRole(String userID, String roleName, CommandEvent event){
        try {
            System.out.println(roleName);
            Role role = event.getGuild().getRolesByName(roleName, true).get(0);

            event.getGuild().addRoleToMember(userID, role).queue();
        }catch (Exception e){
            System.out.println(e);
            event.reply("Either my role on the server needs to be moved up by the owner or the server owner hasnt added" +
                    "those roles yet");
        }
    }

    public void updateBal(long cost, String userID){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long balance = doc.getLong("balance");
        Bson updatedvalue = new Document("balance", balance-cost);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);

    }

}
