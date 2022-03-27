package com.bol.mancala.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum PlayerTurn {
    PLAYER_A (0),
    PLAYER_B (1);

    private int turn;

}

