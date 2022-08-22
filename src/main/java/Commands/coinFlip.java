package Commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.File;
import java.util.Random;

public class coinFlip extends Command {

    public coinFlip(){
        this.name = "flip";
        this.help = "use this command to do a coin flip";
        this.aliases = new String[]{ "cf","coinflip" };
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        Random rand = new Random();
        int random = rand.nextInt(100)+1;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.cyan);
        eb.setTitle("Coin flip results");
        eb.setDescription("");
        eb.setImage("https://media3.giphy.com/media/PLJ3gbNlkSVDL3IZlp/giphy.gif");
        if(random>50){
            eb.addField("The results of the coin flip is: " , "Tails", true);
        }else{
            eb.addField("The results of the coin flip is: " , "Heads", true);
        }

        event.reply(eb.build());

    }
}
