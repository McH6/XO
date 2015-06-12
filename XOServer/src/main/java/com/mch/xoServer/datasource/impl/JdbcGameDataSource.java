package com.mch.xoServer.datasource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mch.xoServer.dao.SpringGamesDao;
import com.mch.xoServer.datasource.GameDataSource;
import com.mch.xoServer.model.Game;

@Component
public class JdbcGameDataSource implements GameDataSource {
	@Autowired
	//private GamesDao gamesDao;
	private SpringGamesDao gamesDao;

	@Override
	public Game loadGame(String player1, String player2) {
		return gamesDao.retrieveGame(player1, player2);
	}

  @Override
  public void saveGame(Game game) {
    gamesDao.saveGame(game);
  }

}
