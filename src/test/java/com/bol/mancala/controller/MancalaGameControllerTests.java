package com.bol.mancala.controller;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.model.*;
import com.bol.mancala.service.MancalaGameService;
import com.bol.mancala.service.MancalaSowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MancalaGameController.class)
class MancalaGameControllerTests {

    @MockBean
    MancalaGameController mancalaGameController;

    @BeforeEach
    void setUp() {
        MancalaGameService mancalaGameService = Mockito.mock(MancalaGameService.class);
        MancalaSowService mancalaSowService = Mockito.mock(MancalaSowService.class);
        mancalaGameController = new MancalaGameController(mancalaGameService, mancalaSowService);
        MancalaGame game = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, 6))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, 6))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_A)
                .build();
        Mockito.when(mancalaGameService.createGame(anyString(), anyString())).thenReturn(game);
        Mockito.when(mancalaGameService.getGameById("test-game-1")).thenReturn(game);
        Mockito.when(mancalaSowService.sow(anyString(), anyInt())).thenReturn(game);
    }

    @Test
    void shouldCreateNewGame() {
        ResponseEntity<MancalaGame> game = mancalaGameController.createGame("Chandler", "Joe");
        assertNotNull(game);
    }

    @Test
    void shouldGetGame_SUCCESS() {
        ResponseEntity<MancalaGame> game = mancalaGameController.getGame("test-game-1");
        assertNotNull(game.getBody());
        assertNotNull(game.getBody().getPlayers());
    }

    @Test
    void shouldGetGame_FAILURE() {
        ResponseEntity<MancalaGame> game = mancalaGameController.getGame("test-game-2");
        assertNull(game.getBody());
    }

    @Test
    void shouldSow_SUCCESS() {
        ResponseEntity<MancalaGame> game = mancalaGameController.sow("test-game-1",1);
        assertNotNull(game.getBody());
    }
}
