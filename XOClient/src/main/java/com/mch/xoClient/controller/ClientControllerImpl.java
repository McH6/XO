package com.mch.xoClient.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mch.xoClient.view.ClientView;
import com.mch.xoData.exception.XOException;
import com.mch.xoData.transferData.TransferData;
import com.mch.xoData.transferData.TransferType;


@Component
public class ClientControllerImpl implements ClientController{
  
	// for I/O
	private ObjectInputStream sInput; // to read from the socket
	private ObjectOutputStream sOutput; // to write on the socket
	private Socket socket;

  private String userName;
	private String enemyName;
	private char mark;
	
	@Autowired
	private ClientView view;
	
	// start the dialog
	@SuppressWarnings("unchecked")
	public boolean start(String userName, String host, int port) {
	  this.userName = userName;
		// try to connect to the server
		try {
			socket = new Socket(host, port);
		}
		// if it failed not much I can so
		catch (Exception ec) {
			updateView("Error connectiong to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":"
				+ socket.getPort();
		updateView(msg);

		/* Creating both Data Stream */
		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
			updateView("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// Send user name to the server this is the only message that we
		// will send as a String. All other messages will be TransferData objects
		try {
			sOutput.writeObject(userName);
		} catch (IOException eIO) {
			updateView("Exception doing login : " + eIO);
			disconnect();
			return false;
		}

		// wait the server response if the user name is not taken, block the
		// client GUI
		TransferData<Boolean> cm = null;
		try {
			cm = (TransferData<Boolean>) sInput.readObject();
		} catch (ClassNotFoundException e) {
			updateView(e.getMessage());
		} catch (IOException e) {
			updateView(e.getMessage());
		}
		if (cm.getType() != TransferType.LOGIN || cm.getMessage() != true) {
			updateView("User name taken.");
			return false;
		}

		// creates the Thread to listen from the server
		new ListenFromServer().start();

		// success we inform the caller that it worked
		return true;
	}

  public boolean mark(int location) {
    TransferData<Integer> msg = new TransferData<>(TransferType.MARK,
        location, userName, enemyName);
    return sendMessage(msg);
  }

	public boolean invite(String to) {
		TransferData<?> msg = new TransferData<String>(TransferType.INVITE,
				null, userName, to);
		return sendMessage(msg);
	}

	public void doGetUserList() {
		sendMessage(new TransferData<String>(TransferType.WHOISIN, "", null, null));
	}

	public void doLogout() {
		sendMessage(new TransferData<String>(TransferType.LOGOUT, "", null, null));
	}

	// solve this with Observer? ..
	private void updateView(String msg) {
		view.log(msg);
	}

	// send a message to the server
	private boolean sendMessage(TransferData<?> msg) {
		try {
			sOutput.writeObject(msg);
			return true;
		} catch (IOException e) {
			updateView("Exception writing to server: " + e);
	    return false;
		}
	}

  @Override
  public void saveGame() {
    sendMessage(new TransferData<String>(TransferType.SAVE, "", null, null));
  }
  
  @Override
  public void loadGame() {
    sendMessage(new TransferData<String>(TransferType.LOAD, "", null, null));
  }

	public char getMark() {
	  return mark;
	}
	
	// if something goes wrong Close the Input/Output streams and disconnect
	private void disconnect() {
		try {
			if (sInput != null)
				sInput.close();
			if (sOutput != null)
				sOutput.close();
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			updateView(e.getMessage());
		}
		// inform the GUI
		if (view != null)
			view.connectionFailed();
	}
	
	// a class that waits for the message from the server
	private class ListenFromServer extends Thread {

		@SuppressWarnings("unchecked")
		public void run() {
			while (true) {
				try {
					// all results are received here
					Object o = sInput.readObject();
					if (o instanceof TransferData<?>) {
						TransferData<?> cm = (TransferData<?>) o;

						TransferType transType = cm.getType();
		        // Switch on the type of message receive
		        switch (transType) {
		        case WHOISIN:
		          view.setUsers((List<String>) cm.getMessage());
		          break;
		        case INVITE:
		          enemyName = (String) cm.getFrom();
		          mark = (Character) cm.getMessage();
		          view.notifyInvited(mark, enemyName);
		          break;
		        case LOAD:
		          char[][] table = (char[][]) cm.getMessage();
		          if (table != null) {
  		          view.log("Game loaded.");
  		          if (userName.equals(cm.getFrom()))
  		            mark = 'X';
  		          else if (userName.equals(cm.getTo()))
  		            mark = 'O';
  		          int xCount = 0;
  		          int yCount = 0;
  		          for (char[] cv : table)
  		            for (char c : cv)
  		              if (c == 'X')
  		                xCount++;
  		              else if (c == 'O')
  		                yCount++;
  		          if (xCount == yCount) {
  		            if (mark == 'X') {
  		              view.updateWholeTable(table, true, mark);
  		            } else {
  		              view.updateWholeTable(table, false, mark);
  		            }
  		          } else {
  		            if (mark == 'O') {
                    view.updateWholeTable(table, true, mark);
  		            } else {
                    view.updateWholeTable(table, false, mark);
  		            }
  		          }
		          } else {
		            view.log("Loading failed!");
		          }
		          break;
		        case WIN:
		          view.notifyGameEnded((Boolean) cm.getMessage());
		          break;
		        case MARK:
		          if (mark == 'X')
		            view.putMark((Integer) cm.getMessage(), 'O');
		          else
		            view.putMark((Integer) cm.getMessage(), 'X');
		          break;
		        default:
		          view.showMessage("Unknown data type received!");
		        }
					} else
					  throw new XOException(
								"Did not receive TransferData object from stream.");
				} catch (IOException e) {
					updateView("Server has close the connection: " + e);
					if (view != null)
						view.connectionFailed(); // ..
					// break the while loop, finish listening
					break;
				} catch (XOException e2) {
					updateView(e2.getMessage());
				} catch (ClassNotFoundException e3) {
					updateView("Error reading from stream in client: "
							+ e3.getMessage());
				}
			}
		}
	}
}
