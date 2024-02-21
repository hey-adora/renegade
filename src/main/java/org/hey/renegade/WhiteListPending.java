package org.hey.renegade;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class WhiteListPending {
    public static String version = "v1";
    public static String name = "pending_players.txt";
    public static long time_limit = 10 * 60 * 1000;

    static public class PendingPlayer {
        public String name;
        public String ip;
        public int code;
        public long time;

        public PendingPlayer(WhiteList.Player player) {
            this.name = player.name;
            this.ip = player.ip;
            this.code = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
            this.time = System.currentTimeMillis();
        }

        public PendingPlayer(String name, String ip) {
            this.name = name;
            this.ip = ip;
            this.code = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
            this.time = System.currentTimeMillis();
        }

        public PendingPlayer(String name, String ip, int code, long time) {
            this.name = name;
            this.ip = ip;
            this.code = code;
            this.time = time;
        }

    }


    public static String get_path() {
        return Renegade.path + WhiteListPending.name;
    }

    public static void init() {
        File file = new File(get_path());
        if (!file.exists()) {
            try {
                FileWriter file_writer = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(file_writer);
                writer.append(WhiteListPending.version+"\n");
                writer.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


    }

    public static WhiteListPending.PendingPlayer verify(int code)  {
        try {
            WhiteListPending.PendingPlayer player = null;
            List<WhiteListPending.PendingPlayer> pending_players = get_pending_players();
            for (int i = 0; i < pending_players.size(); i++) {
                WhiteListPending.PendingPlayer existing_pending_player = pending_players.get(i);
                if (existing_pending_player.code == code && (System.currentTimeMillis() - existing_pending_player.time) <= time_limit ) {
                    player = existing_pending_player;
                    pending_players.remove(i);
                    break;
                }
            }

            WhiteListPending.save_pending_players(pending_players);
            return player;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static WhiteListPending.PendingPlayer add_pending_player(WhiteList.Player player)  {
        try {
            WhiteListPending.PendingPlayer new_pending_player = new WhiteListPending.PendingPlayer(player);
            boolean not_updated = true;
            List<WhiteListPending.PendingPlayer> allowed_players = get_pending_players();
            for (int i = 0; i < allowed_players.size(); i++) {
                WhiteListPending.PendingPlayer existing_player = allowed_players.get(i);
                if (Objects.equals(existing_player.name.toLowerCase(), player.name.toLowerCase())) {
                    allowed_players.set(i, new_pending_player);
                    not_updated = false;
                    break;
                }
            }
            if (not_updated) {
                allowed_players.add(new_pending_player);
            }

            WhiteListPending.save_pending_players(allowed_players);
            return new_pending_player;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void remove_pending_player(String name)  {
        try {
            List<WhiteListPending.PendingPlayer> pending_players = get_pending_players();
            for (int i = 0; i < pending_players.size(); i++) {
                WhiteListPending.PendingPlayer existing_pending_player = pending_players.get(i);
                if (Objects.equals(existing_pending_player.name.toLowerCase(), name.toLowerCase())) {
                    pending_players.remove(i);
                    break;
                }
            }
            WhiteListPending.save_pending_players(pending_players);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<WhiteListPending.PendingPlayer> get_pending_players() throws IOException {
        try {
            FileReader file_reader = new FileReader(get_path());
            BufferedReader reader = new BufferedReader(file_reader);
            List<WhiteListPending.PendingPlayer> players = new ArrayList<WhiteListPending.PendingPlayer>();
            String version = reader.readLine();
            String result;
            while ((result = reader.readLine()) != null) {
                String[] player = result.split(":");
                players.add(new WhiteListPending.PendingPlayer(player[0], player[1], Integer.parseInt(player[2]), Long.parseLong(player[3])));
            }
            return players;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public static void save_pending_players(List<WhiteListPending.PendingPlayer> pending_players) throws IOException {
        StringBuilder output = new StringBuilder(WhiteListPending.version + "\n");
        for (WhiteListPending.PendingPlayer pending_player : pending_players) {
            output.append(pending_player.name + ":" + pending_player.ip + ":" + pending_player.code + ":" + pending_player.time + "\n");
        }

        FileWriter file_writer = new FileWriter(get_path(), false);
        BufferedWriter writer = new BufferedWriter(file_writer);
        writer.write(output.toString());
        writer.close();
    }

    public static boolean exists(WhiteListPending.PendingPlayer new_player) throws IOException {
        List<WhiteListPending.PendingPlayer> players = WhiteListPending.get_pending_players();
        for (WhiteListPending.PendingPlayer player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(List<WhiteListPending.PendingPlayer> players, WhiteListPending.PendingPlayer new_player) throws IOException {
        for (WhiteListPending.PendingPlayer player : players) {
            if (Objects.equals(player.name.toLowerCase(), new_player.name.toLowerCase()) && Objects.equals(player.ip, new_player.ip)) {
                return true;
            }
        }
        return false;
    }
}
