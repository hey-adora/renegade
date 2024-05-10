package org.hey.renegade.command;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hey.renegade.WhiteList;
import org.hey.renegade.WhiteListHTTP;

import java.net.InetAddress;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class CommandWhiteListAdd implements CommandExecutor {
    private final Logger logger;
    public CommandWhiteListAdd(Logger logger) {
        this.logger = logger;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length != 2) return false;
            String name = args[0];
            String ip = args[1];
            InetAddress inet = InetAddress.getByName(ip);
            WhiteList.Player player = new WhiteList.Player(name, ip);

            Server server = getServer();
            //ConsoleCommandSender console = server.getConsoleSender();

            server.unbanIP(inet);
            WhiteList.add_allowed_player(player);
            WhiteListHTTP.remove_discord_player(player.name);

            server.broadcast(ChatColor.YELLOW + "[RENEGADE]: Added: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            return true;
        } catch (Exception e) {
            logger.info(e.getMessage());
            return false;
        }
    }

}