package io.github.subzerolabs.wsnoop.api.relationship;

import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;

import java.util.UUID;

public record TransactionBundle(UUID source, TransactionPair transactionPair) {
}
