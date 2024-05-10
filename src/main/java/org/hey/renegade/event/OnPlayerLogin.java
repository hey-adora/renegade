package org.hey.renegade.event;

import org.bukkit.BanList;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.profile.PlayerProfile;
import org.hey.renegade.Renegade;
import org.hey.renegade.WhiteList;
import org.hey.renegade.WhiteListPending;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class OnPlayerLogin implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerLogin(PlayerLoginEvent event) {
        Server server = getServer();
        org.bukkit.entity.Player player = event.getPlayer();
        String name = player.getName();
        InetAddress inet = event.getAddress();
        String ip = inet.getHostAddress();
        WhiteList.Player renegete_player = new WhiteList.Player(name, ip);
        try {
            String reason = "Ask Hey.";
            if (name.contains(":") || name.contains("\n")){
                reason = "Invalid name.";
                server.broadcast("[RENEGADE]: Banned for invalid name: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            }
            else if (!WhiteList.exists(renegete_player)) {
                WhiteListPending.PendingPlayer pending_player = WhiteListPending.add_pending_player(renegete_player);
                if (pending_player != null) {
                    reason = "Verify with /verify " + pending_player.code + "\nIn our discord server #gaming channel.\n(Expires in 10 minutes.)";
                    server.broadcast("[RENEGADE]: Banned not whitelisted ip: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
                } else {
                    server.broadcast("[RENEGADE]: Failed to create pending player for: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
                }
            }
            else {
                boolean unbanned = false;

                if (server.getIPBans().contains(ip)) {
                    server.unbanIP(inet);
                    unbanned = true;
                }

                BanList<PlayerProfile> banList = server.getBanList(BanList.Type.PROFILE);
                PlayerProfile player_profile = player.getPlayerProfile();
                //player_profile.setTextures();
                //player_profile.getTextures();
                if (banList.isBanned(player_profile)) {
                    banList.pardon(player_profile);
                    unbanned = true;
                }

                if (unbanned) {
                    server.broadcast("[RENEGADE]: Unbanned: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                }

                return;
            }

            server.banIP(inet);
            event.setKickMessage(reason);
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
            player.kickPlayer("Error");
            server.banIP(inet);
        }
    }
}
