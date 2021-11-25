package io.github.subzerolabs.wsnoop.common.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.subzerolabs.wsnoop.api.WSnoopConfiguration;
import io.github.subzerolabs.wsnoop.api.transactions.MessageTransaction;
import io.github.subzerolabs.wsnoop.api.transactions.MessageTransactionStore;
import io.github.subzerolabs.wsnoop.api.transactions.TransactionPair;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonMessageTransactionStore implements MessageTransactionStore {
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final WSnoopConfiguration configuration;

    public JsonMessageTransactionStore(Logger logger, WSnoopConfiguration configuration) {
        this(logger, configuration, new ObjectMapper());
    }

    public JsonMessageTransactionStore(Logger logger, WSnoopConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    @Override
    public void storeTransaction(UUID source, UUID destination, MessageTransaction transaction) throws IOException {
        var path = configuration.getMessageDirectory().resolve(source.toString()).resolve(destination.toString());
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        var length = Files.list(path).count();
        if (configuration.getMaxStoredRelationshipTransactions() > 0 && length >= configuration.getMaxStoredRelationshipTransactions()) {
            Files.list(path)
                    .map(sub -> {
                        try (InputStream reader = Files.newInputStream(path)) {
                            return Map.entry(sub, this.objectMapper.readValue(reader, MessageTransaction.class));
                        } catch (IOException ex) {
                            logger.error("Failed to read json path %s".formatted(sub), ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong(e -> e.getValue().date()))
                    .skip(configuration.getMaxStoredRelationshipTransactions() - 1)
                    .forEach(entry -> {
                        try {
                            Files.delete(entry.getKey());
                        } catch (IOException ex) {
                            logger.error("Failed to delete json path %s".formatted(entry.getKey()), ex);
                        }
                    });
        }
        Path targetPath = path.resolve("%d.msg".formatted(transaction.date()));
        try (OutputStream writer = Files.newOutputStream(targetPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            objectMapper.writeValue(writer, transaction);
        }
    }

    @Override
    public List<TransactionPair> resolveTransactions(UUID source) throws IOException {
        var path = configuration.getMessageDirectory().resolve(source.toString());
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return Files.list(path).flatMap(sub -> {
            UUID uuid = UUID.fromString(sub.getFileName().toString());
            try {
                return Files.list(sub).map(subSub -> {
                    try (InputStream reader = Files.newInputStream(subSub)) {
                        return new TransactionPair(uuid, this.objectMapper.readValue(reader, MessageTransaction.class));
                    } catch (IOException ex) {
                        logger.error("Failed to read json from path %s".formatted(subSub), ex);
                        return null;
                    }
                }).filter(Objects::nonNull);
            } catch (IOException ex) {
                logger.error("Failed to list from sub path %s".formatted(sub), ex);
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<TransactionPair> resolveTransactions(UUID source, UUID destination) throws IOException {
        var path = configuration.getMessageDirectory().resolve(source.toString()).resolve(destination.toString());
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return Files.list(path).map(sub -> {
            try (InputStream reader = Files.newInputStream(sub)) {
                return new TransactionPair(destination, this.objectMapper.readValue(reader, MessageTransaction.class));
            } catch (IOException ex) {
                logger.error("Failed to read json from path %s".formatted(sub), ex);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
