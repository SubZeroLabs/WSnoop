package io.github.subzerolabs.wsnoop.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.subzerolabs.wsnoop.velocity.MessageSink;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ReplyCommand implements SimpleCommand {
    private final ProxyServer server;
    private final MessageSink messageSink;

    public ReplyCommand(ProxyServer server, MessageSink messageSink) {
        this.server = server;
        this.messageSink = messageSink;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(Component.text("Please include a message.", NamedTextColor.RED));
            return;
        }

        Player source;
        Player destination;
        if (invocation.source() instanceof Player player) {
            source = player;
            destination = this.messageSink.getReplyDestination(this.server, player.getUniqueId());
        } else {
            invocation.source().sendMessage(Component.text("Non player sources cannot invoke this command.", NamedTextColor.RED));
            return;
        }

        if (destination == null) {
            source.sendMessage(Component.text("You have nobody to reply to.", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", invocation.arguments());
        try {
            this.messageSink.message(source, destination, message);
        } catch (IOException e) {
            source.sendMessage(Component.text("Something went wrong!", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }
}
