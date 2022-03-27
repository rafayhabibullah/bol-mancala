package com.bol.mancala.service;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.model.*;
import com.bol.mancala.repository.MancalaGameRepository;
import com.bol.mancala.util.MancalaGameUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class MancalaSowServiceImpl implements MancalaSowService {

    private static final Logger log = LoggerFactory.getLogger(MancalaSowServiceImpl.class);

    @Autowired
    MancalaGameService gameService;

    @Autowired
    MancalaGameRepository gameRepository;

    @Override
    public MancalaGame sow(String gameId, int pitIndex) {
        MancalaGame game = gameService.getGameById(gameId);

        log.info("current turn : {}", game.getPlayerTurn());

        sowRight(game, game.getPlayers(), pitIndex);

        this.gameOverRule(game, game.getPlayers());
        MancalaGameUtil.switchPlayer(game);

        log.info("next turn : {}", game.getPlayerTurn());
        return gameRepository.save(game);
    }

    private void sowRight(MancalaGame game, List<Player> players, int pitIndex) {
        game.setGameStatus(GameStatus.CURRENT_PLAYER);

        PlayerTurn playerTurn = game.getPlayerTurn();
        List<Pit> currentBoard = players.get(playerTurn.getTurn()).getPits();
        BigPit currentBigPit = players.get(playerTurn.getTurn()).getBigPit();

        int numberOfStonesToMove = currentBoard.get(pitIndex - 1).clear();

        while (numberOfStonesToMove > 0) {
            if (pitIndex < Constants.NUMBER_OF_PITS_PER_PLAYER) {
                numberOfStonesToMove = currentBoard.get(pitIndex).sow(numberOfStonesToMove);
                this.lastStoneInEmptyPitRule(game, numberOfStonesToMove, pitIndex);
                pitIndex++;
            } else {
                numberOfStonesToMove = sowCurrentPlayerBigPit(game, currentBigPit, numberOfStonesToMove);

                if (numberOfStonesToMove == 0)
                    game.setGameStatus(GameStatus.BIG_PIT);
                else
                    currentBoard = MancalaGameUtil.switchBoard(game, players);

                pitIndex = 0;
            }
        }
    }

    private int sowCurrentPlayerBigPit(MancalaGame game, BigPit currentBigPit, int numberOfStonesToMove) {
        return GameStatus.CURRENT_PLAYER.equals(game.getGameStatus()) ?
                currentBigPit.sow(numberOfStonesToMove) : numberOfStonesToMove;
    }

    private void lastStoneInEmptyPitRule(MancalaGame game, int numberOfStonesToMove, int pitIndex) {
        if (numberOfStonesToMove != 0)
            return;

        List<Player> players = game.getPlayers();
        List<Pit> currentBoard = players.get(game.getPlayerTurn().getTurn()).getPits();
        List<Pit> opponentBoard = players.get(game.getPlayerTurn().getTurn() ^ 1).getPits();
        BigPit currentBigPit = players.get(game.getPlayerTurn().getTurn()).getBigPit();
        int opponentPitIndex = opponentBoard.size() - pitIndex - 1;

        if (GameStatus.CURRENT_PLAYER.equals(game.getGameStatus()) &&
                currentBoard.get(pitIndex).getStones() == 1 &&
                !opponentBoard.get(opponentPitIndex).isEmpty()) {
            int currentPitStones = currentBoard.get(pitIndex).clear();
            int opponentPitStones = opponentBoard.get(opponentPitIndex).clear();
            currentBigPit.addStones(currentPitStones + opponentPitStones);
        }
    }

    private void gameOverRule(MancalaGame game, List<Player> players) {
        int currentPlayerStones = MancalaGameUtil.getTotalStonesInPlayerPit(players, game.getPlayerTurn().getTurn());

        if (currentPlayerStones != 0)
            return;

        int otherPlayerStones = MancalaGameUtil.getTotalStonesInPlayerPit(players, game.getPlayerTurn().getTurn() ^ 1);

        players.get(game.getPlayerTurn().getTurn()).getBigPit().addStones(currentPlayerStones);
        players.get(game.getPlayerTurn().getTurn() ^ 1).getBigPit().addStones(otherPlayerStones);

        MancalaGameUtil.resetPitsToZero(game.getPlayerTurn().getTurn() ^ 1, players);

        Optional<Player> winningPlayer = players.stream()
                .max(Comparator.comparing(player -> player.getBigPit().getStones()));

        if (winningPlayer.isPresent()) {
            Winner winner = new Winner(players.get(game.getPlayerTurn().getTurn()).getName(),
                    winningPlayer.get().getBigPit().getStones());
            game.setWinner(winner);
            game.setGameStatus(GameStatus.GAME_OVER);
        }
    }
}
