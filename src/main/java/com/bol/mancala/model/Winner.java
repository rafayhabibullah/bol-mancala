package com.bol.mancala.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Winner {
    private String playerName;
    private int score;
}
