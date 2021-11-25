package io.github.subzerolabs.wsnoop.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.subzerolabs.wsnoop.api.WSnoopConfiguration;
import io.github.subzerolabs.wsnoop.api.relationship.Relationship;
import io.github.subzerolabs.wsnoop.api.relationship.RelationshipMingle;
import io.github.subzerolabs.wsnoop.api.transactions.MessageTransaction;
import io.github.subzerolabs.wsnoop.api.transactions.MessageTransactionStore;
import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;
import io.github.subzerolabs.wsnoop.common.relationships.BasicRelationshipMingle;
import io.github.subzerolabs.wsnoop.common.transactions.JsonMessageTransactionStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSink {
    private final WSnoopConfiguration configuration;
    private final MessageTransactionStore transactionStore;
    private final RelationshipMingle mingle;
    private final Map<UUID, UUID> replyCache;

    public MessageSink(Logger logger, WSnoopConfiguration configuration) {
        this.configuration = configuration;
        this.transactionStore = new JsonMessageTransactionStore(logger, configuration);
        this.mingle = new BasicRelationshipMingle();
        this.replyCache = new ConcurrentHashMap<>();
    }

    public void message(Player source, Player destination, String message) throws IOException {
        this.transactionStore.storeTransaction(
                source.getUniqueId(),
                destination.getUniqueId(),
                new MessageTransaction(new Date().getTime(), message)
        );
        this.replyCache.put(destination.getUniqueId(), source.getUniqueId());
        destination.sendMessage(Component.join(
                Component.empty(),
                Component.text("[", NamedTextColor.AQUA),
                Component.text(source.getUsername(), NamedTextColor.DARK_AQUA),
                Component.text(" -> ", NamedTextColor.AQUA),
                Component.text("Me", NamedTextColor.DARK_AQUA),
                Component.text("] ", NamedTextColor.AQUA),
                Component.text(message)
        ));
        source.sendMessage(Component.join(
                Component.empty(),
                Component.text("[", NamedTextColor.AQUA),
                Component.text("Me", NamedTextColor.DARK_AQUA),
                Component.text(" -> ", NamedTextColor.AQUA),
                Component.text(destination.getUsername(), NamedTextColor.DARK_AQUA),
                Component.text("] ", NamedTextColor.AQUA),
                Component.text(message)
        ));
    }

    public @Nullable Player getReplyDestination(ProxyServer server, UUID source) {
        UUID cacheReply = replyCache.get(source);
        if (cacheReply == null) {
            return null;
        }
        return server.getPlayer(cacheReply).orElse(null);
    }

    public Relationship getRelationship(UUID user1, UUID user2) throws IOException {
        List<TransactionPair> u1Transactions = this.transactionStore.resolveTransactions(user1, user2);
        List<TransactionPair> u2Transactions = this.transactionStore.resolveTransactions(user2, user1);
        return this.mingle.mingle(user1, u1Transactions, user2, u2Transactions);
    }
}
