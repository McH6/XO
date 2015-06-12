package com.mch.xoServer.controller;

import com.mch.xoServer.view.ServerView;

public interface ServerController {

  void setView(ServerView serverGUImpl);

  void stop();

  void start(int port);

}
