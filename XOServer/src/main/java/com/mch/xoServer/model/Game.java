package com.mch.xoServer.model;

public class Game {
  // player1Id + player2Id is PK
  // => only one saved game for player pairs
  private String player1;
  private String player2;

  private char player1Mark;
  private char player2Mark;

  private char[][] table;

  private int n;
  private int k;

  public Game(String player1, String player2, int n, int k) {
    super();
    this.player1 = player1;
    this.player2 = player2;
    this.player1Mark = 'X';
    this.player2Mark = 'O';
    this.n = n;
    this.k = k;
    this.table = new char[n][n];
  }

  public Game(String player1, String player2, char player1Mark,
      char player2Mark, char[][] table, int n, int k) {
    super();
    this.player1 = player1;
    this.player2 = player2;
    this.player1Mark = player1Mark;
    this.player2Mark = player2Mark;
    this.table = table;
    this.n = n;
    this.k = k;
  }

  public char getMark(String player) {
    if (player1.equals(player))
      return player1Mark;
    if (player2.equals(player))
      return player2Mark;
    // this should never happen
    return '_';
  }

  public boolean mark(String player, int row, int col) throws IndexOutOfBoundsException {
    char orig = table[row][col];
    // if already marked don't mark
    if (orig == player1Mark || orig == player2Mark)
      return false;
    table[row][col] = getMark(player);
    return verifyWin(row, col);
  }

  public String getPlayer1() {
    return player1;
  }

  public void setPlayer1(String player1) {
    this.player1 = player1;
  }

  public String getPlayer2() {
    return player2;
  }

  public void setPlayer2(String player2) {
    this.player2 = player2;
  }

  public char[][] getTable() {
    return table;
  }

  public void setTable(char[][] table) {
    this.table = table;
  }

  public int getN() {
    return n;
  }

  public void setN(int n) {
    this.n = n;
  }

  public int getK() {
    return k;
  }

  public void setK(int k) {
    this.k = k;
  }

  // return the userName of the winner or null of not finished yet
  private boolean verifyWin(int row, int col) {
    // check for this mark

    // horizontal
    if (k <= collect(row, col, 1, 0))
      return true;
    // vertical
    if (k <= collect(row, col, 0, 1))
      return true;
    // main diagonal
    if (k <= collect(row, col, 1, 1))
      return true;
    // secondary diagonal
    if (k <= collect(row, col, 1, -1))
      return true;
    return false;
  }

  // new mark row, new mark column, direction row, direction column
  private int collect(int row, int col, int dirRow, int dirCol) {
    int count = 1;
    char mark = table[row][col];

    int nextRow = row + dirRow;
    int nextCol = col + dirCol;
    // there is a next element and its marked with the mark
    while (0 <= nextRow && nextRow < n && 0 <= nextCol && nextCol < n
        && mark == table[nextRow][nextCol]) {
      count++;
      nextRow += dirRow;
      nextCol += dirCol;
    }

    // repeat for the other direction too
    nextRow = row - dirRow;
    nextCol = col - dirCol;
    // there is a next element and its marked with the mark
    while (0 <= nextRow && nextRow < n && 0 <= nextCol && nextCol < n
        && mark == table[nextRow][nextCol]) {
      count++;
      nextRow -= dirRow;
      nextCol -= dirCol;
    }
    return count;
  }

}
