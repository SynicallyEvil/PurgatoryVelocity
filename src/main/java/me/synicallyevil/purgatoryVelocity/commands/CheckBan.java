package me.synicallyevil.purgatoryVelocity.commands;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.synicallyevil.purgatoryVelocity.PurgatoryVelocity;
import me.synicallyevil.purgatoryVelocity.mysql.MySQLUser;
import me.synicallyevil.purgatoryVelocity.request.ProfileChecker;
import me.synicallyevil.purgatoryVelocity.utils.DateUtils;

import static me.synicallyevil.purgatoryVelocity.utils.Utils.colorize;

public class CheckBan implements SimpleCommand {

    private final PurgatoryVelocity purge;
    private final Toml config;

    public CheckBan(PurgatoryVelocity purge, Toml config) {
        this.purge = purge;
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
            source.sendMessage(colorize(config.getString("messages.usage_checkban")));
            return;
        }

        try {
            ProfileChecker profile = new ProfileChecker(args[0]);
            String uuid = profile.getUUID().toString();
            MySQLUser user = purge.getMySQL().getMYSQLUser(uuid);

            if(!user.isBanned()){
                source.sendMessage(colorize(config.getString("messages.player_not_banned")));
                return;
            }

            String checkban = config.getString("messages.checkban")
                    .replaceAll("\n", System.lineSeparator())
                    .replaceAll("%id%", user.getId())
                    .replaceAll("%player%", profile.getCurrentName())
                    .replaceAll("%reason%", user.getReason())
                    .replaceAll("%staff%", user.getStaff())
                    .replaceAll("%blacklisted%", String.valueOf(user.isBanned()))
                    .replaceAll("%time%", DateUtils.formatDateDiff(user.getTime()));

            source.sendMessage(colorize(checkban));

        } catch (IllegalArgumentException e) {
            source.sendMessage(colorize(config.getString("messages.invalid_uuid")));
        }
    }
}