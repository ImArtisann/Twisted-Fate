package Commands;

import Database.mongo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public class info extends Command {

    public mongo db = new mongo();


    public info(){
        this.name = "info";
        this.help = "This command just shows info about the bot";
    }

    public info(SlashCommandEvent event){
        System.out.println("hi");
        execute2(event);
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getAuthor().isBot()) return;

        long users = db.mongoCo.countDocuments();
        long servers = db.shopCo.countDocuments();

        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(Color.cyan);
        eb.setTitle("Twisted Fate bot stats");
        eb.setDescription("Wanna add the bot to your sever use this link : " +
                "https://discord.com/api/oauth2/authorize?client_id=804091938361573447&permissions=2110094449&scope=bot");
        eb.addField("Number of registered users :", users+"", true);
        eb.addField("Number of servers : ", servers + "" , true);
        event.reply(eb.build());

    }

    protected void execute2(SlashCommandEvent event) {

        System.out.println("hi");

        long users = db.mongoCo.countDocuments();
        long servers = db.shopCo.countDocuments();

        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(Color.cyan);
        eb.setTitle("Twisted Fate bot stats");
        eb.setDescription("Wanna add the bot to your sever use this link : " +
                "https://discord.com/api/oauth2/authorize?client_id=804091938361573447&permissions=2110094449&scope=bot");
        eb.addField("Number of registered users :", users+"", true);
        eb.addField("Number of servers : ", servers + "" , true);
        TextChannel channel = event.getTextChannel();
        channel.sendMessage(eb.build()).queue();
        event.reply("").queue();

    }
}
