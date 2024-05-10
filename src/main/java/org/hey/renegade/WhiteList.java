package org.hey.renegade;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class WhiteList {
    public static String version = "v1";
    public static String name = "players.txt";


    static public class Player {
        public String name;
        public String ip;

        public Player(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public Player(WhiteListPending.PendingPlayer player) {
            this.name = player.name;
            this.ip = player.ip;
        }
    }


    public static String get_path() {
        return Renegade.path + WhiteList.name;
    }

    public static void init() {
        File file = new File(get_path());
        if (!file.exists()) {
            try {
                FileWriter file_writer = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(file_writer);
                writer.append(WhiteList.version+"\n");
                writer.close();
            } catch (Exception e) {
                Logger.getLogger("Renegade").info(e.getMessage());
            }
        }
    }

    public static void add_allowed_player(WhiteListPending.PendingPlayer player) {
        WhiteList.add_allowed_player(new Player(player));
    }

    public static void add_allowed_player(Player player)  {
        try {
            boolean not_updated = true;
            List<Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), player.name.toLowerCase())) {
                    allowed_players.set(i, player);
                    not_updated = false;
                    break;
                }
            }
            if (not_updated) {
                allowed_players.add(player);
            }

            WhiteList.save_allowed_players(allowed_players);
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
        }
    }

    public static void remove_allowed_player(String name)  {
        try {
            List<Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), name.toLowerCase())) {
                    allowed_players.remove(i);
                    break;
                }
            }
            WhiteList.save_allowed_players(allowed_players);
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
        }
    }

    public static List<Player> get_allowed_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<Player> players = new ArrayList<Player>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                players.add(new Player(player[0], player[1]));
            }
            return players;
        } catch (Exception e) {
            Logger.getLogger("Renegade").info(e.getMessage());
            throw e;
        }
    }

    public static void save_allowed_players(List<Player> allowed_players) throws IOException {
        try {
            StringBuilder output = new StringBuilder(WhiteList.version + "\n");
            for (Player allowed_player : allowed_players) {
                output.append(allowed_player.name).append(":").append(allowed_player.ip).append("\n");
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

    public static boolean exists(Player new_player) throws IOException {
        List<Player> players = WhiteList.get_allowed_players();
        for (Player player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }
}
