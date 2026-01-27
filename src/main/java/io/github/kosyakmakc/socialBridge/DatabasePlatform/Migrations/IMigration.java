package io.github.kosyakmakc.socialBridge.DatabasePlatform.Migrations;

import io.github.kosyakmakc.socialBridge.ITransactionConsumer;

public interface IMigration extends ITransactionConsumer<Void> {
    String getName();
    int getVersion();
}
