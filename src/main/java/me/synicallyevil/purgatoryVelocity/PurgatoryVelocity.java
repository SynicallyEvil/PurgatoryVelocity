package me.synicallyevil.purgatoryVelocity;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.synicallyevil.purgatoryVelocity.commands.Ban;
import me.synicallyevil.purgatoryVelocity.commands.CheckBan;
import me.synicallyevil.purgatoryVelocity.commands.Unban;
import me.synicallyevil.purgatoryVelocity.mysql.MySQL;
import me.synicallyevil.purgatoryVelocity.mysql.MySQLUser;
import me.synicallyevil.purgatoryVelocity.utils.DateUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.synicallyevil.purgatoryVelocity.utils.Utils.colorize;


@Plugin(id = "purgatoryvelocity", name = "PurgatoryVelocity", version = "1.0-SNAPSHOT", authors = {"SynicallyEvil"})
public class PurgatoryVelocity {

    private final ProxyServer server;
    private final Path dataDirectory;
    private final Map<UUID, MySQLUser> bannedPlayers = new HashMap<>();
    private Toml config = new Toml();
    private final MySQL mysql;

    @Inject
    private Logger logger;

    @Inject
    public PurgatoryVelocity(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

        try {
            Path configPath = dataDirectory.resolve("config.toml");

            if (!Files.exists(configPath)) {
                Files.createDirectories(dataDirectory);
                Files.copy(getClass().getResourceAsStream("/config.toml"), configPath);
            }

            this.config = new Toml().read(configPath.toFile());
        } catch (Exception e) {
            logger.warn("Failed to load configuration: " + e.getMessage());
        }

        this.mysql = new MySQL(config.getString("mysql.ip"),
                config.getString("mysql.username"),
                config.getString("mysql.password"),
                config.getString("mysql.database"));

        for(MySQLUser user : this.mysql.getAllUsers())
            bannedPlayers.put(UUID.fromString(user.getUUID()), user);
    }

    @Inject
    public void registerCommands(CommandManager commandManager) {
        commandManager.register("purge", new Ban(this, server, config), "ban");
        commandManager.register("unpurge", new Unban(this, server, config), "unban");
        commandManager.register("checkpurge", new CheckBan(this, config), "checkban");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        startTimer();
    }

    private void startTimer(){
        server.getScheduler().buildTask(this, () -> {
            server.getServer(config.getString("purgatory.server")).ifPresent(purgatoryServer -> {
                purgatoryServer.getPlayersConnected().forEach(player -> {
                    //MySQLUser user = getMySQL().getMYSQLUser(player.getUniqueId().toString());

                    if(!bannedPlayers.containsKey(player.getUniqueId()))
                        return;

                    MySQLUser user = bannedPlayers.get(player.getUniqueId());
                    if(!user.isBanned())
                        return;

                    if(user.getTime() <= System.currentTimeMillis()){
                        getMySQL().unbanPlayer(player.getUniqueId().toString());
                        bannedPlayers.remove(player.getUniqueId());
                        //player.sendMessage(colorize("You are now unbanned"));

                        Optional<RegisteredServer> hub = server.getServer(config.getString("purgatory.hub"));
                        hub.ifPresent((pur) -> {
                            player.createConnectionRequest(pur).connect();

                        });
                    }
                });
            });
        }).repeat(2L, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onLoginEvent(LoginEvent event) {
        Player player = event.getPlayer();
        //MySQLUser user = getMySQL().getMYSQLUser(player.getUniqueId().toString());

        if(!bannedPlayers.containsKey(player.getUniqueId()))
            return;

        MySQLUser user = bannedPlayers.get(player.getUniqueId());
        if(user.isBanned()){
            if(user.getTime() <= System.currentTimeMillis()){
                getMySQL().unbanPlayer(player.getUniqueId().toString());
                bannedPlayers.remove(player.getUniqueId());
                return;
            }

            //logger.info("User is banned");
            Optional<RegisteredServer> purgatory = server.getServer(config.getString("purgatory.server"));
            purgatory.ifPresentOrElse((server) -> {
                player.createConnectionRequest(server).connect()
                        .thenAccept((result) -> {
                            if(result.isSuccessful()) {
                                String message = config.getString("messages.ban_message_when_online")
                                        .replaceAll("%reason%", user.getReason())
                                        .replaceAll("%time%", DateUtils.formatDateDiff(user.getTime()))
                                        .replaceAll("\n", System.lineSeparator());

                                player.sendMessage(colorize(message));
                                logger.info(player.getUsername() + " is banned, so they are being transferred to purgatory.");
                            }
                        });
            }, () -> {
                player.disconnect(colorize("&cYou are still banned for: " + user.getReason()));
                logger.info(player.getUsername() + " is banned, so they are being kicked from the server.");
            });
        }
    }

    @Subscribe
    public void onServerPreConnectEvent(ServerPreConnectEvent event){
        Player player = event.getPlayer();
        //MySQLUser user = getMySQL().getMYSQLUser(player.getUniqueId().toString());

        if(!bannedPlayers.containsKey(player.getUniqueId()))
            return;

        MySQLUser user = bannedPlayers.get(player.getUniqueId());
        if(user.isBanned()){
            Optional<ServerConnection> currentServer = player.getCurrentServer();
            String currentServerName = currentServer.map(server -> server.getServerInfo().getName()).orElse("None");

            Optional<RegisteredServer> targetServer = event.getResult().getServer();
            String targetServerName = targetServer.map(server -> server.getServerInfo().getName()).orElse("Unknown");

            // if player is already on purgatory, disable all.
            if(currentServerName.equalsIgnoreCase(config.getString("purgatory.server")))
                event.setResult(ServerPreConnectEvent.ServerResult.denied());

            // if player is NOT on purgatory, disable all EXCEPT purgatory.
            if(!targetServerName.equalsIgnoreCase(config.getString("purgatory.server")))
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    public MySQL getMySQL(){
        return mysql;
    }

    public Map<UUID, MySQLUser> getBannedPlayers() {
        return bannedPlayers;
    }

    public void addBannedPlayer(String id, String uuid, String reason, String staff, Long time){
        bannedPlayers.put(UUID.fromString(uuid), new MySQLUser(true, uuid, id, reason, staff, time));
    }

}
