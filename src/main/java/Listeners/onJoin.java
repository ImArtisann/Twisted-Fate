package Listeners;

import Database.mongo;
import Database.topgg;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.*;
import java.util.EnumSet;
import java.util.Timer;

public class onJoin extends ListenerAdapter {

    public mongo db = new mongo();
    public topgg stats = new topgg();

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        // Creating text channels when the bot joins a server to prevent spam in the general chat
        try {
            e.getGuild().createCategory("Twisted Fate").complete();
            e.getGuild().getCategoriesByName("Twisted Fate", true).get(0).createTextChannel("Place Bets Here").complete();
        }catch (Exception e1){
            System.out.println(e1);
        }

        Document doc = (Document) db.shopCo.find(new Document("serverID", e.getGuild().getId())).first();

        if(doc == null){
            Document doc2 = (Document) new Document("serverID", e.getGuild().getId());
            doc2.append("roles", "");
            doc2.append("commands", "");
            db.shopCo.insertOne(doc2);
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Joined A New Server");
        eb.setDescription("Joined GuildID " + e.getGuild().getId());
        eb.addField("Servers Name", e.getGuild().getName(), false);
        e.getJDA().getGuildById("747272674853519451").getTextChannelById("804048020500250624").sendMessage(eb.build()).queue();

        long stats2 = db.shopCo.countDocuments();
        stats.statsapi.setStats((int) stats2);



    }
}
