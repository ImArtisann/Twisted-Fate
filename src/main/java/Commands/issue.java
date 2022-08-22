package Commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.rithms.riot.api.RiotApiException;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class issue extends Command {

    public ArrayList<String> cooldown = new ArrayList<>();
    public HashMap<String, Integer> ran = new HashMap<String, Integer>();

    public issue(){
        this.name = "issue";
        this.help = "Did the bot give you an issue or you want to report a bug? use this command +issue [Brief description of error or bug that you found]";
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(event.getArgs().isEmpty()){
            event.reply("Please add an issue that you'd like to report after the command issue examples (active bets not clearing after 10+ mins, typos, reward was wrong)");
        }else{
            cooldownTimer(event.getAuthor().getId(), event);
        }

    }

    // posts the issue in the channel i want them to be posted in and also adds the user to the cool down list to prevent spam
    public void cooldownTimer(String userID, CommandEvent event){

        if(cooldown.size()>0) {
            boolean alreadyReportedAnIssue = false;
            for (int i = 0; i < cooldown.size(); i++) {
                if(cooldown.get(i).equalsIgnoreCase(userID)){
                    alreadyReportedAnIssue = true;
                }
            }
            if(alreadyReportedAnIssue == true){
                event.reply("You already reported an issue in the past 10 mins please do not spam this command");
            }else{
                cooldown.add(userID);
                int MINUTES = 10;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() { // Function runs every MINUTES minutes.
                        if(alreadyRan(userID)){
                            cooldown.remove(getLocation(userID));
                            ran.remove(userID);
                        }else{
                            ran.put(userID,1);
                        }
                    }
                }, 0, 1000 * 60 * MINUTES);
                System.out.println("You made it here");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.RED);
                eb.setTitle("New Issue Reported");
                eb.setDescription("User Reporting issue " + event.getAuthor().getAsTag());
                eb.addField("Issue", event.getArgs(), false);
                event.getJDA().getGuildById("747272674853519451").getTextChannelById("804836127873040384").sendMessage(eb.build()).queue();

            }
        }else{
            cooldown.add(userID);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle("New Issue Reported");
            eb.setDescription("User Reporting issue " + event.getAuthor().getAsTag());
            eb.addField("Issue", event.getArgs(), false);
            event.getJDA().getGuildById("747272674853519451").getTextChannelById("804836127873040384").sendMessage(eb.build()).queue();
        }

    }

    //Since a timers operation already runs at the start of a timer this is to double check that it hasnt ran or did
    public boolean alreadyRan(String userID){

        if(ran.containsKey(userID)){
            return true;
        }
        return false;
    }

    //gets location of the userid in the cooldown list to remove them so they can use the command again after 10mins
    public int getLocation(String userID){
        int location = 0;

        for(int i = 0; i < cooldown.size(); i++){
            if(cooldown.get(i).equalsIgnoreCase(userID)){
                location = i;
                break;
            }
        }
        return location;
    }
}
