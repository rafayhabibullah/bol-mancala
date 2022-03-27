package com.bol.mancala.controller;

import com.bol.mancala.model.MancalaGame;
import com.bol.mancala.service.MancalaGameService;
import com.bol.mancala.service.MancalaSowService;
import com.bol.mancala.util.MancalaGameUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@RestController
@CrossOrigin
@Validated
@RequestMapping("/mancala")
public class MancalaGameController {

    private static final Logger log = LoggerFactory.getLogger(MancalaGameController.class);

    @Autowired
    private MancalaGameService gameService;

    @Autowired
    private MancalaSowService sowService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MancalaGame> createGame(@RequestParam @NotNull String firstPlayerName,
                                                  @RequestParam @NotNull String secondPlayerName) {
        MancalaGame game = gameService.createGame(firstPlayerName, secondPlayerName);
        MancalaGameUtil.printBoard(game);
        log.info("mancala game created : {}", game);
        return new ResponseEntity<>(game, HttpStatus.CREATED);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<MancalaGame> getGame(@PathVariable String gameId) {
        MancalaGame game = gameService.getGameById(gameId);
        if(game == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping("/{gameId}/sow")
    public ResponseEntity<MancalaGame> sow(@PathVariable String gameId,
                                               @RequestParam @NotNull @Min(1) @Max(6) int pitIndex) {
        MancalaGame game = sowService.sow(gameId, pitIndex);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }
}
