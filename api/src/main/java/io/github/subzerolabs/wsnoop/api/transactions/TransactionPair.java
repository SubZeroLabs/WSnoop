package io.github.subzerolabs.wsnoop.api.transactions;

import java.util.UUID;

/**
 * A pair of {@link UUID} destination and the {@link MessageTransaction}.
 */
public record TransactionPair(UUID destination, MessageTransaction transaction) {
}
