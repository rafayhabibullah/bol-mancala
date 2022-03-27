package com.bol.mancala.service;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.exception.model.EntityNotFoundException;
import com.bol.mancala.model.*;
import com.bol.mancala.repository.MancalaGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MancalaGameServiceImpl implements MancalaGameService {

    private static final Logger log = LoggerFactory.getLogger(MancalaGameServiceImpl.class);

    @Value("${mancala.game.number.of.stones.per.pit}")
    private int numberOfStonesPerPit;

    @Autowired
    private MancalaGameRepository gameRepository;

    @Override
    public MancalaGame getGameById(String gameId) {
        Optional<MancalaGame> mancalaGameOptional = gameRepository.findById(gameId);
        if (mancalaGameOptional.isEmpty())
            throw new EntityNotFoundException("game not found for " + gameId);

        if (mancalaGameOptional.get().getPlayers() == null || mancalaGameOptional.get().getPlayers().size() != 2 ||
                mancalaGameOptional.get().getPlayerTurn() == null)
            throw new EntityNotFoundException("game is invalid " + gameId);

        return mancalaGameOptional.get();
    }

    @Override
    public MancalaGame createGame(String firstPlayerName, String secondPlayerName) {
        log.info("create mancala game");
        MancalaGame mancalaGame = MancalaGame.builder()
                .players(Arrays.asList(
                        this.createPlayer(firstPlayerName),
                        this.createPlayer(secondPlayerName)
                ))
                .playerTurn(PlayerTurn.PLAYER_A)
                .build();
        log.info("saving game in repository");
        return gameRepository.save(mancalaGame);
    }

    private Player createPlayer(String playerName) {
        log.info("create mancala player : {}", playerName);

        return Player.builder()
                .name(playerName)
                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                        .mapToObj(i -> new Pit(i, numberOfStonesPerPit))
                        .collect(Collectors.toList()))
                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                .build();
    }

    public MancalaGameServiceImpl(MancalaGameRepository mancalaGameRepository) {
        this.gameRepository = mancalaGameRepository;
    }
}
