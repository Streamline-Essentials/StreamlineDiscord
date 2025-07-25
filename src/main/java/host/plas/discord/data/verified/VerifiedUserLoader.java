package host.plas.discord.data.verified;

import host.plas.StreamlineDiscord;
import host.plas.database.VerifiedUserKeeper;
import singularity.database.modules.DBKeeper;
import singularity.loading.Loader;

public class VerifiedUserLoader extends Loader<VerifiedUser> {
    public VerifiedUserLoader getInstance() {
        return StreamlineDiscord.getVerifiedUserLoader();
    }

    @Override
    public DBKeeper<VerifiedUser> getKeeper() {
        return null;
    }

    @Override
    public VerifiedUser getConsole() {
        return null;
    }

    @Override
    public void fireLoadEvents(VerifiedUser verifiedUser) {

    }

    @Override
    public VerifiedUser instantiate(String s) {
        return null;
    }

    @Override
    public void fireCreateEvents(VerifiedUser verifiedUser) {

    }

    public boolean isLoaded(VerifiedUser user) {
        return isLoaded(user.getIdentifier());
    }
}
