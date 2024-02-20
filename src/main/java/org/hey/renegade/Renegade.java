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


public final class Renegade extends JavaPlugin implements Listener {
    public static String path = "plugins/renegade/";

    public void create_dirs() {
        File file = new File(Renegade.path);
        boolean result = file.mkdirs();
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
        System.out.println("Loading renegade");
        create_dirs();
        WhiteList.init();


        getServer().getPluginManager().registerEvents(new OnPlayerLogin(), this);
        getCommand("allow").setExecutor(new CommandWhiteListAdd());
        getCommand("deny").setExecutor(new CommandWhiteListRemove());

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8069), 0);
            server.createContext("/verify", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
