package com.bol.mancala.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class MancalaGame {
    private String id;
    private List<Player> players;
    private PlayerTurn playerTurn;
    private GameStatus gameStatus;
    private Winner winner;
}
