package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import io.github.kosyakmakc.socialBridge.ITransaction;

public record DatabaseTransaction(DatabaseContext context) implements ITransaction {
    @Override
    public DatabaseContext getDatabaseContext() {
        return context;
    }
}
