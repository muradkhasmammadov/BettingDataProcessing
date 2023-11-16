import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            Map<UUID, Match> matches = readMatchData("src/match_data.txt");
            Map<UUID, Player> players = readPlayerData("src/player_data.txt", matches);

            processPlayerActions(players, matches);

            writeResults("src/result.txt", players, matches);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<UUID, Player> readPlayerData(String fileName, Map<UUID, Match> matches) throws IOException {
        Map<UUID, Player> players = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                UUID playerId = UUID.fromString(tokens[0]);
                Player player = players.computeIfAbsent(playerId, id -> new Player(id));

                String operation = tokens[1];
                switch (operation) {
                    case "DEPOSIT":
                        player.deposit(Integer.parseInt(tokens[3]));
                        break;
                    case "WITHDRAW":
                        player.withdraw(Integer.parseInt(tokens[3]));
                        break;
                    case "BET":
                        UUID matchId = UUID.fromString(tokens[2]);
                        int betAmount = Integer.parseInt(tokens[3]);
                        String side = tokens[4];
                        handleBet(player, matches, matchId, betAmount, side);
                        break;
                }
            }
        }
        return players;
    }

    private static Map<UUID, Match> readMatchData(String fileName) throws IOException {
        Map<UUID, Match> matches = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                UUID matchId = UUID.fromString(tokens[0]);
                double rateA = Double.parseDouble(tokens[1]);
                double rateB = Double.parseDouble(tokens[2]);
                String result = tokens[3];
                matches.put(matchId, new Match(matchId, rateA, rateB, result));
            }
        }
        return matches;
    }

    private static void processPlayerActions(Map<UUID, Player> players, Map<UUID, Match> matches) {
        for (Player player : players.values()) {
            List<String> playerActions = getPlayerActions(player.getPlayerId(), "src/player_data.txt");
            for (String action : playerActions) {
                String[] tokens = action.split(",");
                String operation = tokens[1];
                switch (operation) {
                    case "DEPOSIT":
                        player.deposit(Integer.parseInt(tokens[3]));
                        break;
                    case "WITHDRAW":
                        player.withdraw(Integer.parseInt(tokens[3]));
                        break;
                    case "BET":
                        UUID matchId = UUID.fromString(tokens[2]);
                        int betAmount = Integer.parseInt(tokens[3]);
                        String side = tokens[4];
                        handleBet(player, matches, matchId, betAmount, side);
                        break;
                }
            }
        }
    }

    private static void handleBet(Player player, Map<UUID, Match> matches, UUID matchId, int betAmount, String side) {
        Match match = matches.get(matchId);
        if (match != null) {
            if (player.withdraw(betAmount)) {
                if (match.getResult().equals(side)) {
                    int winnings = (int) (betAmount * (side.equals("A") ? match.getRateA() : match.getRateB()));
                    player.winBet(winnings);
                }
            }
        }
    }


    private static void writeResults(String fileName, Map<UUID, Player> players, Map<UUID, Match> matches) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writeLegitimatePlayers(writer, players);
            writer.write("\n");
            writeIllegitimatePlayers(writer, players, matches);
            writer.write("\n");
            writeCasinoBalance(writer, players);
        }
    }



    private static List<String> getPlayerActions(UUID playerId, String fileName) {
        List<String> playerActions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                UUID currentId = UUID.fromString(tokens[0]);
                if (playerId.equals(currentId)) {
                    playerActions.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerActions;
    }
    private static void writeLegitimatePlayers(FileWriter writer, Map<UUID, Player> players) throws IOException {
        for (Player player : players.values()) {
            writer.write(player.getPlayerId() + " " + player.getBalance() + " " + player.getWinRate() + "\n");
        }
    }
    private static boolean isIllegitimateAction(Player player, Map<UUID, Match> matches, String operation, String[] tokens) {
        switch (operation) {
            case "BET":
                UUID matchId = UUID.fromString(tokens[2]);
                int betAmount = Integer.parseInt(tokens[3]);
                String side = tokens[4];
                Match match = matches.get(matchId);
                return player.getBalance() < betAmount || (match != null && !match.getResult().equals(side));
            case "WITHDRAW":
                int withdrawAmount = Integer.parseInt(tokens[3]);
                return player.getBalance() < withdrawAmount;
            default:
                return false;
        }
    }

    private static void writeIllegitimatePlayers(FileWriter writer, Map<UUID, Player> players, Map<UUID, Match> matches) throws IOException {
        for (Player player : players.values()) {
            List<String> playerActions = getPlayerActions(player.getPlayerId(), "src/player_data.txt");
            for (String action : playerActions) {
                String[] tokens = action.split(",");
                String operation = tokens[1];
                if (isIllegitimateAction(player, matches, operation, tokens)) {
                    writeIllegitimateAction(writer, tokens);
                }
            }
        }
    }


    private static void writeIllegitimateAction(FileWriter writer, String[] tokens) throws IOException {
        writer.write(tokens[0] + " " + tokens[1]);
        for (int i = 2; i < tokens.length; i++) {
            writer.write(" " + tokens[i]);
        }
        writer.write("\n");
    }

    private static void writeCasinoBalance(FileWriter writer, Map<UUID, Player> players) throws IOException {
        long casinoBalance = 0;
        for (Player player : players.values()) {
            casinoBalance += player.getBalance() - 0;
        }
        writer.write("Casino Balance: " + casinoBalance + "\n");
    }
}