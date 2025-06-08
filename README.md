# 🧨 PurgatoryVelocity

**PurgatoryVelocity** is a Velocity proxy plugin that replaces traditional bans by redirecting punished players to a configurable "purgatory" server for a specified time.

---

## 🚀 Features

* 🧹 **Purge Instead of Ban** – Redirect players to a "purgatory" server
* ⏳ **Timed Punishments** – Flexible time formats (1m, 1h, 1d, 1w, 1mo, 1y)
* 💾 **MySQL Integration** – Required for storing purge data
* 🧪 **Punishment Checker** – View player punishment details
* ⚙️ **Command Permissions** – All functionality gated by permissions
* 🔁 **Auto Redirection** – Players online will be instantly moved to purgatory

---

## 📂 Configuration (`config.toml`)

```toml
[messages]
no_permission = "&cYou don't have permission to use this command."
usage_ban = "&eUsage: /ban <player> <time (1d, 1m, 1w, 1mo, 1y)> <reason>"
usage_unban = "&eUsage: /unban <player>"
usage_checkban = "&eUsage: /checkban <player>"
checkban = "&4&m-----&4[ &cPurgatory &4]&m-----\n&cUser: &f%player% &c(ID: &f%id%&c)\n&cBanned: &f%blacklisted%\n&cReason: &f%reason%\n&cStaff: &f%staff%\n&cTime: &f%time%\n&4&m---------------------------"
player_not_found = "&cPlayer not found."
player_banned = "&aPlayer %player% has been banned for: %reason%"
player_disconnected = "&cYou have been banned: %reason%"
player_unbanned = "&aPlayer has been unbanned."
player_not_banned = "&cPlayer is not banned."
invalid_uuid = "&cInvalid UUID format."
ban_message_when_online = "&cYou are still banned for: %reason%\n&cTime remaining: %time%"

[permissions]
manager = "purgatory.manager"

[mysql]
enabled = false
ip = "ip:3306"
username = "username"
password = "password"
database = "database"

[purgatory]
server = "purgatory"
hub = "hub"
```

---

## 🔧 Commands

> All commands require the `purgatory.manager` permission.

* `/purge <player> <time>` – Send a player to purgatory for a specified time
* `/unpurge <player>` – Remove a player from purgatory
* `/checkpurge <player>` – View purge information for a player

---

## 📦 Requirements

* Velocity Proxy
* MySQL database (required)

---

## 👤 Author

Made by [SynicallyEvil](https://github.com/SynicallyEvil)  
Leave a ⭐ on GitHub!
