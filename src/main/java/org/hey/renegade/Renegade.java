package org.hey.renegade;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



public final class Renegade extends JavaPlugin implements Listener {
    public static String version = "v1";
    public static String path = "plugins/renegade/";
    public static String name = "players.txt";
    List<Renegade.Player> allowed_players = new ArrayList<Renegade.Player>();

//    public void write(String stuff) {
//        try {
//            FileOutputStream fileOut = new FileOutputStream(Renegade.path);
//            GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
//            BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzOut);
//            out.write(stuff.getBytes());
//            out.close();
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//    }
    String get_path() {
        return Renegade.path + Renegade.name;
    }

    public void write(Renegade.Player player)  {
        try {
            FileWriter file_writer = new FileWriter(get_path());
            BufferedWriter writer = new BufferedWriter(file_writer);
            writer.append(player.name+":"+player.ip+"\n");
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        getConfig().set("players."+name, stuff);
//        saveConfig();
    }

    public void read() {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<Renegade.Player> players = new ArrayList<Renegade.Player>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                players.add(new Renegade.Player(player[0], player[1]));
            }
            this.allowed_players = players;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println("OwO");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        System.out.println("canceled dude2");
    }

    @EventHandler
    public void onHarvestBlockEvent(PlayerHarvestBlockEvent event) {
        event.setCancelled(true);
        System.out.println("canceled dude3");
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        //event.setCancelled(true);
        //System.out.println("canceled dude4");
    }

    @EventHandler
    public void onPlayerWorldEvent(PlayerChangedMainHandEvent event) {

        System.out.println("world boom");
    }

    @EventHandler
    public void onPlayerBreak(PlayerItemBreakEvent event) {

        System.out.println("breakkkkkkkkkkkkk");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
        System.out.println("dmageeeeeeeeeee");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
       // System.out.println("break block oof " + this.test);
       // this.test++;
    }

    public class Player {
        public String name;
        public String ip;

        public Player(String name, String ip) {
            this.name = name;
            this.ip = name;
        }
    }
}
