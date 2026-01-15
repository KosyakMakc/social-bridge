package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DatabaseContext;

public interface ITransaction {
    DatabaseContext getDatabaseContext();
}
