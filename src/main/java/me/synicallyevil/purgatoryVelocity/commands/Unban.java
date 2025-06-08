package me.synicallyevil.purgatoryVelocity.commands;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.synicallyevil.purgatoryVelocity.PurgatoryVelocity;
import me.synicallyevil.purgatoryVelocity.mysql.MySQLUser;
import me.synicallyevil.purgatoryVelocity.request.ProfileChecker;

import java.util.Optional;
import java.util.UUID;

import static me.synicallyevil.purgatoryVelocity.utils.Utils.colorize;

public class Unban implements SimpleCommand {

    private final PurgatoryVelocity purge;
    private final ProxyServer server;
    private final Toml config;

    public Unban(PurgatoryVelocity purge, ProxyServer server, Toml config) {
        this.purge = purge;
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission(config.getString("permissions.manager"))) {
            source.sendMessage(colorize(config.getString("messages.no_permission")));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 1) {
            source.sendMessage(colorize(config.getString("messages.usage_unban")));
            return;
        }

        try {
            ProfileChecker profile = new ProfileChecker(args[0]);
            String uuid = profile.getUUID().toString();
            MySQLUser user = purge.getMySQL().getMYSQLUser(uuid);

            if(user.isBanned()){
                purge.getMySQL().unbanPlayer(uuid);
                purge.getBannedPlayers().remove(UUID.fromString(uuid));
                source.sendMessage(colorize(config.getString("messages.player_unbanned")));

                server.getPlayer(args[0]).ifPresent((player) -> {
                    Optional<RegisteredServer> purgatory = server.getServer(config.getString("purgatory.hub"));
                    purgatory.ifPresent((server) -> {
                        player.createConnectionRequest(server).connect();
                    });
                });
            }else{
                source.sendMessage(colorize(config.getString("messages.player_not_banned")));
            }

        } catch (IllegalArgumentException e) {
            source.sendMessage(colorize(config.getString("messages.invalid_uuid")));
        }
    }
}