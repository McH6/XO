package com.mch.xoServer.dao;

import javax.sql.DataSource;

import com.mch.xoServer.model.Game;

public interface SpringGamesDao {
	
	void setDataSource(DataSource dataSource);
	
	Game retrieveGame(String player1, String player2);

	void saveGame(Game game);
}
