package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.events.ChanneledFolderCreateEvent;
import tv.quaint.discordmodule.server.ServerEvent;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChanneledFolder {
    @Getter
    private static final File dataFolder = new File(DiscordModule.getInstance().getDataFolder(), "synced-channels" + File.separator);

    @Getter
    private String identifier;
    @Getter @Setter
    private File selfFolder;
    @Getter @Setter
    private File routesFolder;
    @Getter @Setter
    private File eventRoutesFolder;
    @Getter @Setter
    private File jsonFolder;

    public ChanneledFolder(String identifier) {
        setIdentifier(identifier);

        setSelfFolder(new File(getDataFolder(), getIdentifier() + File.separator));
        setRoutesFolder(new File(getSelfFolder(), "routes" + File.separator));
        setEventRoutesFolder(new File(getSelfFolder(), "events" + File.separator));
        setJsonFolder(new File(getSelfFolder(), "json" + File.separator));

        ensureBasics();
    }

    public void setIdentifier(String identifier) {
        if (getIdentifier() == null) {
            this.identifier = identifier;
            return;
        }
        if (getSelfFolder() == null) {
            this.identifier = identifier;
            return;
        }
        if (getSelfFolder().exists() && ! getSelfFolder().getName().equals(identifier)) {
            this.identifier = identifier;
            getSelfFolder().renameTo(new File(getDataFolder(), identifier + File.separator));
        }
    }

    public void ensureBasics() {
        if (getSelfFolder().mkdirs()) new ChanneledFolderCreateEvent(this).fire();
        getRoutesFolder().mkdirs();
        getEventRoutesFolder().mkdirs();
        getJsonFolder().mkdirs();
    }

    public Route route(String uuid) {
        return new Route(uuid, this);
    }

    public Route route(EndPoint input, EndPoint output) {
        return new Route(input, output, this);
    }

    public ServerEventRoute<?> eventRoute(String uuid) {
        return new ServerEventRoute<>(uuid, this);
    }

    public <T extends ServerEvent<?>> ServerEventRoute<T> eventRoute(EndPoint input, EndPoint output, T event) {
        return new ServerEventRoute<>(input, this, event);
    }

    @Getter @Setter
    private ConcurrentSkipListMap<String, Route> loadedRoutes = new ConcurrentSkipListMap<>();

    public boolean loadRoute(Route route) {
        if (routeExists(route)) {
//            DiscordModule.getInstance().logInfo("Not loading route '" + route.getUuid() + "' as it already is loaded.");
            return false;
        }
        getLoadedRoutes().put(route.getUuid(), route);
//        DiscordModule.getInstance().logInfo("Loaded Route '" + route.getUuid() + "'!");
        return true;
    }

    public void unloadRoute(String uuid) {
        getLoadedRoutes().remove(uuid);
    }

    public ConcurrentSkipListSet<Route> getAssociatedRoutes(EndPointType type, String identifier) {
        ConcurrentSkipListSet<Route> r = new ConcurrentSkipListSet<>();

        getLoadedRoutes().forEach((s, route) -> {
            if (route.getInput().getType().equals(type) && route.getInput().getIdentifier().equals(identifier)) {
                r.add(route);
                return;
            }
            if (route.getOutput().getType().equals(type) && route.getOutput().getIdentifier().equals(identifier)) {
                r.add(route);
            }
        });

        return r;
    }

    public ConcurrentSkipListSet<Route> getBackAndForthRoute(EndPointType type, String identifier, String channelId) {
        ConcurrentSkipListSet<Route> r = new ConcurrentSkipListSet<>();

        getAssociatedRoutes(type, identifier).forEach((route) -> {
            if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(channelId)) {
                r.add(route);
                return;
            }
            if (route.getOutput().getType().equals(EndPointType.DISCORD_TEXT) && route.getOutput().getIdentifier().equals(channelId)) {
                r.add(route);
            }
        });

        return r;
    }

    public void killRoutes() {
        getLoadedRoutes().forEach((s, route) -> route.saveAll());
        setLoadedRoutes(new ConcurrentSkipListMap<>());
    }

    public void loadAllRoutes() {
        killRoutes();

        File[] files = getRoutesFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.isFile()) return;
            if (! file.getName().endsWith(".yml")) return;

            String uuid = file.getName().substring(0, file.getName().lastIndexOf("."));
            if (! loadRoute(route(uuid))) {
                /*DiscordModule.getInstance().logWarning("Could not load a route with a UUID of '" + uuid + "'.");*/
            }
        }
    }

    public boolean routeExists(Route route) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedRoutes().forEach((s, r) -> {
            if (r.getUuid().equals(route.getUuid())) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public boolean routeExists(EndPoint p1, EndPoint p2) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedRoutes().forEach((s, r) -> {
            if (r.getInput().equals(p1) && r.getOutput().equals(p2)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    @Getter @Setter
    private ConcurrentSkipListMap<String, ServerEventRoute<?>> loadedEventRoutes = new ConcurrentSkipListMap<>();

    public boolean loadEventRoute(ServerEventRoute<?> eventRoute) {
        if (eventRouteExists(eventRoute)) {
//            DiscordModule.getInstance().logInfo("Not loading route '" + eventRoute.getUuid() + "' as it already is loaded.");
            return false;
        }
        getLoadedEventRoutes().put(eventRoute.getUuid(), eventRoute);
//        DiscordModule.getInstance().logInfo("Loaded ServerEventRoute '" + eventRoute.getUuid() + "'!");
        return true;
    }

    public void unloadEventRoute(String uuid) {
        getLoadedEventRoutes().remove(uuid);
    }

    public ConcurrentSkipListSet<ServerEventRoute<?>> getAssociatedEventRoutes(EndPointType type, String identifier) {
        ConcurrentSkipListSet<ServerEventRoute<?>> r = new ConcurrentSkipListSet<>();

        getLoadedEventRoutes().forEach((s, eventRoute) -> {
            if (eventRoute.getInput().getType().equals(type) && eventRoute.getInput().getIdentifier().equals(identifier)) {
                r.add(eventRoute);
                return;
            }
            if (eventRoute.getOutput().getType().equals(type) && eventRoute.getOutput().getIdentifier().equals(identifier)) {
                r.add(eventRoute);
            }
        });

        return r;
    }

    public ConcurrentSkipListSet<ServerEventRoute<?>> getBackAndForthEventRoute(EndPointType type, String identifier, String channelId) {
        ConcurrentSkipListSet<ServerEventRoute<?>> r = new ConcurrentSkipListSet<>();

        getAssociatedEventRoutes(type, identifier).forEach((eventRoute) -> {
            if (eventRoute.getInput().getType().equals(EndPointType.DISCORD_TEXT) && eventRoute.getInput().getIdentifier().equals(channelId)) {
                r.add(eventRoute);
                return;
            }
            if (eventRoute.getOutput().getType().equals(EndPointType.DISCORD_TEXT) && eventRoute.getOutput().getIdentifier().equals(channelId)) {
                r.add(eventRoute);
            }
        });

        return r;
    }

    public void killEventRoutes() {
        getLoadedEventRoutes().forEach((s, eventRoute) -> eventRoute.saveAll());
        setLoadedEventRoutes(new ConcurrentSkipListMap<>());
    }

    public void loadAllEventRoutes() {
        killEventRoutes();

        File[] files = getEventRoutesFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.isFile()) return;
            if (! file.getName().endsWith(".yml")) return;

            String uuid = file.getName().substring(0, file.getName().lastIndexOf("."));
            if (! loadEventRoute(eventRoute(uuid))) {
                /*DiscordModule.getInstance().logWarning("Could not load a eventRoute with a UUID of '" + uuid + "'.");*/
            }
        }
    }

    public boolean eventRouteExists(ServerEventRoute<?> eventRoute) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedEventRoutes().forEach((s, r) -> {
            if (r.equals(eventRoute)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public boolean eventRouteExists(EndPoint p1, EndPoint p2) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedEventRoutes().forEach((s, r) -> {
            if (r.getInput().equals(p1) && r.getOutput().equals(p2)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }
}
