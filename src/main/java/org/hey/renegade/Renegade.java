package org.hey.renegade;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public final class Renegade extends JavaPlugin implements Listener {
    public void create_dirs() {
        File file = new File(Renegade.path);
        boolean result = file.mkdirs();
//        File file2 = new File(Renegade.backup_path);
//        boolean result2 = file2.mkdirs();
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

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";

            InputStream is = t.getRequestBody();

            byte[] buffer = new byte[4];
            int r = is.read(buffer);
            if (r == 4) {
                ByteBuffer wrapped = ByteBuffer.wrap(buffer);
                int num = wrapped.getInt();

                System.out.println(num);
            }


            //byte[] bytes = is.readAllBytes();


            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    @Override
    public void onEnable() {
        //Logger logger = getLogger();
        System.out.println("Loading renegade");

        create_dirs();
        check_version();



        getServer().getPluginManager().registerEvents(this, this);
        getCommand("allow").setExecutor(new Renegade.CommandAdd());
        getCommand("deny").setExecutor(new Renegade.CommandRemove());

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8069), 0);
            server.createContext("/verify", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //getCommand("backup-now").setExecutor(new Renegade.CommandBackupNow());
        //getCommand("backup-interval").setExecutor(new Renegade.CommandBackupInterval());

//        FileConfiguration config = getConfig();
//        //long ms = default_interval;
//        Object interval_obj = config.get("interval_ms");
//        if (interval_obj == null) {
//            config.set("interval_ms", default_interval);
//            saveConfig();
//        } else {
//            try {
//                interval_ms.set(Long.parseLong(interval_obj.toString(), 10));
//            } catch (NumberFormatException e) {
//                logger.log(Level.CONFIG, "Invalid interval_ms: "+interval_obj+". Example: 3600000");
//            }
//        }
//        System.out.println(interval_obj);
//
//        //interval_ms.set(ms);
//        //AtomicLong finalMs = new AtomicLong(ms);
//        backup_interval_handle = Bukkit.getScheduler().runTaskTimer(this, () -> {
//            //test++;
//            Server server = getServer();
//            long now = System.currentTimeMillis();
//
////            if (((time + interval_ms.get()) < now) && saved_world && saved_world_nether && saved_world_end) {
////                server.broadcast("[RENEGADE]: "+ test, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
////                time  = now;
////                test++;
////                saved_world = false;
////                saved_world_nether = false;
////                saved_world_end = false;
////            }
//            if ((time + interval_ms.get()) < now) {
//                server.broadcast("[RENEGADE]: "+ test, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//                time  = now;
//                test++;
//                saved_world = false;
//                saved_world_nether = false;
//                saved_world_end = false;
//                ready_to_backup = true;
//                List<World> worlds = server.getWorlds();
//                for (World world : worlds) {
//                    world.save();
//                }
//            }
//
////            List<World> worlds = server.getWorlds();
////            for (World world : worlds) {
////                world.save();
////            }
//          // ZipUtil.pack(new File("world"), new File("world.zip"));
//
//        }, 0L, 20L);


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
        //server.getWorld("").save();

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
        } catch (IOException e) {
            System.out.println(e.getMessage());
            player.kickPlayer("Error");
            server.banIP(inet);
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onWorldSave(WorldSaveEvent event) {
//        System.out.println("EVENT NAME: "+event.getEventName());
//        if (ready_to_backup && (!saved_world || !saved_world_nether || !saved_world_end)) {
//            Logger logger = getLogger();
//
//            String world_name = event.getWorld().getName();
//            switch (world_name) {
//                case "world": {
//                    logger.log(Level.INFO, "World saved.");
//                    saved_world = true;
//                    ZipUtil.pack(new File("world"), new File(backup_path+"world_"+time+".zip"));
//                    break;
//                }
//                case "world_nether": {
//                    logger.log(Level.INFO, "Nether saved.");
//                    saved_world_nether = true;
//                    ZipUtil.pack(new File("world_nether"), new File(backup_path+"world_nether_"+time+".zip"));
//                    break;
//                }
//                case "world_the_end": {
//                    logger.log(Level.INFO, "TheEnd saved.");
//                    saved_world_end = true;
//                    ZipUtil.pack(new File("world_the_end"), new File(backup_path+"world_the_end_"+time+".zip"));
//                    break;
//                }
//                default: {
//                    logger.log(Level.WARNING, "Unknown world saved: " + world_name);
//                }
//            }
//
////            if (world_name == "world") {
////                saved_world = true;
////            }
//
//            //server.broadcast("[RENEGADE]1: " +  event.getEventName(), server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//            //server.broadcast("[RENEGADE]2: " +  event.getWorld().getName(), server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//        }
//        if (saved_world && saved_world_nether && saved_world_end) {
//            Server server = getServer();
//            server.broadcast("[RENEGADE] Backup made.", server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//        }
//
//    }

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

            //server.broadcast("[RENEGADE]: Removed: "+name, server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            return true;
        }
    }
    // ZipUtil.pack(new File("world"), new File("world.zip"));
//    public class CommandBackupNow implements CommandExecutor {
//        @Override
//        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//            Server server = getServer();
//            time  = System.currentTimeMillis();
//            test++;
//            saved_world = false;
//            saved_world_nether = false;
//            saved_world_end = false;
//            ready_to_backup = true;
//            List<World> worlds = server.getWorlds();
//            for (World world : worlds) {
//                world.save();
//            }
//
////            test++;
////
////
////            List<World> worlds = server.getWorlds();
////            for (World world : worlds) {
////                world.save();
////            }
////            ZipUtil.pack(new File("world"), new File("world.zip"));
//            server.broadcast("[RENEGADE]: Backup started...", server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//            return true;
//        }
//    }
//
//    public class CommandBackupInterval implements CommandExecutor {
//        @Override
//        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//            Server server = getServer();
//            try {
//                interval_ms.set(Long.parseLong(args[0], 10));
//            } catch (NumberFormatException e) {
//                return false;
//            }
//            server.broadcast("[RENEGADE]: "+interval_ms.get(), server.BROADCAST_CHANNEL_ADMINISTRATIVE);
//            return true;
//        }
//    }


}
