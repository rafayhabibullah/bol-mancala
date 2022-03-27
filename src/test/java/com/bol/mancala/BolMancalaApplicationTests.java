package com.bol.mancala;

import com.bol.mancala.controller.MancalaGameController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BolMancalaApplicationTests {

	@Autowired
	MancalaGameController mancalaGameController;

	@Test
	void contextLoads() {
		assertThat(mancalaGameController).isNotNull();
	}

}
