package com.bol.mancala.service;

import com.bol.mancala.model.MancalaGame;

public interface MancalaSowService {
    MancalaGame sow(String gameId, int pitIndex);
}
