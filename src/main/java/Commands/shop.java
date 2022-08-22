package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;

public class shop extends Command {

    public mongo db = new mongo();

    public shop(){
        this.name = "shop";
        this.help = "use this command to display the shop";
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(event.getArgs().isEmpty()){
            Document doc = (Document) db.shopCo.find(new Document("serverID", event.getGuild().getId())).first();
            String roles = doc.getString("roles");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(event.getGuild().getName() + " server shop");
            eb.setColor(Color.cyan);
            eb.setDescription("To buy a role or command do command +buy [role or command name] RoleName:Cost of role");
            if(roles.equalsIgnoreCase("")){
                eb.addField("Here are the roles that you can buy on the server: ", "Your server owner hasnt" +
                        "added anything to the shop yet have the server owner use command" +
                        "+shop add [Role name]:[Cost]" , false);
            }else{
                String shopRoles = doc.getString("roles");
                eb.addField("Here are the roles that you can buy on the server: ", shopRoles , false);
            }
            String commands = doc.getString("commands");

            event.reply(eb.build());
        }else {
            if(event.getGuild().getOwnerId().equalsIgnoreCase(event.getAuthor().getId())){
                String[] args = event.getArgs().split(" ");
                if(args[0].equalsIgnoreCase("add")){
                    Document doc = (Document) db.shopCo.find(new Document("serverID", event.getGuild().getId())).first();
                    String shopRoles = doc.getString("roles");
                    if(shopRoles.isEmpty()){
                        System.out.println("HELLO");
                        shopRoles = args[1];
                        Bson updatedvalue = new Document("roles", args[1]);
                        Bson updateOperation = new Document("$set", updatedvalue);
                        db.shopCo.updateOne(doc, updateOperation);
                        event.reply("Added the role " + args[1]);
                    }else {
                        shopRoles += "," + args[1];
                        Bson updatedvalue = new Document("roles", shopRoles);
                        Bson updateOperation = new Document("$set", updatedvalue);
                        db.shopCo.updateOne(doc, updateOperation);
                        event.reply("Added the role " + args[1]);
                    }
                }else if(args[0].equalsIgnoreCase("del")){
                        String roleRemoving = args[1];
                        Document doc = (Document) db.shopCo.find(new Document("serverID", event.getGuild().getId())).first();
                        String shopRoles = doc.getString("roles");
                        String[] oldShop = shopRoles.split(",");
                        String newShop = "";
                        for(int i = 0; oldShop.length>i; i++){
                            if(!oldShop[i].equalsIgnoreCase(roleRemoving)){
                                newShop += "," + oldShop[i];
                            }
                        }
                    Bson updatedvalue = new Document("roles", newShop);
                    Bson updateOperation = new Document("$set", updatedvalue);
                    db.shopCo.updateOne(doc, updateOperation);
                    event.reply("You have deleted the role " + roleRemoving);

                }else{
                    event.reply("Server owner must use the command like so +shop add [RoleName]:[Cost]");
                }
            }else{
                event.reply("Sorry you are not the server owner you cant add anything to the server shop");
            }
        }

    }
}
