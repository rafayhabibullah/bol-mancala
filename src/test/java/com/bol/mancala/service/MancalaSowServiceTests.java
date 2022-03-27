package com.bol.mancala.service;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.model.*;
import com.bol.mancala.repository.MancalaGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MancalaSowService.class)
class MancalaSowServiceTests {

    @Value("${mancala.game.number.of.stones.per.pit}")
    private int numberOfStonesPerPit;

    @MockBean
    MancalaSowService mancalaSowService;

    @MockBean
    MancalaGameService mancalaGameService;

    MancalaGame game;

    @BeforeEach
    void setUp() {
        MancalaGameService mancalaGameService = Mockito.mock(MancalaGameService.class);
        MancalaGameRepository mancalaGameRepository = Mockito.mock(MancalaGameRepository.class);
        game = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, numberOfStonesPerPit))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, numberOfStonesPerPit))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_A)
                .gameStatus(GameStatus.IN_PROGRESS)
                .build();

        mancalaSowService = new MancalaSowServiceImpl(mancalaGameService, mancalaGameRepository);

        Mockito.when(mancalaGameService.getGameById(anyString())).thenReturn(game);
        Mockito.when(mancalaGameRepository.save(any(MancalaGame.class)))
                .thenAnswer((invocation) ->invocation.getArguments()[0]);
        Mockito.when(mancalaGameRepository.findById(anyString()))
                .thenReturn(Optional.of(game));
    }

    @Test
    void sow_firstMoveByPlayerOne() {
        MancalaGame savedGame = mancalaSowService.sow("test-game-1", 3);

        List<Pit> player1Pits = Arrays.asList(new Pit(0, 6),
                new Pit(1, 6),
                new Pit(2, 0),
                new Pit(3, 7),
                new Pit(4, 7),
                new Pit(5, 7));
        BigPit player1BigPit = new BigPit(6);
        player1BigPit.sow();

        List<Pit> player2Pits = Arrays.asList(new Pit(0, 7),
                new Pit(1, 7),
                new Pit(2, 6),
                new Pit(3, 6),
                new Pit(4, 6),
                new Pit(5, 6));
        BigPit player2BigPit = new BigPit(6);

        assertEquals(player1Pits, savedGame.getPlayers().get(0).getPits());
        assertEquals(player1BigPit, savedGame.getPlayers().get(0).getBigPit());
        assertEquals(player2Pits, savedGame.getPlayers().get(1).getPits());
        assertEquals(player2BigPit, savedGame.getPlayers().get(1).getBigPit());
        assertEquals(PlayerTurn.PLAYER_B, savedGame.getPlayerTurn());
    }

    @Test
    void sow_lastStoneInCurrentPlayerBigPit() {
        MancalaGame savedGame = mancalaSowService.sow("test-game-1", 1);

        List<Pit> player1Pits = Arrays.asList(new Pit(0, 0),
                new Pit(1, 7),
                new Pit(2, 7),
                new Pit(3, 7),
                new Pit(4, 7),
                new Pit(5, 7));
        BigPit player1BigPit = new BigPit(6);
        player1BigPit.sow();

        assertEquals(player1Pits, savedGame.getPlayers().get(0).getPits());
        assertEquals(player1BigPit, savedGame.getPlayers().get(0).getBigPit());
        assertEquals(PlayerTurn.PLAYER_A, savedGame.getPlayerTurn());
    }

    @Test
    void sow_switchBoardsTwiceForPlayerAMove() {
        game.getPlayers().get(0).getPits().get(4).setStones(10);
        MancalaGame savedGame = mancalaSowService.sow("test-game-1", 5);

        List<Pit> player1Pits = Arrays.asList(new Pit(0, 7),
                new Pit(1, 7),
                new Pit(2, 6),
                new Pit(3, 6),
                new Pit(4, 0),
                new Pit(5, 7));
        BigPit player1BigPit = new BigPit(6);
        player1BigPit.sow();

        List<Pit> player2Pits = Arrays.asList(new Pit(0, 7),
                new Pit(1, 7),
                new Pit(2, 7),
                new Pit(3, 7),
                new Pit(4, 7),
                new Pit(5, 7));
        BigPit player2BigPit = new BigPit(6);

        assertEquals(player1Pits, savedGame.getPlayers().get(0).getPits());
        assertEquals(player1BigPit, savedGame.getPlayers().get(0).getBigPit());
        assertEquals(player2Pits, savedGame.getPlayers().get(1).getPits());
        assertEquals(player2BigPit, savedGame.getPlayers().get(1).getBigPit());
        assertEquals(PlayerTurn.PLAYER_B, savedGame.getPlayerTurn());
    }

    @Test
    void sow_applyLastStoneInCurrentEmptyPitRule() {
        List<Pit> player1Pits = Arrays.asList(new Pit(0, 1),
                new Pit(1, 0),
                new Pit(2, 10),
                new Pit(3, 10),
                new Pit(4, 9),
                new Pit(5, 0));
        BigPit player1BigPit = new BigPit(6);
        player1BigPit.setStones(6);

        List<Pit> player2Pits = Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 11),
                new Pit(4, 11),
                new Pit(5, 11));
        BigPit player2BigPit = new BigPit(6);
        player2BigPit.setStones(3);
        game.getPlayers().get(0).setPits(player1Pits);
        game.getPlayers().get(0).setBigPit(player1BigPit);
        game.getPlayers().get(1).setPits(player2Pits);
        game.getPlayers().get(1).setBigPit(player2BigPit);
        game.setPlayerTurn(PlayerTurn.PLAYER_A);

        MancalaGame savedGame = mancalaSowService.sow("test-game-1", 1);

        List<Pit> player1PitsResult = Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 10),
                new Pit(3, 10),
                new Pit(4, 9),
                new Pit(5, 0));

        List<Pit> player2PitsResult = Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 11),
                new Pit(4, 0),
                new Pit(5,  11));

        assertEquals(player1PitsResult, savedGame.getPlayers().get(0).getPits());
        assertEquals(18, savedGame.getPlayers().get(0).getBigPit().getStones());
        assertEquals(player2PitsResult, savedGame.getPlayers().get(1).getPits());
        assertEquals(3, savedGame.getPlayers().get(1).getBigPit().getStones());
        assertEquals(PlayerTurn.PLAYER_B, savedGame.getPlayerTurn());
    }

    @Test
    void sow_GameOverRule() {
        List<Pit> player1Pits = Arrays.asList(new Pit(0, 1),
                new Pit(1, 2),
                new Pit(2, 0),
                new Pit(3, 1),
                new Pit(4, 0),
                new Pit(5, 0));
        BigPit player1BigPit = new BigPit(6);
        player1BigPit.setStones(29);

        List<Pit> player2Pits = Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 0),
                new Pit(4, 0),
                new Pit(5, 1));
        BigPit player2BigPit = new BigPit(6);
        player2BigPit.setStones(38);
        game.getPlayers().get(0).setPits(player1Pits);
        game.getPlayers().get(0).setBigPit(player1BigPit);
        game.getPlayers().get(1).setPits(player2Pits);
        game.getPlayers().get(1).setBigPit(player2BigPit);
        game.setPlayerTurn(PlayerTurn.PLAYER_B);

        MancalaGame savedGame = mancalaSowService.sow("test-game-1", 6);

        assertEquals(33, savedGame.getPlayers().get(0).getBigPit().getStones());
        assertEquals(39, savedGame.getPlayers().get(1).getBigPit().getStones());
        assertEquals(GameStatus.GAME_OVER, savedGame.getGameStatus());
        assertEquals(39, savedGame.getWinner().getScore());
        assertEquals("Joe", savedGame.getWinner().getPlayerName());
    }
}
