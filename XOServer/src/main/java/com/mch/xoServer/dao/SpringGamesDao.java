package com.mch.xoServer.dao;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

import com.mch.xoServer.model.Game;

public interface SpringGamesDao {

  void setDataSource(DataSource dataSource);

  Game retrieveGame(String player1, String player2) throws DataAccessException;

  int saveGame(Game game) throws DataAccessException;
}
