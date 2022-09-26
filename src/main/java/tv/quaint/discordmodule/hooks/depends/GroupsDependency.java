package tv.quaint.discordmodule.hooks.depends;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.modules.ModuleManager;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineGroups;
import net.streamline.api.holders.ModuleDependencyHolder;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;
import tv.quaint.discordmodule.hooks.Hooks;
import tv.quaint.savable.GroupManager;
import tv.quaint.savable.guilds.GuildChatEvent;
import tv.quaint.savable.guilds.SavableGuild;
import tv.quaint.savable.parties.PartyChatEvent;
import tv.quaint.savable.parties.SavableParty;

import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class GroupsDependency extends ModuleDependencyHolder<StreamlineGroups> {
    @Getter @Setter
    private GroupsListener groupsListener;

    public GroupsDependency() {
        super("streamline-groups", "streamline-groups");
        if (super.isPresent()) {
            tryLoad(() -> {
                nativeLoad();
                setGroupsListener(new GroupsListener());
                ModuleUtils.listen(getGroupsListener(), DiscordModule.getInstance());
                return null;
            });
        } else {
            SLAPI.getInstance().getMessenger().logInfo("Did not detect a '" + getIdentifier() + "' plugin... Disabling support for '" + getIdentifier() + "'...");
        }
    }

    public static class GroupsListener implements StreamlineListener {
        @EventProcessor
        public void onGuildChat(GuildChatEvent event) {
            DiscordHandler.getLoadedRoutes().forEach((s, route) -> {
                if (route.getInput().getType().equals(EndPointType.GUILD)) route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
            });
        }

        @EventProcessor
        public void onPartyChat(PartyChatEvent event) {
            DiscordHandler.getLoadedRoutes().forEach((s, route) -> {
                if (route.getInput().getType().equals(EndPointType.PARTY)) route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
            });
        }
    }

    public ConcurrentSkipListMap<String, StreamlineUser> getGuildMembersOf(String uuid) {
        ConcurrentSkipListMap<String, StreamlineUser> r = new ConcurrentSkipListMap<>();
        if (! isPresent()) return r;
        SavableGuild guild = GroupManager.getGroup(SavableGuild.class, uuid);
        if (guild == null) return r;

        guild.getAllUsers().forEach(user -> {
            r.put(user.getUuid(), user);
        });

        return r;
    }

    public ConcurrentSkipListMap<String, StreamlineUser> getPartyMembersOf(String uuid) {
        ConcurrentSkipListMap<String, StreamlineUser> r = new ConcurrentSkipListMap<>();
        if (! isPresent()) return r;
        SavableParty party = GroupManager.getGroup(SavableParty.class, uuid);
        if (party == null) return r;

        party.getAllUsers().forEach(user -> {
            r.put(user.getUuid(), user);
        });

        return r;
    }
}
