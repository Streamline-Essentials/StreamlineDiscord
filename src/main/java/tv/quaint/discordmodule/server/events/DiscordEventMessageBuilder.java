package tv.quaint.discordmodule.server.events;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import net.streamline.api.configs.StreamlineStorageUtils;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.server.ServerEvent;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;
import tv.quaint.discordmodule.server.events.messaging.keyed.*;

import java.io.File;
import java.io.FileReader;

public class DiscordEventMessageBuilder {
    @Getter
    static final String subChannel = "discord-event-message";

    public static ProxiedMessage build(EventMessageInfo messageInfo, StreamlinePlayer carrier) {
        ProxiedMessage r = new ProxiedMessage(carrier, ! DiscordHandler.isBackEnd());

        r.setSubChannel(subChannel);

        r.write("type", messageInfo.getType().name());
        r.write("message-info", messageInfo.read());

        return r;
    }

    public static void handle(ProxiedMessage message) {
        if (! message.getSubChannel().equals(subChannel)) {
            DiscordModule.getInstance().logWarning("DiscordEventMessageBuilder.handle() was called with a message that was not for its subchannel!");
            return;
        }

        try {
            String type = message.getString("type");
            String messageInfo = message.getString("message-info");

            EventMessageInfo.EventType eventType = EventMessageInfo.EventType.valueOf(type);

            switch (eventType) {
                case LOGIN, LOGOUT -> {
                    PlayerKey playerKey = new PlayerKey("");
                    playerKey.implement(messageInfo);

                    DiscordHandler.getLoadedChanneledFolders().forEach((s, channeledFolder) -> {
                        channeledFolder.getLoadedRoutes().forEach((s1, route) -> {
                            if (route.getInput().getType() == EndPointType.SPECIFIC_NATIVE) {
                                if (route.getInput().getIdentifier().equals(message.getCarrier().getLatestServer())) {
                                    if (route.getOutput().getType() == EndPointType.DISCORD_TEXT) {
                                        String reply = eventType == EventMessageInfo.EventType.LOGIN ? DiscordModule.getMessages().forwardedStreamlineLogin() : DiscordModule.getMessages().forwardedStreamlineLogout();
                                        if (isJsonFile(reply)) {
                                            String json = getJsonFromFile(getJsonFile(reply));

                                            StreamlineUser user = ModuleUtils.getOrGetUser(playerKey.getValue());
                                            if (user == null) {
                                                DiscordModule.getInstance().logWarning("DiscordEventMessageBuilder.handle() could not find a user with the identifier " + playerKey.getValue() + "!");
                                                return;
                                            }

                                            json = ModuleUtils.replaceAllPlayerBungee(user, json);

                                            DiscordMessenger.sendSimpleEmbed(Long.parseLong(route.getOutput().getIdentifier()), ModuleUtils.stripColor(
                                                    ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json)));
                                        } else {
                                            DiscordMessenger.sendMessage(Long.parseLong(route.getOutput().getIdentifier()), reply);
                                        }
                                    }
                                }
                            }
                        });
                    });
                }
                case DEATH -> {
                    DeathSetKey deathSetKey = new DeathSetKey(message.getCarrier(), "", false, false);
                    deathSetKey.implement(messageInfo);

                    DiscordHandler.getLoadedChanneledFolders().forEach((s, channeledFolder) -> {
                        channeledFolder.getLoadedRoutes().forEach((s1, route) -> {
                            if (route.getInput().getType() == EndPointType.SPECIFIC_NATIVE) {
                                if (route.getInput().getIdentifier().equals(message.getCarrier().getLatestServer())) {
                                    if (route.getOutput().getType() == EndPointType.DISCORD_TEXT) {
                                        String reply = DiscordModule.getMessages().forwardedSpigotDeath();
                                        if (isJsonFile(reply)) {
                                            String json = getJsonFromFile(getJsonFile(reply));

                                            PlayerKey playerKey = (PlayerKey) deathSetKey.get(PlayerKey.getRegistryValue());

                                            StreamlineUser user = ModuleUtils.getOrGetUser(playerKey.getValue());
                                            if (user == null) {
                                                DiscordModule.getInstance().logWarning("DiscordEventMessageBuilder.handle() could not find a user with the identifier " + playerKey.getValue() + "!");
                                                return;
                                            }

                                            json = ModuleUtils.replaceAllPlayerBungee(user, json);

                                            DeathMessageKey deathMessageKey = (DeathMessageKey) deathSetKey.get(DeathMessageKey.getRegistryValue());
                                            DeathKeepExperienceKey deathKeepExperienceKey = (DeathKeepExperienceKey) deathSetKey.get(DeathKeepExperienceKey.getRegistryValue());
                                            DeathKeepInventoryKey deathKeepInventoryKey = (DeathKeepInventoryKey) deathSetKey.get(DeathKeepInventoryKey.getRegistryValue());

                                            json = json
                                                    .replace("%this_death_message%", ModuleUtils.stripColor(deathMessageKey.getValue()))
                                                    .replace("%this_keep_experience%", ModuleUtils.stripColor(deathKeepExperienceKey.getValue() ? "Yes" : "No"))
                                                    .replace("%this_keep_inventory%", ModuleUtils.stripColor(deathKeepInventoryKey.getValue() ? "Yes" : "No"));
                                                    ;

                                            DiscordMessenger.sendSimpleEmbed(Long.parseLong(route.getOutput().getIdentifier()), ModuleUtils.stripColor(
                                                    ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json)));
                                        } else {
                                            DiscordMessenger.sendMessage(Long.parseLong(route.getOutput().getIdentifier()), reply);
                                        }
                                    }
                                }
                            }
                        });
                    });
                }
                case ADVANCEMENT -> {
                    AdvancementSetKey advancementSetKey = new AdvancementSetKey(message.getCarrier(), "", "", "");
                    advancementSetKey.implement(messageInfo);

                    DiscordHandler.getLoadedChanneledFolders().forEach((s, channeledFolder) -> {
                        channeledFolder.getLoadedRoutes().forEach((s1, route) -> {
                            if (route.getInput().getType() == EndPointType.SPECIFIC_NATIVE) {
                                if (route.getInput().getIdentifier().equals(message.getCarrier().getLatestServer())) {
                                    if (route.getOutput().getType() == EndPointType.DISCORD_TEXT) {
                                        String reply = DiscordModule.getMessages().forwardedSpigotAdvancement();
                                        if (isJsonFile(reply)) {
                                            String json = getJsonFromFile(getJsonFile(reply));

                                            PlayerKey playerKey = (PlayerKey) advancementSetKey.get(PlayerKey.getRegistryValue());

                                            StreamlineUser user = ModuleUtils.getOrGetUser(playerKey.getValue());
                                            if (user == null) {
                                                DiscordModule.getInstance().logWarning("DiscordEventMessageBuilder.handle() could not find a user with the identifier " + playerKey.getValue() + "!");
                                                return;
                                            }

                                            json = ModuleUtils.replaceAllPlayerBungee(user, json);

                                            AdvancementTitleKey advancementTitleKey = (AdvancementTitleKey) advancementSetKey.get(AdvancementTitleKey.getRegistryValue());
                                            AdvancementDescriptionKey advancementDescriptionKey = (AdvancementDescriptionKey) advancementSetKey.get(AdvancementDescriptionKey.getRegistryValue());
                                            AdvancementCriteriaKey advancementCriteriaKey = (AdvancementCriteriaKey) advancementSetKey.get(AdvancementCriteriaKey.getRegistryValue());

                                            json = json
                                                    .replace("%this_advancement_title%", ModuleUtils.stripColor(advancementTitleKey.getValue()))
                                                    .replace("%this_advancement_description%", ModuleUtils.stripColor(advancementDescriptionKey.getValue()))
                                                    .replace("%this_advancement_criteria%", ModuleUtils.stripColor(advancementCriteriaKey.getValue()))
                                                    ;

                                            DiscordMessenger.sendSimpleEmbed(Long.parseLong(route.getOutput().getIdentifier()), ModuleUtils.stripColor(
                                                    ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json)));
                                        } else {
                                            DiscordMessenger.sendMessage(Long.parseLong(route.getOutput().getIdentifier()), reply);
                                        }
                                    }
                                }
                            }
                        });
                    });
                }
            }
        } catch (Exception e) {
            DiscordModule.getInstance().logWarning("DiscordEventMessageBuilder.handle() was called with a message that was not formatted correctly!");
            e.printStackTrace();
        }
    }

    public static void loadFile(String name) {
        StreamlineStorageUtils.ensureFileFromSelfModule(
                DiscordModule.getInstance(),
                DiscordHandler.getForwardedJsonsFolder(),
                new File(DiscordHandler.getForwardedJsonsFolder(), name),
                name
        );
    }

    public static String getJsonFromFile(String fileName) {
        File[] files = DiscordHandler.getForwardedJsonsFolder().listFiles((dir, currentFile) -> currentFile.equals(fileName));

        if (files == null) return "";

        if (files.length < 1) return "";

        try {
            return JsonParser.parseReader(new JsonReader(new FileReader(files[0]))).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isJsonFile(String wholeInput) {
        if (! wholeInput.startsWith("--file:")) return false;
        wholeInput = wholeInput.substring("--file:".length());
        return wholeInput.endsWith(".json");
    }

    public static String getJsonFile(String wholeInput) {
        if (wholeInput.startsWith("--file:")) {
            wholeInput = wholeInput.substring("--file:".length());
        }
        if (! wholeInput.endsWith(".json")) {
            wholeInput = wholeInput + ".json";
        }
        return wholeInput;
    }
}
