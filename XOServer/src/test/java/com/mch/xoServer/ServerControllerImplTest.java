package com.mch.xoServer;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.mch.xoData.transferData.TransferData;
import com.mch.xoData.transferData.TransferType;
import com.mch.xoServer.controller.ServerSocketFactory;
import com.mch.xoServer.controller.impl.ServerControllerImpl;
import com.mch.xoServer.datasource.GameDataSource;
import com.mch.xoServer.model.Game;

public class ServerControllerImplTest {

  private static final String PLAYER_1 = "player1";
  private static final String PLAYER_2 = "player2";
  private static final int N = 20;
  private static final int K = 5;
  private static final int PORT = -1;

  // private OutputStream outStream;
  // private InputStream inStream;

  // private List<Game> games = new ArrayList<>();
  // private Game game;
  // private Queue<Integer> buffer = new LinkedList<>();

  @Mock
  // should be a mock with state (the Game)
  private GameDataSource dataSource;
  @Mock
  private ServerSocketFactory serverSocketFactory;

  @Mock
  private ServerSocket serverSocket;
  @Mock
  private Socket clientSocket;

  @Spy
  private ObjectOutputStream objectOutStream;
  @Spy
  private ObjectInputStream objectInStream;

  @Spy
  @InjectMocks
  // has a:
  // GameDataSource
  // ServerSocketFactory
  private ServerControllerImpl serverContImpl;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(serverSocketFactory.get(anyInt())).thenReturn(serverSocket);
    // we want only one client connection (would be nicer to just pause
    // like the original accept() would, but no clue how to do it)
    when(serverSocket.accept()).thenReturn(clientSocket).thenReturn(null);

    doReturn(objectOutStream).when(serverContImpl).createObjectOutputStream(clientSocket);
    doReturn(objectInStream).when(serverContImpl).createObjectInputStream(clientSocket);

    System.out.println(objectOutStream);
    System.out.println(PLAYER_1);
    TransferData<Boolean> logoutTransData = new TransferData<Boolean>(TransferType.LOGOUT, true,
        null, null);
    doReturn(PLAYER_1).doReturn(logoutTransData).when(objectInStream).readObject();

    doNothing().when(objectOutStream).close();
    doNothing().when(objectInStream).close();

    // View (GUI) specific stuff, don't execute
    doNothing().when(serverContImpl).appendEventLog(anyString());
    // stop() just closes sockets and streams, no reason to call it if we mocked
    // everything anyways
    doNothing().when(serverContImpl).stop();

    when(dataSource.loadGame(PLAYER_1, PLAYER_2)).thenReturn(getSavedGame(PLAYER_1, PLAYER_2));
  }

  @Test
  public void testStart() {
    // we only test a simple login/logout cycle
    serverContImpl.start(PORT);
    // if execution terminates without errors it is considered passed
  }

  private Game getSavedGame(String player1, String player2) {
    char[][] table = new char[N][N];
    table[0][3] = Game.X;
    table[1][2] = Game.X;
    table[1][3] = Game.X;
    table[2][2] = Game.X;
    table[3][3] = Game.X;
    table[2][3] = Game.O;
    return new Game(player1, player2, table, N, K);
  }

}
