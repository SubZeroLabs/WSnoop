package io.github.subzerolabs.wsnoop.common.relationships;

import io.github.subzerolabs.wsnoop.api.relationship.Relationship;
import io.github.subzerolabs.wsnoop.api.relationship.TransactionBundle;
import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record BasicRelationship(
        UUID u1, List<TransactionPair> u1Transactions,
        UUID u2, List<TransactionPair> u2Transactions
) implements Relationship {
    private List<TransactionBundle> bundle(UUID uuid, List<TransactionPair> transactions) {
        return transactions.stream().map(transaction -> new TransactionBundle(uuid, transaction)).collect(Collectors.toList());
    }

    @Override
    public UUID getU1() {
        return this.u1;
    }

    @Override
    public UUID getU2() {
        return this.u2;
    }

    @Override
    public List<TransactionPair> getU1TransactionPairs() {
        return this.u1Transactions;
    }

    @Override
    public List<TransactionPair> getU2TransactionPairs() {
        return this.u2Transactions;
    }

    @Override
    public List<TransactionBundle> getTransactionBundles() {
        List<TransactionBundle> bundle = bundle(u1, u1Transactions);
        bundle.addAll(bundle(u2, u2Transactions));
        bundle.sort(Comparator.comparingLong(item -> item.transactionPair().transaction().date()));
        return bundle;
    }

    @Override
    public Stream<TransactionBundle> stream() {
        return getTransactionBundles().stream();
    }

    @Override
    public void forEach(BiConsumer<UUID, TransactionPair> transactionBundleHandler) {
        getTransactionBundles().forEach(bundle -> transactionBundleHandler.accept(bundle.source(), bundle.transactionPair()));
    }

    @Override
    public <U> Stream<U> map(BiFunction<UUID, TransactionPair, U> transactionBundleMapper) {
        return stream().map(bundle -> transactionBundleMapper.apply(bundle.source(), bundle.transactionPair()));
    }
}
