package org.hey.renegade;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void add_allowed_player(WhiteList.Player player)  {
        try {
            boolean updated = false;
            List<WhiteList.Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                WhiteList.Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), player.name.toLowerCase())) {
                    allowed_players.set(i, player);
                    updated = true;
                    break;
                }
            }
            StringBuilder output = new StringBuilder(WhiteList.version + "\n");
            for (WhiteList.Player allowed_player : allowed_players) {
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

    public static void remove_allowed_player(String name)  {
        try {
            List<WhiteList.Player> allowed_players = get_allowed_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                WhiteList.Player existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), name.toLowerCase())) {
                    allowed_players.remove(i);
                    break;
                }
            }
            StringBuilder output = new StringBuilder(WhiteList.version + "\n");
            for (WhiteList.Player allowed_player : allowed_players) {
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

    public static List<WhiteList.Player> get_allowed_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<WhiteList.Player> players = new ArrayList<WhiteList.Player>();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                players.add(new WhiteList.Player(player[0], player[1]));
            }
            return players;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public static boolean exists(WhiteList.Player new_player) throws IOException {
        List<WhiteList.Player> players = WhiteList.get_allowed_players();
        for (WhiteList.Player player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }
}
