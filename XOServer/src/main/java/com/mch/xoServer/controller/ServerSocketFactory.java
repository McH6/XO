package com.mch.xoServer.controller;

import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.stereotype.Component;


@Component
public class ServerSocketFactory {

  public ServerSocketFactory() {}
  
  public ServerSocket get(int port) throws IOException {
    return new ServerSocket(port);
  }
}
