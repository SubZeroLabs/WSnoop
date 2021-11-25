package io.github.subzerolabs.wsnoop.api.relationship;

import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;

import java.util.List;
import java.util.UUID;

public interface RelationshipMingle {
    Relationship mingle(UUID u1, List<TransactionPair> u1Transactions, UUID u2, List<TransactionPair> u2Transactions);
}
