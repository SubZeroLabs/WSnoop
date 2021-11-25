package io.github.subzerolabs.wsnoop.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.subzerolabs.wsnoop.velocity.MessageSink;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class MessageCommand implements SimpleCommand {
    private final ProxyServer server;
    private final MessageSink messageSink;

    public MessageCommand(ProxyServer server, MessageSink messageSink) {
        this.server = server;
        this.messageSink = messageSink;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Component.text("Please include a player to message and a message.", NamedTextColor.RED));
            return;
        }

        Player source;
        Player destination;
        if (invocation.source() instanceof Player player) {
            source = player;
            destination = this.server.getPlayer(invocation.arguments()[0]).orElse(null);
            if (destination == null) {
                player.sendMessage(Component.text("The player specified could not be found."));
                return;
            }
        } else {
            invocation.source().sendMessage(Component.text("Non player sources cannot invoke this command.", NamedTextColor.RED));
            return;
        }

        try {
            this.messageSink.message(source, destination, Arrays.stream(invocation.arguments()).skip(1).collect(Collectors.joining(" ")));
        } catch (IOException e) {
            source.sendMessage(Component.text("Something went wrong!", NamedTextColor.RED));
        }
    }
}
