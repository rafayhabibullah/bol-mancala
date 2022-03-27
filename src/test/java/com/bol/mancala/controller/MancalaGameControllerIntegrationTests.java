package com.bol.mancala.controller;

import com.bol.mancala.constant.Constants;
import com.bol.mancala.exception.model.EntityNotFoundException;
import com.bol.mancala.model.*;
import com.bol.mancala.repository.MancalaGameRepository;
import com.bol.mancala.service.MancalaGameServiceImpl;
import com.bol.mancala.service.MancalaSowServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MancalaGameController.class)
@Import({MancalaGameServiceImpl.class, MancalaSowServiceImpl.class})
class MancalaGameControllerIntegrationTests {

    @Value("${mancala.game.number.of.stones.per.pit}")
    private int numberOfStonesPerPit;

    @MockBean
    MancalaGameRepository mancalaGameRepository;

    @Autowired
    MockMvc mockMvc;

    MancalaGame game;

    String chandler = "Chandler";
    String joe = "Joe";

    @BeforeEach
    void setUp() {
        game = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name(chandler)
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, numberOfStonesPerPit))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name(joe)
                                .pits(IntStream.rangeClosed(0, Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .filter(i -> i < Constants.NUMBER_OF_PITS_PER_PLAYER)
                                        .mapToObj(i -> new Pit(i, numberOfStonesPerPit))
                                        .collect(Collectors.toList()))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_A)
                .build();

        when(mancalaGameRepository.findById(anyString())).thenReturn(Optional.of(game));
        when(mancalaGameRepository.save(any(MancalaGame.class))).
                thenAnswer((invocation) -> invocation.getArguments()[0]);
        when(mancalaGameRepository.findById("test-game")).thenThrow(new EntityNotFoundException("game not found for test-game"));

    }

    @Test
    void shouldCreateNewGame() throws Exception {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("firstPlayerName", "Chandler");
        queryParams.add("secondPlayerName", "Joe");

        this.mockMvc.perform(post("/mancala/create")
                        .queryParams(queryParams)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerTurn").value("PLAYER_A"));
    }

    @Test
    void shouldGetGame_SUCCESS() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/mancala/{gameId}", "test-game-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-game-1"));
    }

    @Test
    void shouldSow() throws Exception {
        MancalaGame response = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(Arrays.asList(new Pit(0, 6),
                                        new Pit(1, 0),
                                        new Pit(2, 7),
                                        new Pit(3, 7),
                                        new Pit(4, 7),
                                        new Pit(5, 7)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(Arrays.asList(new Pit(0, 7),
                                        new Pit(1, 6),
                                        new Pit(2, 6),
                                        new Pit(3, 6),
                                        new Pit(4, 6),
                                        new Pit(5, 6)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_B)
                .gameStatus(GameStatus.OTHER_PLAYER)
                .build();
        response.getPlayers().get(0).getBigPit().sow();

        this.mockMvc.perform(post("/mancala/test-game-1/sow")
                        .queryParam("pitIndex", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));

        Mockito.verify(mancalaGameRepository, times(1)).save(any(MancalaGame.class));
    }

    @Test
    void shouldSowLastStoneInBigPit() throws Exception {
        MancalaGame response = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(Arrays.asList(new Pit(0, 0),
                                        new Pit(1, 7),
                                        new Pit(2, 7),
                                        new Pit(3, 7),
                                        new Pit(4, 7),
                                        new Pit(5, 7)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(Arrays.asList(new Pit(0, 6),
                                        new Pit(1, 6),
                                        new Pit(2, 6),
                                        new Pit(3, 6),
                                        new Pit(4, 6),
                                        new Pit(5, 6)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_A)
                .gameStatus(GameStatus.BIG_PIT)
                .build();
        response.getPlayers().get(0).getBigPit().sow();

        this.mockMvc.perform(post("/mancala/test-game-1/sow")
                        .queryParam("pitIndex", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));

        Mockito.verify(mancalaGameRepository, times(1)).save(any(MancalaGame.class));
    }

    @Test
    void shouldSowLastStoneInCurrentEmptyPit() throws Exception {
        MancalaGame response = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(Arrays.asList(new Pit(0, 0),
                                        new Pit(1, 0),
                                        new Pit(2, 0),
                                        new Pit(3, 10),
                                        new Pit(4, 10),
                                        new Pit(5, 9)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(Arrays.asList(new Pit(0, 0),
                                        new Pit(1, 0),
                                        new Pit(2, 0),
                                        new Pit(3, 11),
                                        new Pit(4, 0),
                                        new Pit(5, 11)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_B)
                .gameStatus(GameStatus.CURRENT_PLAYER)
                .build();
        response.getPlayers().get(0).getBigPit().addStones(18);
        response.getPlayers().get(1).getBigPit().addStones(3);

        game.getPlayers().get(0).setPits(Arrays.asList(new Pit(0, 1),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 10),
                new Pit(4, 10),
                new Pit(5, 9)));
        game.getPlayers().get(1).setPits(Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 11),
                new Pit(4, 11),
                new Pit(5, 11)));
        game.getPlayers().get(0).getBigPit().addStones(6);
        game.getPlayers().get(1).getBigPit().addStones(3);
        game.setPlayerTurn(PlayerTurn.PLAYER_A);

        this.mockMvc.perform(post("/mancala/test-game-1/sow")
                        .queryParam("pitIndex", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));

        Mockito.verify(mancalaGameRepository, times(1)).save(any(MancalaGame.class));
    }

    @Test
    void shouldSowToGameOver() throws Exception {
        MancalaGame response = MancalaGame.builder()
                .id("test-game-1")
                .players(Arrays.asList(Player.builder()
                                .name("Chandler")
                                .pits(Arrays.asList(new Pit(0, 0),
                                        new Pit(1, 0),
                                        new Pit(2, 0),
                                        new Pit(3, 0),
                                        new Pit(4, 0),
                                        new Pit(5, 0)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build(),
                        Player.builder()
                                .name("Joe")
                                .pits(Arrays.asList(new Pit(0, 0),
                                        new Pit(1, 0),
                                        new Pit(2, 0),
                                        new Pit(3, 0),
                                        new Pit(4, 0),
                                        new Pit(5, 0)))
                                .bigPit(new BigPit(Constants.NUMBER_OF_PITS_PER_PLAYER))
                                .build())
                )
                .playerTurn(PlayerTurn.PLAYER_B)
                .gameStatus(GameStatus.GAME_OVER)
                .winner(Winner.builder()
                        .playerName(joe)
                        .score(39)
                        .build())
                .build();
        response.getPlayers().get(0).getBigPit().addStones(33);
        response.getPlayers().get(1).getBigPit().addStones(39);

        game.getPlayers().get(0).setPits(Arrays.asList(new Pit(0, 1),
                new Pit(1, 2),
                new Pit(2, 0),
                new Pit(3, 1),
                new Pit(4, 0),
                new Pit(5, 0)));
        game.getPlayers().get(1).setPits(Arrays.asList(new Pit(0, 0),
                new Pit(1, 0),
                new Pit(2, 0),
                new Pit(3, 0),
                new Pit(4, 0),
                new Pit(5, 1)));
        game.getPlayers().get(0).getBigPit().addStones(29);
        game.getPlayers().get(1).getBigPit().addStones(38);
        game.setPlayerTurn(PlayerTurn.PLAYER_B);

        this.mockMvc.perform(post("/mancala/test-game-1/sow")
                        .queryParam("pitIndex", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));

        Mockito.verify(mancalaGameRepository, times(1)).save(any(MancalaGame.class));
    }

    @Test
    void shouldNotCreateNewGame() throws Exception {
        this.mockMvc.perform(post("/mancala/test-game-1/sow")
                        .queryParam("pitIndex", "8")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldNotSow() throws Exception {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("firstPlayerName", "Chandler");

        this.mockMvc.perform(post("/mancala/create")
                        .queryParams(queryParams)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldGetGame_Failure() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/mancala/{gameId}", "test-game")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
