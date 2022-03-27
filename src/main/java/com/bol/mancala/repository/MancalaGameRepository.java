package com.bol.mancala.repository;

import com.bol.mancala.model.MancalaGame;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MancalaGameRepository extends MongoRepository<MancalaGame, String> {
}
