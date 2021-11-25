package io.github.subzerolabs.wsnoop.api.relationship;

import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface Relationship {
    UUID getU1();

    UUID getU2();

    List<TransactionPair> getU1TransactionPairs();

    List<TransactionPair> getU2TransactionPairs();

    List<TransactionBundle> getTransactionBundles();

    Stream<TransactionBundle> stream();

    void forEach(BiConsumer<UUID, TransactionPair> transactionBundleHandler);

    <U> Stream<U> map(BiFunction<UUID, TransactionPair, U> transactionBundleMapper);
}
