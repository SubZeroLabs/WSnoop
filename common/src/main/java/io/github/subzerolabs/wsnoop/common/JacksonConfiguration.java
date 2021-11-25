package io.github.subzerolabs.wsnoop.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.subzerolabs.wsnoop.api.WSnoopConfiguration;

import java.nio.file.Path;

public record JacksonConfiguration(
        @JsonProperty("max-transactions") int maxTransactions,
        @JsonProperty("age-check-period") long ageCheckPeriod,
        @JsonProperty("transaction-age-limit") long transactionAgeLimit,
        @JsonProperty("message-directory") Path messageDirectory
) implements WSnoopConfiguration {
    @Override
    public int getMaxStoredRelationshipTransactions() {
        return this.maxTransactions;
    }

    @Override
    public long relationshipTransactionAgeLimitCheckPeriod() {
        return this.ageCheckPeriod;
    }

    @Override
    public long getRelationshipTransactionAgeLimit() {
        return this.transactionAgeLimit;
    }

    @Override
    public Path getMessageDirectory() {
        return this.messageDirectory;
    }
}
