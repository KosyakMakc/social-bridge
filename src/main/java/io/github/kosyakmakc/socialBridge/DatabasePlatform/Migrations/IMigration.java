package io.github.kosyakmakc.socialBridge.DatabasePlatform.Migrations;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.IDatabaseConsumer;

public interface IMigration extends IDatabaseConsumer<Void> {
    String getName();
    int getVersion();
}
