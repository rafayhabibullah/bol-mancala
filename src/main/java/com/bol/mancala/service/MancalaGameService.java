package com.bol.mancala.service;

import com.bol.mancala.model.MancalaGame;

public interface MancalaGameService {

    MancalaGame createGame(String firstPlayerName, String secondPlayerName);
    MancalaGame getGameById(String gameId);

}
