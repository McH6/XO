package com.mch.xoClient.controller;

public interface ClientController {

  boolean mark(int location);

  char getMark();

  void doLogout();

  void doGetUserList();

  boolean invite(String to);

  boolean start(String userName, String host, int port);

  void saveGame();

  void loadGame();

}
