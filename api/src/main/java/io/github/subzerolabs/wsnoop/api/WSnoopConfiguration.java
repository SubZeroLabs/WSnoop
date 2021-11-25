package io.github.subzerolabs.wsnoop.api;

import java.nio.file.Path;

public interface WSnoopConfiguration {
    int getMaxStoredRelationshipTransactions();

    long relationshipTransactionAgeLimitCheckPeriod();

    long getRelationshipTransactionAgeLimit();

    Path getMessageDirectory();
}
