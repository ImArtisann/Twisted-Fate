package Commands;

import Database.mongo;
import com.mongodb.client.FindIterable;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.bson.conversions.Bson;
import org.bson.Document;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

public class leaderboard extends Command {

    public mongo db = new mongo();

    public leaderboard(){
        this.name = "lb";
        this.help = "User this command to see whos the top dog in economy, wins, or loses";
        this.aliases = new String[]{ "leaderboard", "leader", "board"};
    }

    //Command to display leaderboards the user wants to see economy,wins,loses

    @Override
    protected void execute(CommandEvent event) {
        if(event.getAuthor().isBot()) return;

        String args = event.getArgs();

        if(args.equalsIgnoreCase("economy")){
            economyLeaderboard(event);
        }else if(args.equalsIgnoreCase("wins")){
            winsLeaderBoard(event);
        }else if(args.equalsIgnoreCase("loses")){
            losesLeaderBoard(event);
        }else if(args.equalsIgnoreCase("local")){
            localEconomyLeaderboard(event);
        }else{
            event.reply("Which leaderboad do you want to display use the command like so +b [Economy,Wins,Loses] choose one");
        }
    }

    //will generate the economy leaderboard

    public void economyLeaderboard(CommandEvent event){
        List<String> userIDList = new ArrayList<>();
        List<Long> userBalanceList = new ArrayList<>();
        Bson sort = descending("balance");
        FindIterable<Document> docs = db.mongoCo.find().sort(sort);
        DecimalFormat formatter = new DecimalFormat("#,###");
        for(Document doc : docs){
            String userID = doc.getString("userID");
            Long bal = doc.getLong("balance");
            userIDList.add(userID);
            userBalanceList.add(bal);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Economy Leaderboard");
        eb.setDescription("");
        eb.addField("1st", "<@" + userIDList.get(0) + "> Balance : " + formatter.format(userBalanceList.get(0)) + " LP", false );
        eb.addField("2nd", "<@" + userIDList.get(1) + "> Balance : " + formatter.format(userBalanceList.get(1)) + " LP", false );
        eb.addField("3rd", "<@" + userIDList.get(2) + "> Balance : " + formatter.format(userBalanceList.get(2)) + " LP", false );
        eb.addField("4th", "<@" + userIDList.get(3) + "> Balance : " + formatter.format(userBalanceList.get(3)) + " LP", false );
        eb.addField("5th", "<@" + userIDList.get(4) + "> Balance : " + formatter.format(userBalanceList.get(4)) + " LP", false );
        event.reply(eb.build());

    }

    public void localEconomyLeaderboard(CommandEvent event){
        List<Member> userIDList = new ArrayList<>();
        List<Long> userBalanceList = new ArrayList<>();
        List<String> userIDList2 = new ArrayList<>();
        userIDList = event.getGuild().getMembersWithRoles();
        String serverID = event.getGuild().getId();
        DecimalFormat formatter = new DecimalFormat("#,###");

        Bson sort = descending("balance");
        FindIterable<Document> docs = db.mongoCo.find().sort(sort);
        for(int i = 0; i < userIDList.size(); i++) {
            System.out.println(userIDList.get(i).getUser().getName());
            for (Document doc : docs) {
                String userID = doc.getString("userID");
                if(userIDList.get(i).getId().equalsIgnoreCase(userID)) {
                    long userBalance = doc.getLong("balance");
                    userIDList2.add(userID);
                    userBalanceList.add(userBalance);
                }

            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Economy Leaderboard for " + event.getGuild().getName());
        eb.setDescription("");
        for(int j = 0; j < userIDList2.size() && j <= 4; j++){
            eb.addField((j+1)+ ": ", "<@" + userIDList2.get(j) + "> Balance : " + formatter.format(userBalanceList.get(j)) + " LP", false );
        }
        event.reply(eb.build());

    }

    //Will generate the wins leaderboard

    public void winsLeaderBoard(CommandEvent event){
        List<String> userIDList = new ArrayList<>();
        List<Long> userWinsList = new ArrayList<>();
        Bson sort = descending("wins");
        FindIterable<Document> docs = db.mongoCo.find().sort(sort);
        for(Document doc : docs){
            String userID = doc.getString("userID");
            Long wins = doc.getLong("wins");
            userIDList.add(userID);
            userWinsList.add(wins);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Wins Leaderboard");
        eb.setDescription("");
        eb.addField("1st", "<@" + userIDList.get(0) + "> Wins " + userWinsList.get(0), false );
        eb.addField("2nd", "<@" + userIDList.get(1) + "> Wins " + userWinsList.get(1), false );
        eb.addField("3rd", "<@" + userIDList.get(2) + "> Wins " + userWinsList.get(2), false );
        eb.addField("4th", "<@" + userIDList.get(3) + "> Wins " + userWinsList.get(3), false );
        eb.addField("5th", "<@" + userIDList.get(4) + "> Wins " + userWinsList.get(4), false );
        event.reply(eb.build());
    }

    //will generate the loses leaderboard

    public void losesLeaderBoard(CommandEvent event){
        List<String> userIDList = new ArrayList<>();
        List<Long> userLosesList = new ArrayList<>();
        Bson sort = descending("loses");
        FindIterable<Document> docs = db.mongoCo.find().sort(sort);
        for(Document doc : docs){
            String userID = doc.getString("userID");
            Long loses = doc.getLong("loses");
            userIDList.add(userID);
            userLosesList.add(loses);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Loses Leaderboard");
        eb.setDescription("");
        eb.addField("1st", "<@" + userIDList.get(0) + "> Loses " + userLosesList.get(0), false );
        eb.addField("2nd", "<@" + userIDList.get(1) + "> Loses " + userLosesList.get(1), false );
        eb.addField("3rd", "<@" + userIDList.get(2) + "> Loses " + userLosesList.get(2), false );
        eb.addField("4th", "<@" + userIDList.get(3) + "> Loses " + userLosesList.get(3), false );
        eb.addField("5th", "<@" + userIDList.get(4) + "> Loses " + userLosesList.get(4), false );
        event.reply(eb.build());
    }


}
