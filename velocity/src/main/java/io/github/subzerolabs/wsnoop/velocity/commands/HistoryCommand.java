package io.github.subzerolabs.wsnoop.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import io.github.subzerolabs.wsnoop.velocity.MessageSink;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

// using https://playerdb.co/api/player/minecraft/

@SuppressWarnings("ClassCanBeRecord")
public class HistoryCommand implements SimpleCommand {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
    private final MessageSink messageSink;

    public HistoryCommand(MessageSink messageSink) {
        this.messageSink = messageSink;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Component.text("Please include 2 players to lookup.", NamedTextColor.RED));
            return;
        }
        try {
            String _name1 = invocation.arguments()[0];
            UUID _player1;
            try {
                _player1 = UUID.fromString(_name1);
                _name1 = _name1.substring(0, 6);
            } catch (IllegalArgumentException unused) {
                _player1 = playerToUUID(_name1);
            }

            UUID _player2;
            String _name2 = invocation.arguments()[1];
            try {
                _player2 = UUID.fromString(_name2);
                _name2 = _name2.substring(0, 6);
            } catch (IllegalArgumentException unused) {
                _player2 = playerToUUID(_name2);
            }

            final String name1 = _name1;
            final String name2 = _name2;
            final UUID player1 = _player1;
            final UUID player2 = _player2;

            if (player1 == null || player2 == null) {
                invocation.source().sendMessage(Component.text("Could not resolve both users. Please try again.", NamedTextColor.RED));
                return;
            }

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
        JsonNode response = Unirest
                .get("https://playerdb.co/api/player/minecraft/%s".formatted(player))
                .asJson().getBody();
        JSONObject object;
        if (response == null || !(object = response.getObject()).getBoolean("success")) {
            return null;
        }
        return UUID.fromString(object.getJSONObject("data").getJSONObject("player").getString("id"));
    }
}
