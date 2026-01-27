package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DatabaseContext;
import io.github.kosyakmakc.socialBridge.Utils.AsyncEvent;

public interface ITransaction {
    DatabaseContext getDatabaseContext();
    /**
     * Event container, which handle commit\rollback transactions
     * <br>
     * true - commit, false - rollback
     * @return Event container
     */
    AsyncEvent<Boolean> getCloseEvent();
}
