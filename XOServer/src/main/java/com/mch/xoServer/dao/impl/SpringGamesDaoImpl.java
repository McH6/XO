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
  public Game retrieveGame(String player1, String player2)
      throws DataAccessException {
    // !! will load both player1 vs player2 AND player2 vs player1 games
    String sql = "SELECT * from games where (player1 = '" + player1
        + "' AND player2 = '" + player2 + "') ";
    SqlRowSet rowSet = null;
    try {
      rowSet = jdbcTemplateObject.queryForRowSet(sql);
    } catch (DataAccessException ex) {
      throw ex;
    }
    Game game = null;
    // should have just one element (Primary Key)
    if (rowSet.next()) {
      String p1 = rowSet.getString("player1");
      String p2 = rowSet.getString("player2");

      int n = rowSet.getInt("n");
      int k = rowSet.getInt("k");

      String tb = rowSet.getString("gameTable");
      char[][] table = new char[n][n];
      for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
          table[i][j] = tb.charAt(i * n + j);

      game = new Game(p1, p2, table, n, k);
    }
    return game;
  }

  @Override
  public int saveGame(Game game) throws DataAccessException {
    String table = "";
    for (char[] row : game.getTable())
      for (char c : row)
        if (c == Game.X || c == Game.O) {
          table += c;
        } else {
          table += Game.SPACE;
        }

    try {
      String updatetSql = "UPDATE games SET n=" + game.getN() + ", " + "k="
          + game.getK() + ", " + "gameTable='" + table + "' "
          + "WHERE player1 = '" + game.getPlayer1() + "' AND player2 = '"
          + game.getPlayer2() + "' ";

      int res = jdbcTemplateObject.update(updatetSql);
      if (res != 0) {
        // updated old game
        return 2;
      } else {
        // no lines updated => insert
        String insertSql = "INSERT INTO games (player1, player2, gameTable, n, k) "
            + "VALUES ('"
            + game.getPlayer1()
            + "', '"
            + game.getPlayer2()
            + "', '" + table + "', " + game.getN() + "," + game.getK() + ");";
        jdbcTemplateObject.execute(insertSql);
        return 1;
      }
    } catch (DataAccessException ex) {
      throw ex;
    }
  }
}
