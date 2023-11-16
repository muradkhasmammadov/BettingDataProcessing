import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class Player {
    private UUID playerId;
    private long balance;
    private int totalBets;
    private int totalWins;

    public Player(UUID playerId) {
        this.playerId = playerId;
        this.balance = 0;
        this.totalBets = 0;
        this.totalWins = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public boolean withdraw(int amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public void placeBet(int amount) {
        balance -= amount;
        totalBets++;
    }

    public void winBet(int amount) {
        balance += amount;
        totalWins++;
    }

    public BigDecimal getWinRate() {
        if (totalBets == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalWins).divide(BigDecimal.valueOf(totalBets), 2, RoundingMode.HALF_UP);
    }
}
