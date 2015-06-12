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
    /*
     * - - - X - -
     * - - X X - -
     * - - X Y - -
     * - - - X - -
     * - - - - - -
     * - - - - - -
     */
    table = new char[n][n];
    table[0][3] = x;
    table[1][2] = x;
    table[1][3] = x;
    table[2][2] = x;
    table[3][3] = x;
    table[2][3] = o;
    
    game = new Game(player1, player2, n, k);
  }

  @Test
  public void testMark() {
    // player1 should have X, player2 Y
    char p1Mark = game.getMark(player1);
    char p2Mark = game.getMark(player2);

    assertEquals(p1Mark, x);
    assertEquals(p2Mark, o);

    game.setTable(table);

    // test if you can overwrite previous marks (not OK)
    game.mark(player1, 2, 3);
    assertNotEquals(game.getTable()[2][3], x);

    // test private methods verifyWin and collect through
    // the calling public method mark
    
    // test a not win
    assertFalse(game.mark(player2, 3, 3));
    
    // test wins from all directions
    assertTrue(game.mark(player1, 1, 1));
    assertTrue(game.mark(player1, 0, 2));
    assertTrue(game.mark(player1, 2, 1));
    assertTrue(game.mark(player1, 4, 4));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testMarkIndexOutOfBoundsException() {
    game.mark(player1, 100, 100);
  }
}
