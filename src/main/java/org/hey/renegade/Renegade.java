package org.hey.renegade;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



public final class Renegade extends JavaPlugin implements Listener {
    public static String version = "v1";
    public static String path = "plugins/renegade/";
    public static String name = "players.txt";

    String get_path() {
        return Renegade.path + Renegade.name;
    }

    public void write(Renegade.Player player)  {
        try {
            List<Renegade.Player> allowed_players = read();
            for (int i = 0; i < allowed_players.size(); i++) {
                Renegade.Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name, player.name) && !Objects.equals(player.ip, existing_player.ip)) {
                    allowed_players.set(i, player);
                }
            }
            StringBuilder output = new StringBuilder(Renegade.version + "\n");
            output.append(player.name).append(":").append(player.ip).append("\n");

            FileWriter file_writer = new FileWriter(get_path(), false);
            BufferedWriter writer = new BufferedWriter(file_writer);
            writer.write(output.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Renegade.Player> read() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<Renegade.Player> players = new ArrayList<Renegade.Player>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                System.out.println("here: "+player[0]+player[1]);
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
            if (Objects.equals(player.name, new_player.name) && Objects.equals(player.ip, new_player.ip)) {
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
        getCommand("add_ip").setExecutor(new Renegade.CommandAdd());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Server server = getServer();
        ConsoleCommandSender console = server.getConsoleSender();
        org.bukkit.entity.Player player = event.getPlayer();
        String name = player.getName();
        InetAddress inet = event.getAddress();
        String ip = inet.getHostAddress();
        Renegade.Player renegete_player = new Renegade.Player(name, ip);
        try {
            String reason = "Ask Hey.";
            if (name.contains(":") || name.contains("\n")){
                reason = "Invalid name.";
                console.sendMessage("Banned for invalid name: "+ip);
            }
            else if (!exists(read(), renegete_player)) {
                reason = "Ask Hey.";
                console.sendMessage("Banned not whitelisted ip: "+ip);
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
                    console.sendMessage("Unbanned: "+ip);
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                }

                return;
            }

            server.banIP(inet);
            event.setKickMessage(reason);
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            System.out.println(name + ":" + ip);
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
                ConsoleCommandSender console = server.getConsoleSender();

                server.unbanIP(inet);
                write(player);

                console.sendMessage("Added: "+name+":"+ip);
                return true;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return false;
            }
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
