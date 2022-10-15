package tv.quaint.discordmodule.discord.messaging;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.savables.users.StreamlineUser;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesSentStat;

import java.awt.*;
import java.net.URL;
import java.util.Optional;

public class DiscordMessenger {
    public static void incrementMessageCountOut() {
        DiscordModule.getBotStats().getMessagesSentStat().increment();
    }

    public static void incrementMessageCountIn() {
        DiscordModule.getBotStats().getMessagesRecievedStat().increment();
    }

    public static void incrementMessageCountInBots() {
        DiscordModule.getBotStats().getBotMessagesRecievedStat().increment();
    }

    public static void sendMessage(long channelId, String message, StreamlineUser on, boolean format) {
        if (format) message = ModuleUtils.replaceAllPlayerBungee(on, message);

        Optional<TextChannel> optionalChannel = DiscordHandler.getTextChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to TextChannel with ID of '" + channelId + "', but failed.");
            return;
        }
        TextChannel channel = optionalChannel.get();

        MessageBuilder builder = new MessageBuilder(); // https://javacord.org/wiki/basic-tutorials/message-builder.html
        builder.append(message);
        builder.send(channel);

        incrementMessageCountOut();
    }

    public static void sendMessage(long channelId, String message, StreamlineUser user) {
        sendMessage(channelId, message, user, true);
    }

    public static void sendMessage(long channelId, String message, boolean format) {
        sendMessage(channelId, message, ModuleUtils.getConsole(), format);
    }

    public static void sendMessage(long channelId, String message) {
        sendMessage(channelId, message, ModuleUtils.getConsole(), true);
    }

    /**
     * Converts a {@link JsonObject} to {@link Embed}.
     * Supported Fields: Title, Author, Description, Color, Fields, Thumbnail, Footer.
     *
     * @param json The JsonObject
     * @return The Embed
     */
    public static SingleSet<BotMessageConfig, EmbedBuilder> jsonToEmbed(JsonObject json){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        BotMessageConfig botMessageConfig = new BotMessageConfig(json);

        JsonPrimitive titleObj = json.getAsJsonPrimitive("title");
        if (titleObj != null){ // Make sure the object is not null before adding it onto the embed.
            embedBuilder.setTitle(titleObj.getAsString());
        }

        JsonObject authorObj = json.getAsJsonObject("author");
        if (authorObj != null) {
            String authorName = authorObj.get("name").getAsString();
            String authorUrl = authorObj.get("author_url").getAsString();
            String authorIconUrl = authorObj.get("icon_url").getAsString();
            if (authorUrl != null && authorIconUrl != null) // Make sure the icon_url is not null before adding it onto the embed. If its null then add just the author's name.
                embedBuilder.setAuthor(authorName, authorUrl, authorIconUrl);
            else
                embedBuilder.setAuthor(authorName);
        }

        JsonPrimitive descObj = json.getAsJsonPrimitive("description");
        if (descObj != null){
            embedBuilder.setDescription(descObj.getAsString());
        }

        JsonPrimitive colorObj = json.getAsJsonPrimitive("color");
        if (colorObj != null){
            Color color = new Color(colorObj.getAsInt());
            embedBuilder.setColor(color);
        }

        JsonArray fieldsArray = json.getAsJsonArray("fields");
        if (fieldsArray != null) {
            // Loop over the fields array and add each one by order to the embed.
            fieldsArray.forEach(ele -> {
                String name = ele.getAsJsonObject().get("name").getAsString();
                String content = ele.getAsJsonObject().get("value").getAsString();
                boolean inline = ele.getAsJsonObject().get("inline").getAsBoolean();
                embedBuilder.addField(name, content, inline);
            });
        }

        JsonPrimitive thumbnailObj = json.getAsJsonPrimitive("thumbnail");
        if (thumbnailObj != null){
            embedBuilder.setThumbnail(thumbnailObj.getAsString());
        }

        JsonObject footerObj = json.getAsJsonObject("footer");
        if (footerObj != null){
            String content = footerObj.get("text").getAsString();
            String footerIconUrl = footerObj.get("icon_url").getAsString();

            if (footerIconUrl != null)
                embedBuilder.setFooter(content, footerIconUrl);
            else
                embedBuilder.setFooter(content);
        }

        return new SingleSet<>(botMessageConfig, embedBuilder);
    }

    public static void sendSimpleEmbed(long channelId, String message, String title, StreamlineUser on, boolean formatMessage, boolean formatTitle, String authorName, String authorUrl, String iconUrl) {
        if (formatMessage) message = ModuleUtils.stripColor(ModuleUtils.replaceAllPlayerBungee(on, message));
        if (formatTitle) title = ModuleUtils.stripColor(ModuleUtils.replaceAllPlayerBungee(on, title));

        Optional<TextChannel> optionalChannel = DiscordHandler.getTextChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to TextChannel with ID of '" + channelId + "', but failed.");
            return;
        }
        TextChannel channel = optionalChannel.get();

        MessageBuilder builder = new MessageBuilder(); // https://javacord.org/wiki/basic-tutorials/message-builder.html
        builder.append(new EmbedBuilder()
                .setAuthor(ModuleUtils.stripColor(authorName), authorUrl, iconUrl)
                .setTitle(title)
                .setDescription(message)
        );
        builder.send(channel);
        incrementMessageCountOut();
    }

    public static void sendSimpleEmbed(long channelId, String message, String title, StreamlineUser on, boolean formatMessage, boolean formatTitle) {
        sendSimpleEmbed(channelId, message, title, on, formatMessage, formatTitle,
                on.getName(), "https://dsc.gg/streamline-project", ModuleUtils.replaceAllPlayerBungee(on, "%discord_user_avatar_url%"));
    }

    public static void sendSimpleEmbed(long channelId, String jsonString) {
        Optional<TextChannel> optionalChannel = DiscordHandler.getTextChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to TextChannel with ID of '" + channelId + "', but failed.");
            return;
        }
        TextChannel channel = optionalChannel.get();

        SingleSet<BotMessageConfig, EmbedBuilder> set = jsonToEmbed((JsonObject) JsonParser.parseString(jsonString));

        BotMessageConfig config = set.getKey();
        if (config.isAvatarChange()) {
            DiscordHandler.updateBotAvatar(config.getChangeOnMessage());
        }

        MessageBuilder builder = new MessageBuilder(); // https://javacord.org/wiki/basic-tutorials/message-builder.html
        builder.setEmbeds(set.getValue());
        builder.send(channel);
        if (config.isAvatarChange()) {
            DiscordHandler.updateBotAvatar(config.getChangeAfterMessage());
        }
        incrementMessageCountOut();
    }
}
