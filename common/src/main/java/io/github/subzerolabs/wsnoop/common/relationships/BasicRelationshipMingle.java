package io.github.subzerolabs.wsnoop.common.relationships;

import io.github.subzerolabs.wsnoop.api.relationship.Relationship;
import io.github.subzerolabs.wsnoop.api.relationship.RelationshipMingle;
import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;

import java.util.List;
import java.util.UUID;

public class BasicRelationshipMingle implements RelationshipMingle {
    @Override
    public Relationship mingle(UUID u1, List<TransactionPair> u1Transactions, UUID u2, List<TransactionPair> u2Transactions) {
        return new BasicRelationship(u1, u1Transactions, u2, u2Transactions);
    }
}
