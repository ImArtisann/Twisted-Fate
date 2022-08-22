package Commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class odds extends Command {

    //odds command so users can understand how the bonus is calculated

    public odds(){
        this.name = "odds";
        this.help = "use this command to understand the odds of when placing a bet";
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.MAGENTA);
        eb.setTitle("How the betting odds work on the bot");
        eb.setDescription("Each objective in the game baron, dragon, rift herald, towers, and inhibitors all have a percentage value to them and get x by the amount of those objects claimed");
        eb.addField("Baron Kills: " , " .05%" , true);
        eb.addField("Dragon Kills: " , " .02%" , true);
        eb.addField("RiftHerald kills: " , " .05%" , true);
        eb.addField("Tower Kills: " , " .016%" , true);
        eb.addField("Inhibitor kills: " , " .033%" , true);
        event.reply(eb.build());

    }
}
