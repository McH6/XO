package com.mch.xoServer.controller.impl;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mch.xoData.defaults.Defaults;
import com.mch.xoData.exception.XOException;
import com.mch.xoData.transferData.TransferData;
import com.mch.xoData.transferData.TransferType;
import com.mch.xoServer.controller.ServerController;
import com.mch.xoServer.controller.ServerSocketFactory;
import com.mch.xoServer.datasource.GameDataSource;
import com.mch.xoServer.model.Game;
import com.mch.xoServer.view.ServerView;
import com.mch.xoServer.view.awtEvent.MyEventLogAWTEvent;
import com.mch.xoServer.view.awtEvent.MyMessageLogAWTEvent;

// the actual server
@Component
public class BetterServerController implements ServerController {

  // list of client handling threads
  // it does not have to be synchronized, all accessing methods are..
  private Map<String, ClientHandlerThread> clientThreads;

  private final ServerSocketFactory serverSocketFactory;
  private final GameDataSource dataSource;
  
  private ServerSocket serverSocket;
  private ServerView view;

  @Autowired
  public BetterServerController(final GameDataSource dataSource,
      final ServerSocketFactory serverSocketFactory) {
    this.dataSource = dataSource;
    this.serverSocketFactory = serverSocketFactory;
    // ArrayList for the Client list
    clientThreads = new HashMap<>();
  }

  @Override
  public void setView(ServerView view) {
    this.view = view;
  }

  // @prototype
  public void start(int port) {
    /* create socket server and wait for connection requests */
    try {
      // the socket used by the server
      serverSocket = serverSocketFactory.get(port);
      String waitMsg = "Server waiting for Clients on port " + port + "...";
      // infinite loop to wait for connections
      appendEventLog(waitMsg);
      // accept connection
      Socket socket = null;
      while ((socket = serverSocket.accept()) != null) {
        // format message saying we are waiting
        appendEventLog(waitMsg);
        // make a handler thread
        ClientHandlerThread t = null;
        try {
          t = new ClientHandlerThread(socket);
          // if returned with exception thread wont be created
          clientThreads.put(t.userName, t); // save it in the Map
          t.start();
        } catch (XOException e) {
          appendEventLog(e.getMessage());
        }
        // continue with next iteration
      }
      // Error occurred try and stop
      try {
        stop();
      } catch (Exception e) {
        appendEventLog("Exception closing the server and clients: " + e);
      }
    } catch (IOException e) {
      // the serverSocket.accept() will return with an Exception,
      // but no problems here
      appendEventLog("Server terminated: " + e.getMessage());
    }
  }

  // stop the server
  public void stop() {
    // close client connection
    for (ClientHandlerThread clientThread : clientThreads.values()) {
      clientThread.closeAndStop();
    }
    // close the server socket
    try {
      serverSocket.close();
    } catch (IOException e) {
      appendEventLog("" + e);
    }
  }

  // for a client who log off using the LOGOUT message
  synchronized void remove(ClientHandlerThread clientHandler) {
    clientThreads.remove(clientHandler.userName);
  }

  public void appendEventLog(String str) {
    // Instance of the main thread
    Object target = view;
    EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    eventQueue.postEvent(new MyEventLogAWTEvent(target, str));
  }

  private void appendMessageLog(String str) {
    // Instance of the main thread
    Object target = view;
    EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    eventQueue.postEvent(new MyMessageLogAWTEvent(target, str));
  }

  public ObjectOutputStream createObjectOutputStream(Socket socket) throws IOException {
    return new ObjectOutputStream(socket.getOutputStream()); 
  }
  public ObjectInputStream createObjectInputStream(Socket socket) throws IOException {
    return new ObjectInputStream(socket.getInputStream());
  }

  /** One instance of this thread will run for each client */
  private class ClientHandlerThread extends Thread {
    // the socket where to listen/talk
    private Socket socket;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    // the user name of the Client
    // access the enemyName through the Game instance
    private String userName;
    // the date I connected as String
    private String date;
    // to hold the score, each mark is the userName of who put it there
    private Game game;
    
    public ClientHandlerThread(Socket socket) throws XOException {
      this.socket = socket;
      /* Creating both Data Stream */
      try {
        // create output first
        System.out.println("real:" + socket);
        this.sOutput = createObjectOutputStream(this.socket);
        this.sInput = createObjectInputStream(this.socket);
        // read the user name String, the only time a string is sent,
        // expect TransferData after this
        // mock
        userName = (String) sInput.readObject();
        
        // check if user name is not taken
        if (clientThreads.get(userName) != null) {
          // reply and abort
          sOutput.writeObject(new TransferData<Boolean>(TransferType.LOGIN, false, null, userName));
          throw new XOException("User Name taken.");
        } else {
          // reply positively
          sOutput.writeObject(new TransferData<Boolean>(TransferType.LOGIN, true, null, userName));
        }

        appendEventLog(userName + " just connected.");
      } catch (IOException e) {
        throw new XOException("Exception creating new Input/output Streams.");
      } catch (ClassNotFoundException e) {
        throw new XOException("Unexpected object read form stream.");
      }
      date = new Date().toString() + "\n";
    }

    // what will run forever
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
      // to loop until LOGOUT
      boolean keepGoing = true;
      while (keepGoing) {
        // read Object
        TransferData<?> cm = null;
        try {
          cm = (TransferData<?>) sInput.readObject();
        } catch (IOException e) {
          appendEventLog(userName + " Exception reading Streams: " + e);
          break;
        } catch (ClassNotFoundException e2) {
          break;
        }

        TransferType transType = cm.getType();
        // Switch on the type of message receive
        switch (transType) {
        case INVITE:
          // from is X, to is O
          startGame(cm.getFrom(), cm.getTo());
          writeMsg(
              new TransferData<Character>(TransferType.INVITE, 'X',
                  this.game.getEnemy(this.userName), null), clientThreads.get(this.userName));
          writeMsg(new TransferData<Character>(TransferType.INVITE, 'O', this.userName, null),
              clientThreads.get(this.game.getEnemy(this.userName)));
          break;
        case SAVE:
          dataSource.saveGame(this.game);
          break;
        case LOAD:
          Game loadGame = dataSource.loadGame(this.userName, this.game.getEnemy(this.userName));
          char[][] response = null;
          if (loadGame != null) {
            this.game = loadGame;
            clientThreads.get(this.game.getPlayer2()).game = loadGame;
            response = this.game.getTable();
          }
          writeMsg(
              new TransferData<char[][]>(TransferType.LOAD, response, game.getPlayer1(),
                  game.getPlayer2()), clientThreads.get(this.game.getPlayer1()));
          writeMsg(
              new TransferData<char[][]>(TransferType.LOAD, response, game.getPlayer1(),
                  game.getPlayer2()), clientThreads.get(this.game.getPlayer2()));
        case MARK:
          if (cm.getTo() != null) {
            String from = cm.getFrom();
            String to = cm.getTo();
            forwardMessage((TransferData<Integer>) cm, this);
            int loc = (Integer) cm.getMessage();
            int row = loc / Defaults.N;
            int col = loc % Defaults.N;
            boolean won = game.mark(cm.getFrom(), row, col);
            appendMessageLog(userName + " marked at [" + row + ", " + col + "]");
            if (won) {
              appendMessageLog(userName + " won the game: " + userName + " vs. "
                  + this.game.getEnemy(this.userName));
              // view.appendEvent(clientThreads.get(from).socket.toString());
              // view.appendEvent(clientThreads.get(to).socket.toString());
              writeMsg(new TransferData<Boolean>(TransferType.WIN, true, null, cm.getFrom()),
                  clientThreads.get(from));
              writeMsg(new TransferData<Boolean>(TransferType.WIN, false, null, cm.getTo()),
                  clientThreads.get(to));
            }
          }
          break;
        case LOGOUT:
          appendEventLog(userName + " disconnected with a LOGOUT message.");
          keepGoing = false;
          break;
        case WHOISIN:
          List<String> userList = new ArrayList<String>();
          // scan all the users connected
          for (ClientHandlerThread ct : clientThreads.values())
            if (ct.game == null && !ct.userName.equals(this.userName))
              userList.add(ct.userName + " -- since " + ct.date);
          writeMsg(new TransferData<List<String>>(TransferType.WHOISIN, userList, null, null), this);
          break;
        default:
          appendEventLog("Unrecognized data received!");
        }
      }
      // remove myself from the arrayList containing the list of the
      // connected Clients
      remove(this);
      closeAndStop();
    }

    private void startGame(String from, String to) {
      ClientHandlerThread cl1 = clientThreads.get(from);
      ClientHandlerThread cl2 = clientThreads.get(to);
      Game game = new Game(from, to, Defaults.N, Defaults.K);
      cl1.game = game;
      cl2.game = game;
    }

    // try to close everything
    private void closeAndStop() {
      // try to close the connection
      try {
        if (sOutput != null)
          sOutput.close();
        if (sInput != null)
          sInput.close();
        if (socket != null)
          socket.close();
      } catch (IOException e) {
        appendEventLog("Error closing client handler: " + e);
      } finally {
        try {
          this.interrupt();
        } catch (SecurityException ee) {
          appendEventLog("Error stopping the client handler thread: " + ee);
        }
      }
    }

    // Write to the Client output stream
    private synchronized boolean writeMsg(TransferData<?> msg, ClientHandlerThread to) {
      // if Client is still connected send the message to it
      if (!to.socket.isConnected()) {
        to.closeAndStop();
        return false;
      }
      // write the message to the stream
      try {
        to.sOutput.writeObject(msg);
      }
      // if an error occurs, do not abort just inform the user
      catch (IOException e) {
        // to.appendEventLog("Error sending message to " + userName);
        // to.appendEventLog(e.toString());
      }
      return true;
    }

    // write to specific Client
    private synchronized boolean forwardMessage(TransferData<?> cm, ClientHandlerThread fromC) {
      // add HH:mm:ss and \n to the message
      appendMessageLog("" + cm.getMessage()); // append in the room window

      ClientHandlerThread toC = clientThreads.get(cm.getTo());
      // if client is not online send message back to sender
      if (toC == null) {
        // toC = fromC;
        // cm = new TransferData<>(cm.getType(), "Client "
        // + cm.getTo() + " does not exist anymore", cm.getFrom(),
        // cm.getFrom());
        appendEventLog("---Forwarding failed!");
        return false;
      }
      Socket socket = toC.socket;
      ObjectOutputStream sOutput = toC.sOutput;
      // if Client is still connected send the message to it
      if (!socket.isConnected()) {
        closeAndStop();
        return false;
      }
      // write the message to the stream
      try {
        sOutput.writeObject(cm);
      }
      // if an error occurs, do not abort just inform the user
      catch (IOException e) {
        appendEventLog("Error sending message to " + userName);
        appendEventLog(e.toString());
      }
      return true;
    }
  }

}
