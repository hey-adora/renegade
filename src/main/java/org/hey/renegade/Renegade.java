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
import org.hey.renegade.command.CommandWhiteListAdd;
import org.hey.renegade.command.CommandWhiteListRemove;
import org.hey.renegade.event.OnPlayerLogin;
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
import java.util.concurrent.ThreadLocalRandom;


public final class Renegade extends JavaPlugin implements Listener {
    public static String path = "plugins/renegade/";
    public Logger logger;

    public void create_dirs() {
        File file = new File(Renegade.path);
        boolean result = file.mkdirs();
    }



    @Override
    public void onEnable() {
        logger = getLogger();
        logger.info("Loading Renegade!");
        //logger.info("Loading renegade");
        create_dirs();
        WhiteList.init();
        WhiteListPending.init();
        WhiteListHTTP.init();

        getServer().getPluginManager().registerEvents(new OnPlayerLogin(), this);
        getCommand("allow").setExecutor(new CommandWhiteListAdd(logger));
        getCommand("deny").setExecutor(new CommandWhiteListRemove(logger));




    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Disabling Renegade!");
    }

}
