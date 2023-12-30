package org.hey.renegade;

import org.bukkit.BanList;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public final class Renegade extends JavaPlugin implements Listener {
    public static String version = "v1";
    public static String path = "plugins/renegade/";
    public static String name = "players.txt";

    String get_path() {
        return Renegade.path + Renegade.name;
    }

    public void add_allowed_player(Renegade.Player player)  {
        try {
            boolean updated = false;
            List<Renegade.Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                Renegade.Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), player.name.toLowerCase())) {
                    allowed_players.set(i, player);
                    updated = true;
                    break;
                }
            }
            StringBuilder output = new StringBuilder(Renegade.version + "\n");
            for (Renegade.Player allowed_player : allowed_players) {
                output.append(allowed_player.name).append(":").append(allowed_player.ip).append("\n");
            }
            if (!updated) {
                output.append(player.name).append(":").append(player.ip).append("\n");
            }

            FileWriter file_writer = new FileWriter(get_path(), false);
            BufferedWriter writer = new BufferedWriter(file_writer);
            writer.write(output.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void remove_allowed_player(String name)  {
        try {
            List<Renegade.Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                Renegade.Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), name.toLowerCase())) {
                    allowed_players.remove(i);
                    break;
                }
            }
            StringBuilder output = new StringBuilder(Renegade.version + "\n");
            for (Renegade.Player allowed_player : allowed_players) {
                output.append(allowed_player.name).append(":").append(allowed_player.ip).append("\n");
            }

            FileWriter file_writer = new FileWriter(get_path(), false);
            BufferedWriter writer = new BufferedWriter(file_writer);
            writer.write(output.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Renegade.Player> get_allowed_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<Renegade.Player> players = new ArrayList<Renegade.Player>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                //System.out.println("here: "+player[0]+player[1]);
                players.add(new Renegade.Player(player[0], player[1]));
            }
            return players;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public boolean exists(List<Renegade.Player> players, Renegade.Player new_player) {
        for (Player player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }

    public void create_dirs() {
        File file = new File(Renegade.path);
        boolean result = file.mkdirs();
    }

    public void check_version() {
        File file = new File(get_path());
        if (!file.exists()) {
            try {
                FileWriter file_writer = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(file_writer);
                writer.append(Renegade.version+"\n");
                writer.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }



    @Override
    public void onEnable() {
        System.out.println("Loading renegade");

        create_dirs();
        check_version();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("allow").setExecutor(new Renegade.CommandAdd());
        getCommand("deny").setExecutor(new Renegade.CommandRemove());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

//    @EventHandler
//    public void onServerCommand(ServerCommandEvent event) {
//        System.out.println("whaaaaaaaa: "+event.getCommand().split(" ")[0]);
//    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Server server = getServer();
        //ConsoleCommandSender console = server.getConsoleSender();
        org.bukkit.entity.Player player = event.getPlayer();
        String name = player.getName();
        InetAddress inet = event.getAddress();
        String ip = inet.getHostAddress();
        Renegade.Player renegete_player = new Renegade.Player(name, ip);
        try {
            String reason = "Ask Hey.";
            if (name.contains(":") || name.contains("\n")){
                reason = "Invalid name.";
                server.broadcast("[RENEGADE]: Banned for invalid name: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            }
            else if (!exists(get_allowed_players(), renegete_player)) {
                reason = "Ask Hey.";
                server.broadcast("[RENEGADE]: Banned not whitelisted ip: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            }
            else {
                boolean unbanned = false;

                if (server.getIPBans().contains(ip)) {
                    server.unbanIP(inet);
                    unbanned = true;
                }

                BanList<PlayerProfile> banList = server.getBanList(BanList.Type.PROFILE);
                PlayerProfile player_profile = player.getPlayerProfile();
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
        } catch (IOException e) {
            System.out.println(e.getMessage());
            player.kickPlayer("Error");
            server.banIP(inet);
        }
    }

    public class CommandAdd implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            try {
                if (args.length != 2) return false;
                String name = args[0];
                String ip = args[1];
                InetAddress inet = InetAddress.getByName(ip);
                Renegade.Player player = new Renegade.Player(name, ip);

                Server server = getServer();
                //ConsoleCommandSender console = server.getConsoleSender();

                server.unbanIP(inet);
                add_allowed_player(player);

                server.broadcast("[RENEGADE]: Added: "+name+":"+ip, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
                return true;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }

    }

    public class CommandRemove implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length != 1) return false;
            String name = args[0];

            Server server = getServer();
            //ConsoleCommandSender console = server.getConsoleSender();

            remove_allowed_player(name);
            org.bukkit.entity.Player online_players = server.getPlayer(name);
            if (online_players != null) {
                online_players.kickPlayer("Removed.");

            }

            server.broadcast("[RENEGADE]: Removed: "+name, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            return true;
        }
    }

    public class Player {
        public String name;
        public String ip;

        public Player(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
    }
}
