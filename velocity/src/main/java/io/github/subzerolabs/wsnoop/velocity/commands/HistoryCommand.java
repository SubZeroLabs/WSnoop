package io.github.subzerolabs.wsnoop.velocity.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.subzerolabs.wsnoop.velocity.MessageSink;
import kong.unirest.Unirest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

// using https://playerdb.co/api/player/minecraft/

public class HistoryCommand implements SimpleCommand {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
    private final MessageSink messageSink;
    private final ObjectMapper objectMapper;

    public HistoryCommand(Logger logger, MessageSink messageSink) {
        this.messageSink = messageSink;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDefaultLeniency(true);
        Unirest.config().setObjectMapper(new kong.unirest.ObjectMapper() {
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return HistoryCommand.this.objectMapper.readValue(value, valueType);
                } catch (JsonProcessingException e) {
                    logger.error("Error parsing json.", e);
                    return null;
                }
            }

            @Override
            public String writeValue(Object value) {
                try {
                    return HistoryCommand.this.objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    logger.error("Error serializing json.", e);
                    return "{}";
                }
            }
        });
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Component.text("Please include 2 players to lookup.", NamedTextColor.RED));
            return;
        }
        try {
            String name1 = invocation.arguments()[0];
            String name2 = invocation.arguments()[1];
            UUID player1 = playerToUUID(name1);
            UUID player2 = playerToUUID(name2);
            messageSink.getRelationship(player1, player2).forEach((uuid, transaction) -> {
                String source = resolveName(player1, name1, player2, name2, uuid);
                String destination = resolveName(player1, name1, player2, name2, transaction.destination());
                invocation.source().sendMessage(Component.join(Component.empty(),
                        Component.text("(", NamedTextColor.DARK_GRAY),
                        Component.text(SDF.format(new Date(transaction.transaction().date())), NamedTextColor.GRAY),
                        Component.text(")", NamedTextColor.DARK_GRAY),
                        Component.text("[", NamedTextColor.AQUA),
                        Component.text(source, NamedTextColor.DARK_AQUA),
                        Component.text(" -> ", NamedTextColor.AQUA),
                        Component.text(destination, NamedTextColor.DARK_AQUA),
                        Component.text("] ", NamedTextColor.AQUA),
                        Component.text(transaction.transaction().message())
                ));
            });
        } catch (IOException ex) {
            invocation.source().sendMessage(Component.text("Could not find one of the specified users.", NamedTextColor.RED));
        }
    }

    private String resolveName(UUID u1, String s1, UUID u2, String s2, UUID test) {
        if (test.equals(u1)) {
            return s1;
        } else if (test.equals(u2)) {
            return s2;
        } else {
            return "null";
        }
    }

    private UUID playerToUUID(String player) throws IOException {
        PlayerDBResponse response = Unirest
                .get("https://playerdb.co/api/player/minecraft/%s".formatted(player))
                .asObject(PlayerDBResponse.class)
                .getBody();
        if (response == null || !response.success()) {
            return null;
        }
        return response.data.map(item -> item.player.id).orElse(null);
    }

    private static record DataStep2(
            String username,
            UUID id,
            @JsonProperty("raw_id") String rawId,
            String avatar
    ) {
    }

    private static record DataStep1(DataStep2 player) {
    }

    private static record PlayerDBResponse(
            String code,
            Optional<DataStep1> data,
            String message,
            boolean success
    ) {
    }
}
