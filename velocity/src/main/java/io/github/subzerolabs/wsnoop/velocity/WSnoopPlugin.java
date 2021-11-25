package io.github.subzerolabs.wsnoop.velocity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.subzerolabs.wsnoop.api.WSnoopConfiguration;
import io.github.subzerolabs.wsnoop.common.JacksonConfiguration;
import io.github.subzerolabs.wsnoop.velocity.commands.HistoryCommand;
import io.github.subzerolabs.wsnoop.velocity.commands.MessageCommand;
import io.github.subzerolabs.wsnoop.velocity.commands.ReplyCommand;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

@Plugin(
        id = "wsnoop",
        name = "WSnoop",
        url = "https://github.com/SubZeroLabs/WSnoop/",
        authors = {"FiXed"},
        version = "0.0.0",
        description = "Snooper for messages, keeps a detailed log of messages between users."
)
public class WSnoopPlugin {
    @Inject
    private ProxyServer proxyServer;
    @Inject
    @DataDirectory
    private Path dataDirectory;
    @Inject
    private Logger logger;

    private WSnoopConfiguration loadConfiguration() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Path configPath = this.dataDirectory.resolve("config.json");
        if (Files.exists(configPath)) {
            try (InputStream reader = Files.newInputStream(configPath)) {
                return mapper.readValue(reader, JacksonConfiguration.class);
            }
        } else {
            try (OutputStream writer = Files.newOutputStream(configPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                JacksonConfiguration configuration = new JacksonConfiguration(
                        50,
                        60,
                        Duration.ofDays(10).toMillis(),
                        this.dataDirectory.resolve("relationships")
                );
                mapper.writerWithDefaultPrettyPrinter().writeValue(writer, configuration);
                return configuration;
            }
        }
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent unused) {
        WSnoopConfiguration configuration;
        try {
            if (!Files.exists(this.dataDirectory)) {
                Files.createDirectories(this.dataDirectory);
            }
            configuration = loadConfiguration();
        } catch (IOException ex) {
            this.logger.error("Failed to create data directory.", ex);
            return;
        }

        MessageSink messageSink = new MessageSink(this.logger, configuration);

        CommandManager meta = this.proxyServer.getCommandManager();
        meta.register(
                meta.metaBuilder("message_1").build(),
                new MessageCommand(this.proxyServer, messageSink)
        );
        meta.register(
                meta.metaBuilder("reply_1").build(),
                new ReplyCommand(this.proxyServer, messageSink)
        );
        meta.register(
                meta.metaBuilder("history_1").build(),
                new HistoryCommand(this.logger, messageSink)
        );
    }
}
