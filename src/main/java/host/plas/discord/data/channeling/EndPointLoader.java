package host.plas.discord.data.channeling;

import host.plas.StreamlineDiscord;
import singularity.database.modules.DBKeeper;
import singularity.loading.Loader;

public class EndPointLoader extends Loader<EndPoint> {
    public static EndPointLoader getInstance() {
        return StreamlineDiscord.getEndPointLoader();
    }

    @Override
    public DBKeeper<EndPoint> getKeeper() {
        return StreamlineDiscord.getEndPointKeeper();
    }

    @Override
    public EndPoint getConsole() {
        return null;
    }

    @Override
    public void fireLoadEvents(EndPoint endPoint) {
        // No events to fire for loading EndPoints.
    }

    @Override
    public EndPoint instantiate(String s) {
        return new EndPoint(s);
    }

    @Override
    public void fireCreateEvents(EndPoint endPoint) {
        // No events to fire for creating EndPoints.
    }
}
