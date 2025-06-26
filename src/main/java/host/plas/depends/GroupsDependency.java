package host.plas.depends;

import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.events.processing.BaseProcessor;
import host.plas.data.parties.PartyChatEvent;
import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.StreamlineGroups;
import host.plas.discord.saves.obj.channeling.EndPointType;
import host.plas.discord.saves.obj.channeling.RoutedUser;
import singularity.holders.ModuleDependencyHolder;
import singularity.modules.ModuleUtils;

import java.util.concurrent.ConcurrentSkipListMap;

public class GroupsDependency extends ModuleDependencyHolder<StreamlineGroups> {
    @Getter @Setter
    private GroupsListener groupsListener;

    public GroupsDependency() {
        super("streamline-groups", "streamline-groups");
        if (super.isPresent()) {
            tryLoad(() -> {
                nativeLoad();
                if (getGroupsListener() == null) {
                    setGroupsListener(new GroupsListener());
                    ModuleUtils.listen(getGroupsListener(), DiscordModule.getInstance());
                }
                return null;
            });
        } else {
            DiscordModule.getInstance().logInfo("Did not detect a '" + getIdentifier() + "' module... Disabling support for '" + getIdentifier() + "'...");
        }
    }

    public static class GroupsListener implements BaseEventListener {
//        @BaseProcessor
//        public void onGuildChat(GuildChatEvent event) {
//            if (! DiscordModule.getConfig().allowStreamlineGuildsToDiscord()) return;
//
//
//            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
//                folder.getLoadedRoutes().forEach((s, route) -> {
//                    if (route.getInput().getType().equals(EndPointType.GUILD)) route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
//                });
//            });
//        }

        @BaseProcessor
        public void onPartyChat(PartyChatEvent event) {
            if (! DiscordModule.getConfig().allowStreamlinePartiesToDiscord()) return;

            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                folder.getLoadedRoutes().forEach((s, route) -> {
                    if (route.getInput().getType().equals(EndPointType.PARTY))
                        route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
                });
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
