package com.mch.xoServer.dao.impl;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.mch.xoServer.dao.SpringGamesDao;
import com.mch.xoServer.model.Game;

@Component
public class SpringGamesDaoImpl implements SpringGamesDao {

  private JdbcTemplate jdbcTemplateObject;

  @Override
  @Autowired
  @Qualifier("dataSource")
  public void setDataSource(DataSource dataSource) {
      this.jdbcTemplateObject = new JdbcTemplate(dataSource);
  }

  @Override
  public Game retrieveGame(String player1, String player2) {
    String sql = "SELECT * from games where (player1 = '" + player1
        + "' AND player2 = '" + player2 + "') OR "
        + "(player1 = '" + player2 + "' AND player2 = '" + player1 + "')";
    SqlRowSet rowSet = null;
    try {
      rowSet = jdbcTemplateObject.queryForRowSet(sql);
    } catch (DataAccessException ex) {
      System.err.println(ex.getMessage());
      return null;
    }
    Game game = null;
    if (rowSet.next()) {
      String p1 = rowSet.getString("player1");
      String p2 = rowSet.getString("player2");

      char player1Mark = rowSet.getString("player1Mark").charAt(0);
      char player2Mark = rowSet.getString("player2Mark").charAt(0);

      int n = rowSet.getInt("n");
      int k = rowSet.getInt("k");

      String tb = rowSet.getString("gameTable");
      char[][] table = new char[n][n];
      for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
          table[i][j] = tb.charAt(i * n + j);

      game = new Game(p1, p2, player1Mark, player2Mark, table, n, k);
    }
    return game;
  }

  @Override
  public void saveGame(Game game) {
    String table = "";
    for (char[] row : game.getTable())
      for (char c : row)
        if (c == 'X' || c == 'O') {
          table += c;
        } else {
          table += " ";
        }
    
    String updatetSql = "UPDATE games SET "
        + "player1Mark='" + game.getMark(game.getPlayer1()) + "', "
        + "player2Mark='" + game.getMark(game.getPlayer2()) + "', "
        + "n=" + game.getN() + ", "
        + "k=" + game.getK() + ", "
        + "gameTable='" + table + "' "
        + "WHERE player1 = '" + game.getPlayer1() + "' AND player2 = '" + game.getPlayer2() + "' ";
//        + "IF @@ROWCOUNT=0 "
    String insertSql = "INSERT INTO games (player1, player2, gameTable, "
        + "player1Mark, player2Mark, n, k) VALUES ('"
        + game.getPlayer1() + "', '"
        + game.getPlayer2() + "', '"
        + table + "', '"
        + game.getMark(game.getPlayer1()) + "', '"
        + game.getMark(game.getPlayer2()) + "', "
        + game.getN() + ","
        + game.getK()
        + ");";
   
    try {
      int res = jdbcTemplateObject.update(updatetSql);
      if (res == 0)
        jdbcTemplateObject.execute(insertSql);
    } catch (DataAccessException ex) {
      System.err.println(ex.getMessage());
    }
  }
}
