package io.github.subzerolabs.wsnoop.api.transactions;

/**
 * A message transaction containing the date of the message and the literal message.
 */
public record MessageTransaction(long date, String message) {
}
