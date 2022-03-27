package com.bol.mancala.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pit {
    private int id;
    private int stones;

    @JsonIgnore
    public boolean isEmpty (){
        return this.stones == 0;
    }

    public int clear (){
        int currentPitStones = this.stones;
        this.stones = 0;
        return currentPitStones;
    }

    public void sow () {
        this.stones++;
    }

    public int sow(int numberOfStonesToMove) {
        this.stones ++;
        return --numberOfStonesToMove;
    }

    public void addStones (int stones){
        this.stones+= stones;
    }
}