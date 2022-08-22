package Commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class activeBets extends Command {
    /*
     * The hashmap map is to keep track of all the active bets a user placed
     * The key (String) stores the user ID so when a bet is placed its easy
     * to find the user in the hash map since no user ID is the same
     * The value in the hashmap is an arraylist of strings the size of the
     * array list is how many active bets the user has going on and when we
     * split the string in the list args[0] = summoner they are betting on
     * args[1] is the bet amount they place args[2] is their choice win/lose
     */
    public static HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<String>> baron = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<String>> drag = new HashMap<String, ArrayList<String>>();

    public activeBets(){
        this.name = "ab";
        this.help = "This is the command to show all your active bets that are on going rn";
        this.aliases = new String[]{ "active","activebets" };
    }

    @Override
    protected void execute(CommandEvent event) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(event.getAuthor().getAsTag() +  " Active bets");
        eb.setDescription("Here will display all your Active bets that you have going on");
        eb.setColor(Color.GREEN);

        if(event.getAuthor().isBot()) return;

        if(map.containsKey(event.getAuthor().getId()) || baron.containsKey(event.getAuthor().getId()) || drag.containsKey(event.getAuthor().getId())){
            if(map.containsKey(event.getAuthor().getId())) {
                ArrayList<String> bets = new ArrayList<>();
                bets = map.get(event.getAuthor().getId());
                System.out.println(bets.size());
                for (int i = 0; i < bets.size(); i++) {
                    String[] args = bets.get(i).split(",");
                    eb.addField("You have a bet placed on : " + args[0] + " Bet Amount: " + args[1] + " You are hoping that they: " + args[2], "", false);
                }
            }
            if(baron.containsKey(event.getAuthor().getId())){
                ArrayList<String> baron2 = new ArrayList<>();
                baron2 = baron.get(event.getAuthor().getId());
                System.out.println(baron.size());
                for (int i = 0; i < baron2.size(); i++) {
                    String[] args = baron2.get(i).split(",");
                    eb.addField("You have a bet placed on : " + args[0] + " Bet Amount: " + args[1] + " You are hoping that they get first baron", "", false);
                }
            }
            if(drag.containsKey(event.getAuthor().getId())){
                ArrayList<String> drag2 = new ArrayList<>();
                drag2 = drag.get(event.getAuthor().getId());
                System.out.println(drag2.size());
                for (int i = 0; i < drag2.size(); i++) {
                    String[] args = drag2.get(i).split(",");
                    eb.addField("You have a bet placed on : " + args[0] + " Bet Amount: " + args[1] + " You are hoping that they get first dragon", "", false);
                }

            }
            event.reply(eb.build());
        }else{
            event.reply("You do not have any active bets going on right now use +bet to place a bet");
        }



    }
}
