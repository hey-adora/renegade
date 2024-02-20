package org.hey.renegade;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhiteList {
    public static String version = "v1";
    public static String path = "plugins/renegade/";
    public static String name = "players.txt";

    public class Player {
        public String name;
        public String ip;

        public Player(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
    }

    public WhiteList() {

    }


    String get_path() {
        return WhiteList.path + WhiteList.name;
    }

    public void add_allowed_player(WhiteList.Player player)  {
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

    public void remove_allowed_player(String name)  {
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

    public List<WhiteList.Player> get_allowed_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<WhiteList.Player> players = new ArrayList<WhiteList.Player>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                //System.out.println("here: "+player[0]+player[1]);
                players.add(new WhiteList.Player(player[0], player[1]));
            }
            return players;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public boolean exists(List<WhiteList.Player> players, WhiteList.Player new_player) {
        for (WhiteList.Player player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }
}
