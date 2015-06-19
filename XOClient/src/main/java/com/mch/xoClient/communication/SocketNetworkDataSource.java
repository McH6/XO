package com.mch.xoClient.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

@Component
public class SocketNetworkDataSource implements NetworkDataSource {

  // for I/O
  private Socket socket;
  private ObjectInputStream sInput; // to read from the socket
  private ObjectOutputStream sOutput; // to write on the socket

  @Override
  public void connect(String host, int port) throws UnknownHostException, IOException {
    socket = new Socket(host, port);
    sInput = new ObjectInputStream(socket.getInputStream());
    sOutput = new ObjectOutputStream(socket.getOutputStream());
  }

  @Override
  public InetAddress getInetAddress() {
    return socket.getInetAddress();
  }

  @Override
  public int getPort() {
    return socket.getPort();
  }

  @Override
  public void send(Object toSende) throws IOException {
    sOutput.writeObject(toSende);
  }

  @Override
  public void disconnect() throws IOException {
    if (sInput != null)
      sInput.close();
    if (sOutput != null)
      sOutput.close();
    if (socket != null)
      socket.close();
  }

  @Override
  public Object receive() throws ClassNotFoundException, IOException {
    return sInput.readObject();
  }
}
