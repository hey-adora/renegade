package org.hey.renegade.command;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hey.renegade.WhiteList;

import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class CommandWhiteListRemove implements CommandExecutor {

    private final Logger logger;
    public CommandWhiteListRemove(Logger logger) {
        this.logger = logger;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        String name = args[0];
        Player player = (Player) sender;

        Server server = getServer();



        try {
            WhiteList.remove_allowed_player(name);
            player.sendMessage(ChatColor.YELLOW + "Successfully Blacklisted " + name + ".");

            org.bukkit.entity.Player online_players = server.getPlayer(name);
            try {
                if (online_players != null) {
                    online_players.kickPlayer("Removed.");
                }
            } catch (Exception e){
                player.sendMessage(ChatColor.RED + "Failed to blacklist " + name + ". They may have already been blacklisted.");
                logger.info("An Exception has Occurred: " + e);
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while blacklisting " + name + ".");
            e.printStackTrace();
        }

        return true;
    }
}
