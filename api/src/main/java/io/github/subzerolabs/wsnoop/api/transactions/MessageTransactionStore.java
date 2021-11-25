package io.github.subzerolabs.wsnoop.api.transactions;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * The message transaction store manages {@link MessageTransaction}s in an abstract way.
 */
public interface MessageTransactionStore {
    /**
     * Store a message transaction between a source and destination.
     *
     * @param source      The {@link UUID} defining the source of the message.
     * @param destination The {@link UUID} defining the destination of the message.
     * @param transaction The {@link MessageTransaction} which holds the information of the message.
     * @throws IOException when an IO exception occurs when trying to store the {@code transaction} to DAO.
     */
    void storeTransaction(UUID source, UUID destination, MessageTransaction transaction) throws IOException;

    /**
     * Resolve all the message transactions from a specific source.
     *
     * @param source The {@link UUID} of the user whose messages we're looking at.
     * @return The list of {@link TransactionPair}'s which link to all the messages the {@code source} sent.
     * @throws IOException when an IO exception occurs when trying to read the {@code transaction}s from the DAO.
     */
    List<TransactionPair> resolveTransactions(UUID source) throws IOException;

    /**
     * Retrieve all the messages the {@code source} sent to the {@code destination}.
     *
     * @param source      The {@link UUID} of the user whose messages we're looking at.
     * @param destination The {@link UUID} of the destination for the messages.
     * @return The list of {@link TransactionPair}'s which link to all the messages the {@code source} sent to the {@code destination}.
     * @throws IOException when an IO exception occurs when trying to read the {@code transaction}s from the DAO.
     */
    List<TransactionPair> resolveTransactions(UUID source, UUID destination) throws IOException;
}
