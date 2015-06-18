package com.mch.xoServer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mch.xoServer.model.Game;

public class GameTest {

  private String player1 = "player1";
  private String player2 = "player2";
  private int n = 6;
  private int k = 3;
  private char x = 'X';
  private char o = 'O';
  private char[][] table;

  private Game game;

  @Before
  public void setUp() {
    // init the table to use to set up games later
    // 0 1 2 3 4 5
    // - - - - - - 0
    // - - X - - - 1
    // - X X X - - 2
    // - - X - - - 3
    // - - - - - - 4
    // - - - - - - 5
    table = new char[n][n];
    table[2][2] = x;
    table[1][2] = x;
    table[2][1] = x;
    table[2][3] = x;
    table[3][2] = x;
    table[0][0] = x;
    table[0][4] = x;
    table[4][0] = x;
    table[4][4] = x;
    // set up a general game
    game = new Game(player1, player2, n, k);
  }

  @Test
  public void testStuff() {
    // player1 should have X, player2 Y
    char p1Mark = game.getMark(player1);
    char p2Mark = game.getMark(player2);

    assertEquals(p1Mark, x);
    assertEquals(p2Mark, o);

    assertEquals(game.getN(), n);
    assertEquals(game.getK(), k);
  }

  @Test
  public void testMark() {
    // get a more specific game
    game = new Game(player1, player2, table, n, k);

    // test if you can overwrite previous marks (not OK)
    game.mark(player2, 2, 3);
    assertNotEquals(game.getTable()[2][3], o);

    // test private methods verifyWin and collect through
    // the calling public method mark

    // test wins from all directions
    assertTrue(game.mark(player1, 0, 2));
    assertTrue(game.mark(player1, 2, 0));
    assertTrue(game.mark(player1, 2, 4));
    assertTrue(game.mark(player1, 4, 2));

    table[1][2] = o;
    table[2][1] = o;
    table[2][3] = o;
    table[3][2] = o;
    table[0][2] = o;
    table[2][0] = o;
    table[2][4] = o;
    table[4][2] = o;

    assertTrue(game.mark(player1, 1, 1));
    assertTrue(game.mark(player1, 1, 3));
    assertTrue(game.mark(player1, 3, 1));
    assertTrue(game.mark(player1, 3, 3));

    // for (char[]cv : game.getTable())
    // System.out.println(cv);

    // clean the table
    game.setTable(new char[n][n]);
    // test in the corners
    assertFalse(game.mark(player1, 0, 0));
    assertFalse(game.mark(player1, 0, 5));
    assertFalse(game.mark(player1, 5, 0));
    assertFalse(game.mark(player1, 5, 5));
    // based on the implementation of Game, it is not always
    // possible to get 100% coverage on collect() method
    // you won't get out of the matrix bounds in every direction
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testMarkIndexOutOfBoundsException() {
    game.mark(player1, 100, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMarkException() {
    game.getMark("dsda");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEnemyException() {
    game.getEnemy("dsda");
  }

  @Test
  public void testGetEnemy() {
    assertEquals(game.getPlayer1(), game.getEnemy(player2));
    assertEquals(game.getPlayer2(), game.getEnemy(player1));
  }
}
