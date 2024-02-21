package org.hey.renegade.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hey.renegade.WhiteList;
import org.hey.renegade.WhiteListHTTP;

import java.io.IOException;
import java.net.InetAddress;

import static org.bukkit.Bukkit.getServer;

public class CommandWhiteListAdd implements CommandExecutor {
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

            server.broadcast("[RENEGADE]: Added: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}