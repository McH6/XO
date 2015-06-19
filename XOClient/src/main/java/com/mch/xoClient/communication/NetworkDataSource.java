package com.mch.xoClient.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public interface NetworkDataSource {

  void connect(String host, int port) throws UnknownHostException, IOException;

  void disconnect() throws IOException;

  InetAddress getInetAddress();

  int getPort();

  void send(Object toSend) throws IOException;

  Object receive() throws ClassNotFoundException, IOException;

}
