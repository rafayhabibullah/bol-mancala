package com.bol.mancala.service;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.exception.model.EntityNotFoundException;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MancalaGameService.class)
class MancalaGameServiceTest {

    @Value("${mancala.game.number.of.stones.per.pit}")
    private int numberOfStonesPerPit;

    @MockBean
    MancalaGameService mancalaGameService;

    @MockBean
    MancalaGameRepository mancalaGameRepository;

    @BeforeEach
    void setUp() {
        //MancalaGameRepository mancalaGameRepository = Mockito.mock(MancalaGameRepository.class);
        MancalaGame game = MancalaGame.builder()
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
                .build();

        mancalaGameService = new MancalaGameServiceImpl(mancalaGameRepository);

        Mockito.when(mancalaGameRepository.save(any(MancalaGame.class)))
                .thenAnswer((invocation) ->invocation.getArguments()[0]);
        Mockito.when(mancalaGameRepository.findById(anyString()))
                .thenReturn(Optional.of(game));
    }

    @Test
    void shouldCreateAndSaveNewGame_SUCCESS() {
        MancalaGame savedGame = mancalaGameService.createGame("Chandler", "Joe");
        assertGame(savedGame);
    }

    @Test
    void shouldGetGameById_SUCCESS() {
        MancalaGame game = mancalaGameService.getGameById("test-game-1");
        assertGame(game);
    }

    @Test
    void shouldGetGameById_FAILURE() {
        Mockito.doThrow(new EntityNotFoundException("game not found for test-game-2"))
                .when(mancalaGameRepository).findById("test-game-2");

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> mancalaGameService.getGameById("test-game-2"),
                "game not found for test-game-2"
        );
        assertTrue(exception.getMessage().contains("game not found for test-game-2"));
    }

    private void assertGame(MancalaGame savedGame) {
        assertThat(savedGame).isNotNull();

        assertThat(savedGame.getPlayers()).isNotNull();
        assertEquals(2, savedGame.getPlayers().size());

        assertEquals(PlayerTurn.PLAYER_A, savedGame.getPlayerTurn());

        assertEquals("Chandler", savedGame.getPlayers().get(0).getName());
        assertEquals("Joe", savedGame.getPlayers().get(1).getName());

        assertNotNull(savedGame.getPlayers().get(0).getPits());
        assertNotNull(savedGame.getPlayers().get(1).getPits());

        assertNotNull(savedGame.getPlayers().get(0).getBigPit());
        assertNotNull(savedGame.getPlayers().get(1).getBigPit());
    }

}
