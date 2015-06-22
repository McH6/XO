package com.mch.xoClient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mch.xoClient.communication.NetworkDataSource;
import com.mch.xoClient.controller.ClientControllerImpl;
import com.mch.xoClient.view.ClientView;
import com.mch.xoData.transferData.TransferData;
import com.mch.xoData.transferData.TransferType;

public class ClientControllerImplTest {

  private static final String PLAYER_1 = "player1";
  private static final String PLAYER_2 = "player2";
  // private static final int N = 20;
  // private static final int K = 5;
  private static final int PORT = -1;
  private static final String HOST = "notlocalhost";

  @Mock
  private NetworkDataSource network;

  @Mock
  private ClientView view;

  @InjectMocks
  private ClientControllerImpl clientContImpl;

  // runs before EVERY @TEST method
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
System.out.println("hi");
    // define behavior for methods in NetworkDataSource interface
    doNothing().when(network).connect(HOST, PORT);
    doNothing().when(network).disconnect();
    // should return some random thing (127.0.0.1), not null to avoid crashing
    // things
    when(network.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
    when(network.getPort()).thenReturn(PORT);

    doNothing().when(network).send(any());
    // return different things on each call
    when(network.receive())
        .thenReturn(new TransferData<Boolean>(TransferType.LOGIN, true, null, null))
        .thenReturn(
            new TransferData<List<String>>(TransferType.WHOISIN, Arrays.asList(PLAYER_1, PLAYER_2),
                null, null))
        .thenReturn(
            new TransferData<Character>(TransferType.INVITE, ClientControllerImpl.X, PLAYER_1,
                PLAYER_2))
        .thenReturn(new TransferData<Boolean>(TransferType.WIN, true, null, null))
        .thenReturn(new TransferData<Integer>(TransferType.MARK, 0, null, null))
        .thenReturn(
            new TransferData<char[][]>(TransferType.LOAD, new char[0][0], PLAYER_1, PLAYER_2))
            // this should make the listener thread stop
        .thenThrow(new IOException());

    // define behavior for methods relevant in ClientView interface
    doNothing().when(view).log(anyString());
    doNothing().when(view).setUsers(anyListOf(String.class));
    doNothing().when(view).notifyInvited(anyChar(), anyString());
    doNothing().when(view).notifyGameEnded(anyBoolean());
    doNothing().when(view).showMessage(anyString());
    doNothing().when(view).putMark(anyInt(), anyChar());
  }

  @Test
  public void testStart() {
    clientContImpl.start(PLAYER_1, HOST, PORT);
  }

  @Test
  public void testStartException1() throws UnknownHostException, IOException {
    doThrow(new UnknownHostException()).when(network).connect(null, -1);
    clientContImpl.start("", null, -1);
  }

  @Test
  public void testStartException2() throws IOException {
    String random = "blabla";
    doThrow(new IOException()).when(network).send(random);
    clientContImpl.start(random, HOST, PORT);
  }

  @Test
  public void testStartException3() throws IOException {
    String random = "blabla";
    doThrow(new IOException()).when(network).send(random);
    doThrow(new IOException()).when(network).disconnect();
    clientContImpl.start(random, HOST, PORT);
  }

  @Test
  public void testStartException4() throws IOException, ClassNotFoundException {
    when(network.receive()).thenThrow(new IOException());
    clientContImpl.start("", HOST, PORT);
  }

  @Test
  public void testStartBadUserNameResponse() throws ClassNotFoundException, IOException {
    when(network.receive()).thenReturn(
        new TransferData<Boolean>(TransferType.WHOISIN, true, null, null));
    clientContImpl.start("", HOST, PORT);
  }

  @Test
  public void testStartTakenUserName() throws ClassNotFoundException, IOException {
    when(network.receive()).thenReturn(
        new TransferData<Boolean>(TransferType.LOGIN, false, null, null));
    clientContImpl.start("", HOST, PORT);
  }

  @Test
  public void testMark() {
    assertEquals(true, clientContImpl.mark(0));
  }

  @Test
  public void testInvite() {
    assertEquals(true, clientContImpl.invite(""));
  }

  @Test
  public void testDoGetUserList() {
    clientContImpl.doGetUserList();
  }

  @Test
  public void testDoLogout() {
    clientContImpl.doLogout();
  }

  @Test
  public void testSaveGame() {
    clientContImpl.saveGame();
  }

  @Test
  public void testLoadGame() {
    clientContImpl.loadGame();
  }

  // How to test changes made by the listener thread =>
  // => every test executes before that thread finishes ?? ..
  
  // runs after EVERY @TEST method
//  @AfterClass
//  public static void testStateAfterRun() {
//    assertEquals(ClientControllerImpl.X, clientContImpl.getMark());
//  }
}
