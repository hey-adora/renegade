package org.hey.renegade.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hey.renegade.WhiteList;

import static org.bukkit.Bukkit.getServer;

public class CommandWhiteListRemove implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        String name = args[0];

        Server server = getServer();

        WhiteList.remove_allowed_player(name);
        org.bukkit.entity.Player online_players = server.getPlayer(name);
        if (online_players != null) {
            online_players.kickPlayer("Removed.");

        }

        return true;
    }
}