package host.plas.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class RouteManager {
    @Getter @Setter
    public static ConcurrentSkipListSet<Route> loadedRoutes = new ConcurrentSkipListSet<>();

    public static Optional<Route> registerRoute(Route route) {
        Optional<Route> optional = getRoute(route.getIdentifier());
        if (optional.isPresent()) return optional;

        loadedRoutes.add(route);

        return Optional.of(route);
    }

    public static void unregisterRoute(Route route) {
        loadedRoutes.removeIf(route1 -> route1.getIdentifier().equals(route.getIdentifier()));
    }

    public static Optional<Route> getRoute(String identifier) {
        return loadedRoutes.stream().filter(route -> route.getIdentifier().equals(identifier)).findFirst();
    }

    public static CompletableFuture<Optional<Route>> getOrLoadRouteAsync(String identifier) {
        Optional<Route> optional = getRoute(identifier);
        if (optional.isPresent()) return CompletableFuture.completedFuture(optional);

        return DiscordModule.getRouteKeeper().load(identifier);
    }

    public static void loadAllRoutes() {
        CompletableFuture.runAsync(() -> {
            DiscordModule.getRouteKeeper().loadAllRoutes();

            DiscordModule.getInstance().logInfo("Loaded &a" + getLoadedRoutes().size() + " &froutes.");
        });
    }

    public static ConcurrentSkipListSet<Route> getBackAndForthRoute(EndPointType typeIn, String identifierIn, EndPointType typeOut, String identifierOut) {
        ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>();
        getLoadedRoutes().forEach(route -> {
            if (route.getInput().getType().equals(typeIn) && route.getInput().getIdentifier().equals(identifierIn) && route.getOutput().getType().equals(typeOut) && route.getOutput().getIdentifier().equals(identifierOut)) {
                routes.add(route);
            }
        });

        return routes;
    }
}
