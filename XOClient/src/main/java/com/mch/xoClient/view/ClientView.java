package com.mch.xoClient.view;

import java.util.List;

public interface ClientView {
  
	public void connectionFailed();

	public void setUsers(List<String> users);

  public void putMark(int location, char mark);

  public void showMessage(String error);

  public void log(String msg);

  public void updateWholeTable(char[][] table, boolean active, char mark);

  public void notifyInvited(char mark, String enemy);

  public void notifyGameEnded(Boolean won);
}
