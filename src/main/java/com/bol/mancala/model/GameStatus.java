package com.bol.mancala.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum GameStatus {
    BIG_PIT,
    IN_PROGRESS,
    OTHER_PLAYER,
    CURRENT_PLAYER,
    GAME_OVER
}
