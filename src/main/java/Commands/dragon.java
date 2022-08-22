package Commands;

import Database.mongo;
import Database.riotAPI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.ParticipantIdentity;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class dragon extends Command {

    public riotAPI api = new riotAPI();
    public mongo db = new mongo();
    public activeBets betTracking = new activeBets();
    public Platform summonerPlatform = Platform.NA;

    public dragon(){
        this.name = "drag";
        this.help = "Use this command to bet on which team is going to get drag first which is a 3x multiplier  +drag " +
                "[Summoner's Name],[Bet amount]";
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        if(event.getArgs().isEmpty()) {
            event.reply("You didnt use the command correctly  +drag [Summoner's Name],[Bet amount],[Optional Region]");
        }else{
            String[] args = event.getArgs().split(",");

            try {
                if (!args[2].isEmpty()) {
                    setPlatform(args[2].replaceAll("\\s", ""));
                }
            }catch (Exception e){
                //user is using default region NA
            }
            try{
                Summoner summoner = api.api.getSummonerByName(summonerPlatform, args[0]);
                long gameID = summonerInGame(summoner);
                long bet = Long.parseLong(args[1].trim());
                if(gameID>0){
                    //CurrentGameInfo match = summonerInGame2(summoner);
                    if(isAram(summoner)){
                        event.reply("You cant use this command on aram games");
                    }else {
                        if (userBalanceEnough(event, event.getAuthor().getId(), bet)) {
                            if (api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId()).getGameLength() <= 120) {
                                placeBet(event.getAuthor().getId(), bet, summoner, event);
                                betLogger(event, event.getAuthor().getId(), bet, summoner);
                                checkResults(event.getAuthor().getId(), summoner, bet, gameID, event);
                            } else {
                                event.reply("Sorry you are past the betting time to place a bet on this match. " +
                                        "You must place a bet before 5 mins into the game");
                            }
                        } else {
                            event.reply("You do not have that much in your balance to bet that much");
                        }
                    }
                }else{
                    event.reply("User is currently not in a game or you didnt give a correct bet value");
                }
            }catch (Exception e){
                System.out.println("A user cause an error in the drag command: " + e);
                event.reply("Sorry could not find that summoner");
            }
        }

    }
    public void setPlatform(String plat){
        if(plat.equalsIgnoreCase("br")){
            this.summonerPlatform = Platform.BR;
        }else if(plat.equalsIgnoreCase("eune")){
            this.summonerPlatform = Platform.EUNE;
        }else if(plat.equalsIgnoreCase("euw")){
            this.summonerPlatform = Platform.EUW;
        }else if(plat.equalsIgnoreCase("lan")){
            this.summonerPlatform = Platform.LAN;
        }else if(plat.equalsIgnoreCase("las")){
            this.summonerPlatform = Platform.LAS;
        }else if(plat.equalsIgnoreCase("oce")){
            this.summonerPlatform = Platform.OCE;
        }else if(plat.equalsIgnoreCase("ru")){
            this.summonerPlatform = Platform.RU;
        }else if(plat.equalsIgnoreCase("tr")){
            this.summonerPlatform = Platform.TR;
        }else if(plat.equalsIgnoreCase("JP")){
            this.summonerPlatform = Platform.JP;
        }else if(plat.equalsIgnoreCase("kr")){
            this.summonerPlatform = Platform.KR;
        }
    }

    /*
     * summonerInGame method returns either a 0 or a match id an example of a match id is 42542561
     * in the try its checking to see if the summoner is in an active game and trying to grab the match id
     * If the summoner is not in a game the riot games api throws a 404 error so we catch that error in the try
     * and return as 0. 0 means they are not in a game. Anything greater than 0 means the summoner they are betting
     * on is in an active game
     */
    public long summonerInGame(Summoner summoner){
        CurrentGameInfo match;
        try {
            match = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId());

        }catch (Exception e){
            return 0;
        }
        return match.getGameId();
    }
    /*
     * summonerInGame method returns either a 0 or a match id an example of a match id is 42542561
     * in the try its checking to see if the summoner is in an active game and trying to grab the match id
     * If the summoner is not in a game the riot games api throws a 404 error so we catch that error in the try
     * and return as 0. 0 means they are not in a game. Anything greater than 0 means the summoner they are betting
     * on is in an active game
     */
    public CurrentGameInfo summonerInGame2(Summoner summoner){
        CurrentGameInfo match = new CurrentGameInfo();
        try {
            match = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId());
            System.out.println(match.getGameMode());
        }catch (Exception e){
            System.out.println("Error in summoner in game 2");
        }
        return match;
    }
    /*
     * userBalanceEnough is a method to check the user thats betting to make sure their balance is enough
     * to support the bet they are doing
     */
    public boolean userBalanceEnough(CommandEvent event, String userID, long bet){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        if(doc == null){
            event.reply("You currently do not have an account registered with me please use command +register");
        }else{
            long balance = doc.getLong("balance");
            if(balance>=bet){
                return true;
            }
        }
        return false;
    }
    /*
     * placeBet method is the method used to place a bet on a summoner
     * The first check that the method does is to check if the user placing the bet has already placed a bet on that
     * summoner. If false itll go onto the next check to make sure the user isnt placing a bet on the enemy team also
     * to farm the bonus the bot gives based off how many objs the winning team got. Onces all conditions are met
     * the bot will then place the bet
     */
    public void placeBet(String userID, long bet, Summoner summoner, CommandEvent event){
        ArrayList<String> checker = new ArrayList<>();
        boolean betAlreadyPlaced = false;
        if(betTracking.drag.containsKey(userID)){
//            checker = betTracking.map.get(userID);
//            for(int i =0; i<checker.size(); i++){
//                String[] args = checker.get(i).split(",");
//                if(args[0].equalsIgnoreCase(summoner.getName())){
//                    betAlreadyPlaced = true;
//                }
            if(noDualBetting(summoner,userID)){
                betAlreadyPlaced = true;
            }
        }/*else{
            checker.add(summoner.getName()+","+bet +","+ winOrLose);
            betTracking.map.put(userID, checker);
        }*/
        if(betAlreadyPlaced == true){
            event.reply("You have already place a bet on that game");
        }else {
            if(betTracking.drag.containsKey(userID)) {
                checker = betTracking.drag.get(userID);
            }
            Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
            long balance = doc.getLong("balance");

            Bson updatedvalue = new Document("balance", balance - bet);
            Bson updateOperation = new Document("$set", updatedvalue);
            db.mongoCo.updateOne(doc, updateOperation);
            checker.add(summoner.getName()+","+bet);
            event.reply("We have placed your bet we will update you once the match has finished with the " +
                    "result <@" + event.getAuthor().getId() + ">");
            if(betTracking.drag.containsKey(userID)) {
                betTracking.drag.remove(userID);
            }
            betTracking.drag.put(userID,checker);
        }

    }
    /*
     * noDualBetting is a method that is used to make sure users are not farming the bonuses from games so a user cant place
     * a bet on the same game they already placed bet on
     */
    public boolean noDualBetting(Summoner summoner, String userID){
        ArrayList<String> checker = new ArrayList<>();

        try {
            if(betTracking.drag.containsKey(userID)){
                checker = betTracking.drag.get(userID);
                List<CurrentGameParticipant> participantIdentities = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId()).getParticipants();
                for(int i = 0; i < checker.size(); i++){
                    for(int j = 0; j < participantIdentities.size(); j++){
                        String[] args = checker.get(i).split(",");
                        System.out.println(participantIdentities.get(j).toString() + " " + args[0]);
                        if(participantIdentities.get(j).toString().equalsIgnoreCase(args[0])){
                            return true;
                        }
                    }
                }
            }


        }catch (Exception e){
            System.out.println("Error in noDualBetting: " + e);
        }

        return false;
    }
    /*
     * betLogger method will post a message into my discord server under the text channel bet logger of all bets that have been
     * placed
     */
    public void betLogger(CommandEvent event, String userID, long bet, Summoner summoner){

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.CYAN);
        eb.setTitle("A bet has been placed");
        eb.setDescription("The user <@" + userID +"> has just placed a bet");
        eb.addField("Summoner user is betting on", summoner.getName(),false);
        eb.addField("The user betted this amount ", ""+bet, false);
        eb.addField("The user is hoping the summoner ", "Kills dragon first", false);
        event.getJDA().getGuildById("747272674853519451").getTextChannelById("804047868754395197").sendMessage(eb.build()).queue();

    }
    /*
     * the method checkResults runs a timer when a bet was successfully placed. The timer checks the gameID with the riot
     * api to see if the game is still active or ended. The timer does this every 5 mins once the game ends the timer ends its
     * self and passes the rest over to the results api
     */
    public void checkResults(String userID, Summoner summoner, long bet, long gameID, CommandEvent event) throws RiotApiException {
        try {
            /*while (summonerInGame(summoner) > 0) {
                System.out.println("Ran");
                Thread.sleep(fiveMinTimer);
            } */
            int MINUTES = 5;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int i = 0;
                @Override
                public void run() { // Function runs every MINUTES minutes.
                    if(summonerInGame(summoner)>0){
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("20 min timer starting");
                        if(i>0) {
                            System.out.println("Games going over to 5 min timer");
                            fiveMinTimer(userID,summoner,bet,gameID,event);
                            timer.cancel();
                            timer.purge();
                        }
                        i++;
                    }else{
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("[UserID of bet placer] : " + userID + " The summoner " + summoner.getName()
                                + " game has ended current date and time: "
                                + dateFormat.format(date));
                        try {
                            results(summoner,gameID,userID,bet,event);
                        } catch (RiotApiException e) {
                            e.printStackTrace();
                        }
                        timer.cancel();
                        timer.purge();
                    }
                }
            }, 0, 1000 * 60 * MINUTES);
        }catch (Exception e){
            System.out.println("Error in the checkresults method " + e);
        }

    }
    public void fiveMinTimer(String userID, Summoner summoner, long bet, long gameID, CommandEvent event) {
        try{
            int MINUTES = 5;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int i = 0;
                @Override
                public void run() { // Function runs every MINUTES minutes.
                    if(i==0) {
                        i++;
                        System.out.println("Skip the first check as checked it first in the 20 min");
                    }else if (summonerInGame(summoner)>0) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("[UserID of bet placer] : " + userID + " The summoner " + summoner.getName()
                                + " is still in game will check again in 5 mins current date and time: "
                                + dateFormat.format(date));
                    } else {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("[UserID of bet placer] : " + userID + " The summoner " + summoner.getName()
                                + " game has ended current date and time: "
                                + dateFormat.format(date));
                        try {
                            results(summoner, gameID, userID, bet, event);
                        } catch (RiotApiException e) {
                            try {
                                Thread.sleep(180000);
                                checkResults(userID, summoner, bet, gameID, event);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                event.reply("<@" + event.getAuthor().getId() + "> Looks like there was an error " +
                                        "with riot api we logged this event please " +
                                        "give me 24 hours to update your profile");
                                event.getJDA().getGuildById("747272674853519451")
                                        .getTextChannelById("807105706037739520")
                                        .sendMessage("New 504 error " +
                                                "User : " + event.getAuthor().getAsTag() +
                                                " UserID : " + event.getAuthor().getId() +
                                                " Summoner : " + summoner.getName() +
                                                " Game ID " + gameID +
                                                " Bet amount : " + bet +
                                                " Win/Lose : " ).queue();
                            }
                        }
                        timer.cancel();
                        timer.purge();
                    }
                }
            }, 0, 1000 * 60 * MINUTES);

        }catch(Exception e) {
            System.out.println("Error in the fiveMinTimer method " + e);
        }
    }
    /*
     * The results method is only called once the game is ended. Results method checks to see if the summoner won or lost
     * their match. Once the results method figures out if they won or lost itll update the user who placed a bet on that
     * summoner whither their guess was right or not and update their balance if they won also updates the win and lost
     * counter
     */
    public void results(Summoner summoner, long gameID, String userID, long bet, CommandEvent event) throws RiotApiException{
        List<ParticipantIdentity> participantIdentities = api.api.getMatch(summonerPlatform, gameID).getParticipantIdentities();
        ArrayList<String> checker = new ArrayList<>();
        checker = betTracking.drag.get(userID);
        System.out.println(checker.size());
        for(int i = 0; i < checker.size(); i++){
            String[] args = checker.get(i).split(",");
            if(summoner.getName().equalsIgnoreCase(args[0])){
                checker.remove(i);
                break;
            }
        }
        if(checker.size()==0){
            betTracking.drag.remove(userID);
        }else {
            betTracking.drag.remove(userID);
            betTracking.drag.put(userID, checker);
        }
        int team = 0;
        for(int i = 0; i < 10; i++){
            if(summoner.getName().equalsIgnoreCase(participantIdentities.get(i).toString())){
                team = i;
                break;
            }
        }
        if(team<5){
            if(api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(100).isFirstDragon()){
                long win  = updateDatabaseWin(userID, bet);
                event.reply("<@" + userID + "> The summoner " + summoner.getName() + " team did get the first dragon for guessing correctly you have won " + win);
                postResults(userID,win,"win", event);
            }else {
                event.reply("<@" + userID + "> The summoner " + summoner.getName() + " team did not get the first dragon");
                postResults(userID,bet,"lose", event);
            }
        }else{
            if(api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(200).isFirstDragon()){
                long win  = updateDatabaseWin(userID, bet);
                event.reply("<@" + userID + "> The summoner " + summoner.getName() + " team did get the first dragon for guessing correctly you have won " + win);
                postResults(userID,win,"win", event);
            }else {
                event.reply("<@" + userID + "> The summoner " + summoner.getName() + " team did not get the first dragon");
                postResults(userID,bet,"lose", event);
            }
        }
    }
    public void postResults(String userID,long bet, String winorLost, CommandEvent e){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The user id <@" + userID + "> Results");
        if(winorLost.equalsIgnoreCase("win")){
            eb.setColor(Color.GREEN);
            eb.setDescription("The user has won his bet on dragon");
            eb.addField("Total winnings: ", bet+" LP", true);
        }else{
            eb.setColor(Color.RED);
            eb.appendDescription("The user has lost his bet on dragon");
            eb.addField("What the user lost: ", bet + " LP", true);
        }


        e.getJDA().getGuildById("747272674853519451").getTextChannelById("892247027159810098").sendMessage(eb.build()).queue();

    }
    /*
     * updateDatabaseWin method is the method that is only called when the a match is won to update the mongodb with
     * the amount they won from their bet and to update the win count
     */
    public long updateDatabaseWin(String userID, long bet){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long balance = doc.getLong("balance");
        long winning = bet + bet;
        long winningbet = winning + bet;
        Bson updatedvalue = new Document("balance", balance+winningbet);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
        updateWininDB(userID);
        return winningbet;
    }
    /*
     * For some reason mongodb is not letting me do 2 update operations in one method so this method is use to update
     * the win tracker
     */
    public void updateWininDB(String userID){
        Document doc2 = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long win = doc2.getLong("wins")+1;
        Bson updatedvalue2 = new Document("wins", win);
        Bson updateOperation2 = new Document("$set", updatedvalue2);
        db.mongoCo.updateOne(doc2, updateOperation2);
    }
    /*
     * This command is used to make sure that user is not in an aram game
     */

    public boolean isAram(Summoner summoner){

        try {

            String gameMode = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getName()).toString();
            System.out.println(gameMode);

            if(gameMode.equalsIgnoreCase("ARAM")){
                return true;
            }
        }catch (Exception e){
            System.out.println("Theres an error in isAram method " + e);
            System.out.println(e);
        }
        return false;
    }
}
