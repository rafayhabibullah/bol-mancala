package com.bol.mancala.util;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MancalaGameUtil {

    private static final Logger log = LoggerFactory.getLogger(MancalaGameUtil.class);

    private MancalaGameUtil() {
    }

    public static void printBoard(MancalaGame game) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(game.getPlayers().get(0).getPits().get(i).getStones())
                    .append(" ");
        }
        stringBuilder.append(" | ")
                .append(game.getPlayers().get(0).getBigPit().getStones());
        log.debug("Player 1 : {}", stringBuilder);

        stringBuilder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            stringBuilder.append(game.getPlayers().get(1).getPits().get(i).getStones())
                    .append(" ");
        }
        stringBuilder.append(" | ")
                .append(game.getPlayers().get(1).getBigPit().getStones());
        log.debug("Player 2 : {}", stringBuilder);
    }

    public static void switchPlayer(MancalaGame game) {
        PlayerTurn playerTurn = game.getPlayerTurn();

        if (!GameStatus.GAME_OVER.equals(game.getGameStatus()) && !GameStatus.BIG_PIT.equals(game.getGameStatus()))
            game.setPlayerTurn(
                    playerTurn.equals(PlayerTurn.PLAYER_A) ? PlayerTurn.PLAYER_B : PlayerTurn.PLAYER_A);
    }

    public static List<Pit> switchBoard(MancalaGame game, List<Player> players) {
        game.setGameStatus(GameStatus.CURRENT_PLAYER.equals(game.getGameStatus()) ?
                GameStatus.OTHER_PLAYER : GameStatus.CURRENT_PLAYER);
        int nextBoard = (GameStatus.CURRENT_PLAYER.equals(game.getGameStatus())) ?
                game.getPlayerTurn().getTurn() : game.getPlayerTurn().getTurn() ^ 1;
        return players.get(nextBoard).getPits();
    }

    public static int getTotalStonesInPlayerPit(List<Player> players, int i) {
        return players.get(i)
                .getPits()
                .stream()
                .mapToInt(Pit::getStones)
                .sum();
    }

    public static void resetPitsToZero(int turn, List<Player> players) {
        players.get(turn).setPits(
                IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                        .mapToObj(i -> new Pit(i, 0))
                        .collect(Collectors.toList())
        );
    }
}
