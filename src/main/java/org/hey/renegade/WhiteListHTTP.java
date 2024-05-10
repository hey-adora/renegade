package org.hey.renegade;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
//import org.jooq.types.ULong;

public class WhiteListHTTP {

    public static String version = "v1";
    public static String name = "discord_players.txt";

    static public class DiscordPlayer {
        public String name;
        public long discord_id;

        public DiscordPlayer(String name, long discord_id) {
            this.name = name;
            this.discord_id = discord_id;
        }
    }


    public static String get_path() {
        return Renegade.path + WhiteListHTTP.name;
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();


            byte[] buffer = new byte[12];
            int r = is.read(buffer, 0, 12);

            if (r == 12) {
                byte[] user_id_buffer = Arrays.copyOfRange(buffer, 0, 8);
                byte[] code_buffer = Arrays.copyOfRange(buffer, 8, 12);

                ByteBuffer wrapped = ByteBuffer.wrap(user_id_buffer);
                long user_id = wrapped.getLong();

                ByteBuffer wrapped2 = ByteBuffer.wrap(code_buffer);
                int code = wrapped2.getInt();



                WhiteListPending.PendingPlayer pending_player = WhiteListPending.verify(code);
                if (pending_player != null) {
                    WhiteListHTTP.DiscordPlayer discord_player = new WhiteListHTTP.DiscordPlayer(pending_player.name, user_id);
                    //Logger.getLogger("Renegade").info("[RENEGADE]: dddddddddddddddddd");
                    boolean is_same_discord_player = WhiteListHTTP.verify(discord_player);
                    if (is_same_discord_player) {
                        //Logger.getLogger("Renegade").info("[RENEGADE]: bbbbbbbbbbbbbbbbbbb");
                        Logger.getLogger("Renegade").info("[RENEGADE]: Whitelisted "+user_id+": "+pending_player.name+":"+pending_player.ip+" with code: "+code);
                        WhiteList.add_allowed_player(pending_player);
                        //Logger.getLogger("Renegade").info("[RENEGADE]: ccccccccccccccccc");
                        t.sendResponseHeaders(200, 0);
                    } else {
                        Logger.getLogger("Renegade").info("[RENEGADE]: Failed to verify, invalid discord user: "+user_id+" for "+pending_player.name+": with code: "+code);
                        t.sendResponseHeaders(401, 0);
                    }
                } else {
                    Logger.getLogger("Renegade").info("[RENEGADE]: Failed to verify "+user_id+": with code: "+code);
                    t.sendResponseHeaders(404, 0);
                }

            } else {
                Logger.getLogger("Renegade").info("[RENEGADE]: invalid verification. ");
                t.sendResponseHeaders(402, 0);
            }

            OutputStream os = t.getResponseBody();
            os.close();
        }
    }

    static public void init() {
        try {

            File file = new File(get_path());
            if (!file.exists()) {
                FileWriter file_writer = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(file_writer);
                writer.append(WhiteListHTTP.version+"\n");
                writer.close();
            }

            HttpServer server = null;
            server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8069), 0);
            server.createContext("/verify", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (Exception e) {
            Logger.getLogger("Renegade").info("[RENEGADE]: ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(WhiteListHTTP.DiscordPlayer discord_player)  {
        boolean verified = true;
        try {
            boolean insert_new = true;
            List<WhiteListHTTP.DiscordPlayer> discord_players = get_discord_players();
            for (int i = 0; i < discord_players.size(); i++) {
                WhiteListHTTP.DiscordPlayer existing_discord_player = discord_players.get(i);
                if (Objects.equals(existing_discord_player.name.toLowerCase(), discord_player.name.toLowerCase())) {
                    if (existing_discord_player.discord_id != discord_player.discord_id) {
                        verified = false;
                    }
                    insert_new = false;
                    break;
                }
            }
            if (insert_new) {
                discord_players.add(discord_player);
                WhiteListHTTP.save_discord_players(discord_players);
            }
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
        }
        return verified;
    }

    public static void remove_discord_player(String name)  {
        try {
            List<WhiteListHTTP.DiscordPlayer> discord_players = get_discord_players();
            for (int i = 0; i < discord_players.size(); i++) {
                WhiteListHTTP.DiscordPlayer existing_discord_player = discord_players.get(i);
                if (Objects.equals(existing_discord_player.name.toLowerCase(), name.toLowerCase())) {
                    discord_players.remove(i);
                    break;
                }
            }
            WhiteListHTTP.save_discord_players(discord_players);
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
        }
    }

    public static List<WhiteListHTTP.DiscordPlayer> get_discord_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<WhiteListHTTP.DiscordPlayer> players = new ArrayList<WhiteListHTTP.DiscordPlayer>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                players.add(new WhiteListHTTP.DiscordPlayer(player[0], Long.parseLong(player[1])));
            }
            return players;
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
            throw e;
        }
    }

    public static void save_discord_players(List<WhiteListHTTP.DiscordPlayer> discord_players) throws IOException {
        try {
            StringBuilder output = new StringBuilder(WhiteList.version + "\n");
            for (WhiteListHTTP.DiscordPlayer discord_player : discord_players) {
                output.append(discord_player.name).append(":").append(discord_player.discord_id).append("\n");
            }

            FileWriter file_writer = new FileWriter(get_path(), false);
            BufferedWriter writer = new BufferedWriter(file_writer);
            writer.write(output.toString());
            writer.close();
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
            throw e;
        }
    }
}
