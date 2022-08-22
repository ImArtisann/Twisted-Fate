package Commands;

import Database.mongo;
import Database.riotAPI;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeagueEntry;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.ParticipantIdentity;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class bet extends Command {
    /*
     * api allows me to get data from the riot games api
     * db connects me to my mongodb database
     * betTracking allows me to access the hashmap in active bets file
     */
    public riotAPI api = new riotAPI();
    public mongo db = new mongo();
    public activeBets betTracking = new activeBets();
    public Platform summonerPlatform = Platform.NA;

    public bet() {
        this.name = "bet";
        this.help = "Use this command to place bets on a summoner example: +bet [Summoner Name],[Bet],[Win/Lose]";
        this.aliases = new String[]{"b"};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) return;


        if (event.getArgs().isEmpty()) {
            event.reply("You didnt give me any information please use the command like so +bet [Summoner]," +
                    "[Bet ammount],[Win/lose],[Region is set to NA default to change put either " +
                    " (br,eune,euw,lan,las,oce,ru,tr,jp,kr) here]] ");
        } else {
            try {
                String[] args = event.getArgs().split(",");

                long bet = wrongOrderIdiotProof(args);
                String winOrLose = wrongOrderIdiotProof2(args);
                String fix = winOrLoseIdiotProof(winOrLose);
                try {
                    if (!args[3].isEmpty()) {
                        setPlatform(args[3].replaceAll("\\s", ""));
                    }
                } catch (Exception e) {
                    //user is using default region NA
                }
                //Making sure the user
                if (fix.equalsIgnoreCase("win") || fix.equalsIgnoreCase("lose")) {
                    try {
                        Summoner summoner = api.api.getSummonerByName(summonerPlatform, args[0].trim());
                        long gameID = summonerInGame(summoner);
                        if (gameID > 0) {
                            if (userBalanceEnough(event, event.getAuthor().getId(), bet)) {
                                if (api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId()).getGameLength() <= 120) {
                                    placeBet(event.getAuthor().getId(), bet, summoner, event, winOrLose);
                                    betLogger(event, event.getAuthor().getId(), bet, winOrLose, summoner);
                                    checkResults(event.getAuthor().getId(), summoner, bet, gameID, winOrLose, event);
                                } else {
                                    event.reply("Sorry you are past the betting time to place a bet on this match. " +
                                            "You must place a bet before 5 mins into the game");
                                }
                            } else {
                                event.reply("You dont have enough LP to place that bet please place a bet you can afford");
                            }
                        } else {
                            event.reply("User is currently not in a game");
                        }
                    } catch (Exception e) {
                        event.reply("Chances are you either spelled their name wrong or you are betting on a summoner " +
                                "thats not on the NA server");
                        System.out.println("Error at the second part of the bet command " + e);
                        if (e.equals(RiotApiException.DATA_NOT_FOUND)) {
                            System.out.println("Chances are error");
                            api.riotAPI();
                        }
                    }
                } else {
                    event.reply("You did not enter if theyll win or lose correctly if you want to bet that theyll win " +
                            "ex: +bet Teemo Fking 1,[Bet ammount],win");
                }
            } catch (Exception e) {
                if (e.equals(RiotApiException.DATA_NOT_FOUND)) {
                    System.out.println("You might did the command wrond part");
                    try {
                        api.riotAPI();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                event.reply("You might of did the command wrong please make sure its formatted like so  " +
                        "+bet [Summoner],[Bet ammount],[Win/lose],[Region is set to NA default to change put either" +
                        "(br,eune,euw,lan,las,oce,ru,tr,jp,kr) here]");
                System.out.println("Error at the beginning of the bet command: " + e);
            }
        }
    }

    /*
     *
     */
    public void setPlatform(String plat) {
        if (plat.equalsIgnoreCase("br")) {
            this.summonerPlatform = Platform.BR;
        } else if (plat.equalsIgnoreCase("eune")) {
            this.summonerPlatform = Platform.EUNE;
        } else if (plat.equalsIgnoreCase("euw")) {
            this.summonerPlatform = Platform.EUW;
        } else if (plat.equalsIgnoreCase("lan")) {
            this.summonerPlatform = Platform.LAN;
        } else if (plat.equalsIgnoreCase("las")) {
            this.summonerPlatform = Platform.LAS;
        } else if (plat.equalsIgnoreCase("oce")) {
            this.summonerPlatform = Platform.OCE;
        } else if (plat.equalsIgnoreCase("ru")) {
            this.summonerPlatform = Platform.RU;
        } else if (plat.equalsIgnoreCase("tr")) {
            this.summonerPlatform = Platform.TR;
        } else if (plat.equalsIgnoreCase("JP")) {
            this.summonerPlatform = Platform.JP;
        } else if (plat.equalsIgnoreCase("kr")) {
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
    public long summonerInGame(Summoner summoner) {
        CurrentGameInfo match;
        try {
            match = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId());

        } catch (Exception e) {
            return 0;
        }
        return match.getGameId();
    }

    /*
     * userBalanceEnough is a method to check the user thats betting to make sure their balance is enough
     * to support the bet they are doing
     */
    public boolean userBalanceEnough(CommandEvent event, String userID, long bet) {
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        if (doc == null) {
            event.reply("You currently do not have an account registered with me please use command +register");
        } else {
            long balance = doc.getLong("balance");
            if (balance >= bet) {
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
    public void placeBet(String userID, long bet, Summoner summoner, CommandEvent event, String winOrLose) {
        ArrayList<String> checker = new ArrayList<>();
        boolean betAlreadyPlaced = false;
        if (betTracking.map.containsKey(userID)) {
//            checker = betTracking.map.get(userID);
//            for(int i =0; i<checker.size(); i++){
//                String[] args = checker.get(i).split(",");
//                if(args[0].equalsIgnoreCase(summoner.getName())){
//                    betAlreadyPlaced = true;
//                }
            if (noDualBetting(summoner, userID)) {
                betAlreadyPlaced = true;
            }
        }/*else{
            checker.add(summoner.getName()+","+bet +","+ winOrLose);
            betTracking.map.put(userID, checker);
        }*/
        if (betAlreadyPlaced == true) {
            event.reply("You have already place a bet on that game");
        } else {
            if (betTracking.map.containsKey(userID)) {
                checker = betTracking.map.get(userID);
            }
            Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
            long balance = doc.getLong("balance");

            Bson updatedvalue = new Document("balance", balance - bet);
            Bson updateOperation = new Document("$set", updatedvalue);
            db.mongoCo.updateOne(doc, updateOperation);
            checker.add(summoner.getName() + "," + bet + "," + winOrLose);
            event.reply("We have placed your bet we will update you once the match has finished with the " +
                    "result <@" + event.getAuthor().getId() + "> please give the bot 5-10 mins to update you after match" +
                    "ends");
            if (betTracking.map.containsKey(userID)) {
                betTracking.map.remove(userID);
            }
            betTracking.map.put(userID, checker);
        }

    }

    /*
     * the method checkResults runs a timer when a bet was successfully placed. The timer checks the gameID with the riot
     * api to see if the game is still active or ended. The timer does this every 5 mins once the game ends the timer ends its
     * self and passes the rest over to the results api
     */
    public void checkResults(String userID, Summoner summoner, long bet, long gameID, String winOrLose, CommandEvent event) throws RiotApiException {
        try {
            /*while (summonerInGame(summoner) > 0) {
                System.out.println("Ran");
                Thread.sleep(fiveMinTimer);
            } */
            int MINUTES = 20;
            int i = 0;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int i = 0;
                @Override
                public void run() { // Function runs every MINUTES minutes.

                    if (!gameOver(gameID, summoner)) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("20 min timer starting");
                        if(i>0) {
                            System.out.println("Games going over to 5 min timer");
                            fiveMinTimer(userID,summoner,bet,gameID,winOrLose,event);
                            timer.cancel();
                            timer.purge();
                        }
                        i++;
                    } else {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("[UserID of bet placer] : " + userID + " The summoner " + summoner.getName()
                                + " game has ended current date and time: "
                                + dateFormat.format(date));
                        try {
                            results(summoner, gameID, userID, bet, winOrLose, event);
                        } catch (RiotApiException e) {
                            try {
                                Thread.sleep(180000);
                                checkResults(userID, summoner, bet, gameID, winOrLose, event);
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
                                                " Win/Lose : " + winOrLose).queue();
                            }
                        }
                        timer.cancel();
                        timer.purge();
                    }
                }
            }, 0, 1000 * 60 * MINUTES);

        } catch (Exception e) {
            System.out.println("Error in the checkresults method " + e);
        }

    }
    /* five min timer trying to stop getting rate limited

     */

    public void fiveMinTimer(String userID, Summoner summoner, long bet, long gameID, String winOrLose, CommandEvent event) {
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
            }else if (!gameOver(gameID, summoner)) {
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
                        results(summoner, gameID, userID, bet, winOrLose, event);
                    } catch (RiotApiException e) {
                        try {
                            Thread.sleep(180000);
                            checkResults(userID, summoner, bet, gameID, winOrLose, event);
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
                                            " Win/Lose : " + winOrLose).queue();
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
    public void results(Summoner summoner, long gameID, String userID, long bet, String winOrLose, CommandEvent event) throws RiotApiException{
        List<ParticipantIdentity> participantIdentities = api.api.getMatch(summonerPlatform, gameID).getParticipantIdentities();
        ArrayList<String> checker = new ArrayList<>();
        checker = betTracking.map.get(userID);
        for(int i = 0; i < checker.size(); i++){
            String[] args = checker.get(i).split(",");
            if(summoner.getName().equalsIgnoreCase(args[0])){
                checker.remove(i);
                break;
            }
        }
        if(checker.size()==0){
            betTracking.map.remove(userID);
        }else {
            betTracking.map.remove(userID);
            betTracking.map.put(userID, checker);
        }
        int team = 0;
        for(int i = 0; i < 10; i++){
            if(summoner.getName().equalsIgnoreCase(participantIdentities.get(i).toString())){
                team = i;
                break;
            }
        }
        if(team<5){
            float winPer = winBonusPercentage(gameID,100);
            if(api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(100).getWin().equalsIgnoreCase("win")){
                if(winOrLose.equalsIgnoreCase("win")){ //user betted that the team would win
                    long win = updateDatabaseWin(userID,bet, winPer,gameID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has won their match and " +
                            "you have guessed correctly. You won a total of: " +  win );
                    postResults(userID,win,"win", event);
                }else{ //user lost the bet as they guessed wrong
                    updateDatabaseLose(userID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has won their match " +
                            "and you have guessed wrong");
                    postResults(userID,bet,"lost", event);
                }
            }else{ //if the team lost
                if(winOrLose.equalsIgnoreCase("lose")){
                    long win = updateDatabaseWin(userID,bet, winPer,gameID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has lost their match and " +
                            "you have guessed correctly. You won a total of: " +  win );
                    postResults(userID,win,"win", event);
                }else{
                    updateDatabaseLose(userID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has lost their match and " +
                            "you have guessed wrong");
                    postResults(userID,bet,"lost", event);
                }
            }
        }else{
            float winPer = winBonusPercentage(gameID,200);
            if(api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(200).getWin().equalsIgnoreCase("Win")){
                if(winOrLose.equalsIgnoreCase("win")) {
                    long win = updateDatabaseWin(userID,bet, winPer,gameID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has won their match and " +
                            "you have guessed correctly. You won a total of: " +  win );
                    postResults(userID,win,"win", event);
                }else{
                    updateDatabaseLose(userID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has won their match and " +
                            "you have guessed wrong");
                    postResults(userID,bet,"lost", event);
                }
            }else{
                if(winOrLose.equalsIgnoreCase("lose")){
                    long win = updateDatabaseWin(userID,bet, winPer,gameID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has lost their match and " +
                            "you have guessed correctly. You won a total of: " +  win + " for the " +
                            "amount of objectives their team got");
                    postResults(userID,win,"win", event);
                }else{
                    updateDatabaseLose(userID);
                    event.reply("<@" + userID +"> The summoner: " + summoner.getName() + " has lost their match and " +
                            "you have guessed wrong");
                    postResults(userID,bet,"lost", event);
                }
            }
        }
    }
    public boolean gameOver(long gameID, Summoner summoner){

        try{
            long currentgame = summonerInGame(summoner);
            if(currentgame != gameID) {
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
    /*
     * updateDatabaseWin method is the method that is only called when the a match is won to update the mongodb with
     * the amount they won from their bet and to update the win count
     */
    public long updateDatabaseWin(String userID, long bet, float winPer, long gameID){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long balance = doc.getLong("balance");
        long winning = bet + bet;
        long winning2 = (int)(bet*winPer);
        long winningbet = winning + winning2;
        if(isAram(gameID)){
            winning = (bet/2);
            winning2 = (int) (bet * winPer);
            winningbet = winning + winning2 + bet;
        }else {
            winning = bet + bet;
            winning2 = (int) (bet * winPer);
            winningbet = winning + winning2;
        }
        Bson updatedvalue = new Document("balance", balance+winningbet);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
        updateWininDB(userID);
        return winningbet;
    }

    /*
     * winBonusPercentage method is used to calculate the bonus they get for the number of objects the team they betted
     * on got
     */
    public float winBonusPercentage(long gameID, int teamId){
        float baron = .05f;
        float dragon = .02f;
        float riftherald = .05f;
        float tower = .016f;
        float inhibitor = .033f;
        float percentage = 0;
        try {
            int baron2 = api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(teamId).getBaronKills();
            int dragon2 = api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(teamId).getDragonKills();
            int rift = api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(teamId).getRiftHeraldKills();
            int tower2 = api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(teamId).getTowerKills();
            int inhibitor2 = api.api.getMatch(summonerPlatform, gameID).getTeamByTeamId(teamId).getInhibitorKills();
            percentage = (baron*baron2)+(dragon*dragon2)+(riftherald*rift)+(tower*tower2)+(inhibitor*inhibitor2);

        }catch (Exception e){
            System.out.println("Error in method winBonusPercentage "+e);
        }
            return  percentage;
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
     * Same as above used to update the lose counter in the database
     */
    public void updateDatabaseLose(String userID){
        Document doc = (Document) db.mongoCo.find(new Document("userID", userID)).first();
        long loses = doc.getLong("loses");
        Bson updatedvalue = new Document("loses", loses + 1L);
        Bson updateOperation = new Document("$set", updatedvalue);
        db.mongoCo.updateOne(doc, updateOperation);
    }
    /*
     * betLogger method will post a message into my discord server under the text channel bet logger of all bets that have been
     * placed
     */
    public void betLogger(CommandEvent event, String userID, long bet, String winOrLose, Summoner summoner){

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.CYAN);
        eb.setTitle("A bet has been placed");
        eb.setDescription("The user <@" + userID +"> has just placed a bet");
        eb.addField("Summoner user is betting on", summoner.getName(),false);
        eb.addField("The user betted this amount ", ""+bet, false);
        eb.addField("The user is hoping the summoner ", winOrLose+"s", false);
        event.getJDA().getGuildById("747272674853519451").getTextChannelById("804047868754395197").sendMessage(eb.build()).queue();

    }
    /*
     * noDualBetting is a method that is used to make sure users are not farming the bonuses from games so a user cant place
     * a bet on the same game they already placed bet on
     */
    public boolean noDualBetting(Summoner summoner, String userID){
        ArrayList<String> checker = new ArrayList<>();

        try {
            if(betTracking.map.containsKey(userID)){
                checker = betTracking.map.get(userID);
                List<CurrentGameParticipant> participantIdentities = api.api.getActiveGameBySummoner(summonerPlatform, summoner.getId()).getParticipants();
                for(int i = 0; i < checker.size(); i++){
                    for(int j = 0; j < participantIdentities.size(); j++){
                        String[] args = checker.get(i).split(",");
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
     * This command is used to make sure that user is not in an aram game
     */

    public boolean isAram(long gameID){

        try {

            String gameMode = api.api.getMatch(summonerPlatform, gameID).getGameMode();

            if(gameMode.equalsIgnoreCase("ARAM")){
                return true;
            }
        }catch (Exception e){
            System.out.println("Theres an error in isAram method " + e);
        }
        return false;
    }

    public long wrongOrderIdiotProof(String[] choices){
        if(isNumeric(choices[1].replaceAll("\\s", ""))){
            return Long.parseLong(choices[1].replaceAll("\\s", ""));
        }else if(isNumeric(choices[2].replaceAll("\\s", ""))){
            return Long.parseLong(choices[2].replaceAll("\\s", ""));
        }
        return 0;
    }
    public String wrongOrderIdiotProof2(String[] choices){
        if(isNumeric(choices[1].replaceAll("\\s", ""))){
            return choices[2].replaceAll("\\s", "");
        }else if(isNumeric(choices[2].replaceAll("\\s", ""))){
            return choices[1].replaceAll("\\s", "");
        }
        return "";
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long L = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String winOrLoseIdiotProof(String c){
        String winOrLose = "";
        switch(c.toLowerCase()){
            case "win" :
                winOrLose = "win";
                break;
            case "w" :
                winOrLose = "win";
                break;
            case "lost" :
                winOrLose = "lose";
                break;
            case "l" :
                winOrLose = "lose";
                break;
            case "lose" :
                winOrLose = "lose";
                break;
            case "loss" :
                winOrLose = "lose";
                break;
            case "throws" :
                winOrLose = "lose";
                break;
            case "carry" :
                winOrLose = "win";
                break;
            case "carries" :
                winOrLose = "win";
                break;
            case "carrys" :
                winOrLose = "win";
                break;
            case "sucks" :
                winOrLose = "lose";
                break;
            case "sweep" :
                winOrLose = "win";
                break;
            case "triumph" :
                winOrLose = "win";
                break;
            case "gain" :
                winOrLose = "win";
                break;
            case "forfeit" :
                winOrLose = "lose";
                break;
            case "defeat" :
                winOrLose = "lose";
                break;
            case "victory" :
                winOrLose = "win";
                break;
            case "failure" :
                winOrLose = "lose";
                break;
            case "loses" :
                winOrLose = "lose";
                break;
        }
        return winOrLose;
    }

    public void postResults(String userID,long bet, String winorLost, CommandEvent e){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The user id <@" + userID + "> Results");
        if(winorLost.equalsIgnoreCase("win")){
            eb.setColor(Color.GREEN);
            eb.setDescription("The user has won his bet");
            eb.addField("Total winnings: ", bet+" LP", true);
        }else{
            eb.setColor(Color.RED);
            eb.appendDescription("The user has lost his bet");
            eb.addField("What the user lost: ", bet + " LP", true);
        }


        e.getJDA().getGuildById("747272674853519451").getTextChannelById("892247027159810098").sendMessage(eb.build()).queue();

    }

//    public int whatTeamIsTheSummonerOn(Summoner summoner, List<ParticipantIdentity> participantIdentities){
//        int decider = 0;
//        int team = 0;
//        for(int i = 0; i<participantIdentities.size(); i++){
//            if(summoner.getName().equalsIgnoreCase(participantIdentities.get(i).toString())){
//                decider = i;
//                break;
//            }
//        }
//        if(decider<5){
//            team = 100;
//        }else{
//            team = 200;
//        }
//
//        return team;
//    }
}
