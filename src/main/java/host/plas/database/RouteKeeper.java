package host.plas.database;

import host.plas.StreamlineDiscord;
import host.plas.discord.data.channeling.EndPoint;
import host.plas.discord.data.channeling.Route;
import host.plas.discord.data.channeling.RouteLoader;
import singularity.database.modules.DBKeeper;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RouteKeeper extends DBKeeper<Route> {
    public RouteKeeper() {
        super("route-keeper", Route::new);
    }

    @Override
    public void ensureMysqlTables() {
        String s1 = "CREATE TABLE IF NOT EXISTS `%table_prefix%discord_routes` (" +
                "`Uuid` VARCHAR(36) NOT NULL," +
                "`InputUuid` VARCHAR(36) NOT NULL," +
                "`OutputUuid` VARCHAR(36) NOT NULL," +
                "PRIMARY KEY (`Uuid`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());

        getDatabase().execute(s1, stmt -> {});
    }

    @Override
    public void ensureSqliteTables() {
        String s1 = "CREATE TABLE IF NOT EXISTS `%table_prefix%discord_routes` (" +
                "`Uuid` VARCHAR(36) NOT NULL," +
                "`InputUuid` VARCHAR(36) NOT NULL," +
                "`OutputUuid` VARCHAR(36) NOT NULL," +
                "PRIMARY KEY (`Uuid`)" +
                ");";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());

        getDatabase().execute(s1, stmt -> {});
    }

    @Override
    public void saveMysql(Route route) {
        ensureTables();

        String s1 = "INSERT INTO `%table_prefix%discord_routes` (" +
                "`Uuid`, `InputUuid`, `OutputUuid`" +
                ") VALUES (" +
                "?, ?, ?" +
                ") ON DUPLICATE KEY UPDATE " +
                "`InputUuid` = ?, " +
                "`OutputUuid` = ?;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", route.getIdentifier());
        s1 = s1.replace("%input_uuid%", route.getInput().getIdentifier());
        s1 = s1.replace("%output_uuid%", route.getOutput().getIdentifier());
        
        getDatabase().execute(s1, stmt -> {
            try {
                stmt.setString(1, route.getIdentifier());
                stmt.setString(2, route.getInput().getIdentifier());
                stmt.setString(3, route.getOutput().getIdentifier());
                stmt.setString(4, route.getInput().getIdentifier());
                stmt.setString(5, route.getOutput().getIdentifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        route.getInput().save();
        route.getOutput().save();
    }

    @Override
    public void saveSqlite(Route route) {
        ensureTables();

        String s1 = "INSERT OR REPLACE INTO `%table_prefix%discord_routes` (" +
                "`Uuid`, `InputUuid`, `OutputUuid`" +
                ") VALUES (" +
                "?, ?, ?" +
                ");";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", route.getIdentifier());
        s1 = s1.replace("%input_uuid%", route.getInput().getIdentifier());
        s1 = s1.replace("%output_uuid%", route.getOutput().getIdentifier());
        
        getDatabase().execute(s1, stmt -> {
            try {
                stmt.setString(1, route.getIdentifier());
                stmt.setString(2, route.getInput().getIdentifier());
                stmt.setString(3, route.getOutput().getIdentifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        route.getInput().save();
        route.getOutput().save();
    }

    @Override
    public Optional<Route> loadMysql(String s) {
        ensureTables();

        String s1 = "SELECT * FROM `%table_prefix%discord_routes` WHERE `Uuid` = ?;";
        
        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", s);

        AtomicReference<Optional<Route>> optionalRoute = new AtomicReference<>(Optional.empty());
        getDatabase().executeQuery(s1, stmt -> {
            try {
                stmt.setString(1, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, resultSet -> {
            try {
                if (resultSet.next()) {
                    String uuid = resultSet.getString("Uuid");
                    String inputUuid = resultSet.getString("InputUuid");
                    String outputUuid = resultSet.getString("OutputUuid");

                    Optional<EndPoint> input = StreamlineDiscord.getEndPointKeeper().load(inputUuid).join();
                    if (input.isEmpty()) {
                        optionalRoute.set(Optional.empty());
                        return;
                    }
                    
                    Optional<EndPoint> output = StreamlineDiscord.getEndPointKeeper().load(outputUuid).join();
                    if (output.isEmpty()) {
                        optionalRoute.set(Optional.empty());
                        return;
                    }
                    
                    EndPoint in = input.get();
                    EndPoint out = output.get();

                    Route route = new Route(uuid);
                    route.setInput(in);
                    route.setOutput(out);
                    
                    optionalRoute.set(Optional.of(route));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        return optionalRoute.get();
    }

    @Override
    public Optional<Route> loadSqlite(String s) {
        ensureTables();

        String s1 = "SELECT * FROM `%table_prefix%discord_routes` WHERE `Uuid` = ?;";
        
        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", s);

        AtomicReference<Optional<Route>> optionalRoute = new AtomicReference<>(Optional.empty());
        getDatabase().executeQuery(s1, stmt -> {
            try {
                stmt.setString(1, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, resultSet -> {
            try {
                if (resultSet.next()) {
                    String uuid = resultSet.getString("Uuid");
                    String inputUuid = resultSet.getString("InputUuid");
                    String outputUuid = resultSet.getString("OutputUuid");

                    Optional<EndPoint> input = StreamlineDiscord.getEndPointKeeper().load(inputUuid).join();
                    if (input.isEmpty()) {
                        optionalRoute.set(Optional.empty());
                        return;
                    }
                    
                    Optional<EndPoint> output = StreamlineDiscord.getEndPointKeeper().load(outputUuid).join();
                    if (output.isEmpty()) {
                        optionalRoute.set(Optional.empty());
                        return;
                    }
                    
                    EndPoint in = input.get();
                    EndPoint out = output.get();

                    Route route = new Route(uuid);
                    route.setInput(in);
                    route.setOutput(out);
                    
                    optionalRoute.set(Optional.of(route));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        return optionalRoute.get();
    }

    @Override
    public boolean existsMysql(String s) {
        ensureTables();

        String s1 = "SELECT * FROM `%table_prefix%discord_routes` WHERE `Uuid` = ?;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", s);

        AtomicReference<Boolean> atomicReference = new AtomicReference<>(false);
        getDatabase().executeQuery(s1, stmt -> {
            try {
                stmt.setString(1, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, resultSet -> {
            try {
                atomicReference.set(resultSet.next());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return atomicReference.get();
    }

    @Override
    public boolean existsSqlite(String s) {
        ensureTables();

        String s1 = "SELECT * FROM `%table_prefix%discord_routes` WHERE `Uuid` = ?;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", s);

        AtomicReference<Boolean> atomicReference = new AtomicReference<>(false);
        getDatabase().executeQuery(s1, stmt -> {
            try {
                stmt.setString(1, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, resultSet -> {
            try {
                atomicReference.set(resultSet.next());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return atomicReference.get();
    }

    public void drop(Route route) {
        ensureTables();

        route.getInput().drop();
        route.getOutput().drop();

        String s1 = "DELETE FROM `%table_prefix%discord_routes` WHERE `Uuid` = ?;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());
        s1 = s1.replace("%uuid%", route.getIdentifier());

        getDatabase().execute(s1, stmt -> {
            try {
                stmt.setString(1, route.getIdentifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadAllRoutes() {
        ensureTables();

        String s1 = "SELECT * FROM `%table_prefix%discord_routes`;";

        s1 = s1.replace("%table_prefix%", getDatabase().getConnectorSet().getTablePrefix());

        getDatabase().executeQuery(s1, stmt -> {}, resultSet -> {
            try {
                while (resultSet.next()) {
                    String uuid = resultSet.getString("Uuid");
                    String inputUuid = resultSet.getString("InputUuid");
                    String outputUuid = resultSet.getString("OutputUuid");

                    Optional<EndPoint> input = StreamlineDiscord.getEndPointKeeper().load(inputUuid).join();
                    if (input.isEmpty()) continue;

                    Optional<EndPoint> output = StreamlineDiscord.getEndPointKeeper().load(outputUuid).join();
                    if (output.isEmpty()) continue;

                    EndPoint in = input.get();
                    EndPoint out = output.get();

                    Route route = new Route(uuid);
                    route.setInput(in);
                    route.setOutput(out);

                    RouteLoader.registerRoute(route);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
