package com.mch.xoServer.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.mch.xoServer.model.Game;

public class GameMapper implements RowMapper<Game> {

	@Override
	public Game mapRow(ResultSet rs, int rowNum) throws SQLException {
		String player1 = rs.getString("player1");
		String player2 = rs.getString("player2");
		
		int n = rs.getInt("n");
		int k = rs.getInt("k");
		
		String tb = rs.getString("gameTable");
		char[][] table = new char[n][n];
		for (int i = 0; i < n; i++)
		  for (int j = 0; j < n; j++)
		    table[i][j] = tb.charAt(i * n + j);
		
		return new Game(player1, player2, table, n, k);
	}
}
