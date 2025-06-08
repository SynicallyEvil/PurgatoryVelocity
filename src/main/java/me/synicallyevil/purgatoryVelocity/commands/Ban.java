package me.synicallyevil.purgatoryVelocity.commands;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.synicallyevil.purgatoryVelocity.PurgatoryVelocity;
import me.synicallyevil.purgatoryVelocity.mysql.MySQLUser;
import me.synicallyevil.purgatoryVelocity.request.ProfileChecker;
import me.synicallyevil.purgatoryVelocity.utils.DateUtils;

import java.util.Optional;

import static me.synicallyevil.purgatoryVelocity.utils.Utils.colorize;
import static me.synicallyevil.purgatoryVelocity.utils.Utils.generateRandomCode;

public class Ban implements SimpleCommand {

    private final PurgatoryVelocity purge;
    private final ProxyServer server;
    private final Toml config;

    public Ban(PurgatoryVelocity purge, ProxyServer server, Toml config) {
        this.purge = purge;
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        Player playera = null;

        try{
            playera = (Player)source;
        }catch(Exception ignored){}

        if (!source.hasPermission(config.getString("permissions.manager"))) {
            source.sendMessage(colorize(config.getString("messages.no_permission")));
            return;
        }

        String[] args = invocation.arguments();
        if (!(args.length > 2)) {
            source.sendMessage(colorize(config.getString("messages.usage_ban")));
            return;
        } //ban synicallyevil (1)7d (2)hacking

        long time;
        try {
            time = DateUtils.parseDateDiff(args[1], true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString();

        ProfileChecker profile = new ProfileChecker(args[0]);
        String uuid = profile.getUUID().toString();
        MySQLUser user = purge.getMySQL().getMYSQLUser(uuid);
        String id = generateRandomCode(8);

        if(!user.isBanned()){
            purge.getMySQL().banPlayer(uuid, reason, (playera == null ? "CONSOLE" : playera.getUsername()), id, time);
            purge.addBannedPlayer(id, uuid, reason, (playera == null ? "CONSOLE" : playera.getUsername()), time);

            source.sendMessage(colorize(config.getString("messages.player_banned")
                    .replace("%player%", args[0])
                    .replace("%reason%", reason)));
        }else{
            source.sendMessage(colorize(config.getString("messages.player_not_banned")));
        }

        server.getPlayer(args[0]).ifPresent((player) -> {
            Optional<RegisteredServer> purgatory = server.getServer(config.getString("purgatory.server"));

            purgatory.ifPresent((server) -> {
               player.createConnectionRequest(server).connect()
                       .thenAccept((result) -> {
                           if(result.isSuccessful()) {
                               MySQLUser useru = purge.getBannedPlayers().get(player.getUniqueId());
                               String message = config.getString("messages.ban_message_when_online")
                                       .replaceAll("%reason%", useru.getReason())
                                       .replaceAll("%time%", DateUtils.formatDateDiff(useru.getTime()))
                                       .replaceAll("\n", System.lineSeparator());

                               player.sendMessage(colorize(message));
                           }
                       });
            });
        });
    }
}