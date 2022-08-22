package Listeners;

import Database.mongo;
import Database.topgg;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.*;

public class onLeave extends ListenerAdapter {

    public topgg stats = new topgg();

    public mongo db = new mongo();

    @Override
    public void onGuildLeave (GuildLeaveEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Left a Server");
        eb.setDescription("Left GuildID " + event.getGuild().getId());
        eb.addField("Servers Name", event.getGuild().getName(), false);
        event.getJDA().getGuildById("747272674853519451").getTextChannelById("804048020500250624").sendMessage(eb.build()).queue();

        Document doc = (Document) db.shopCo.find(new Document("serverID", event.getGuild().getId())).first();

        db.shopCo.deleteOne(doc);

        long stats2 = db.shopCo.countDocuments();
        stats.statsapi.setStats((int) stats2);
    }

}
