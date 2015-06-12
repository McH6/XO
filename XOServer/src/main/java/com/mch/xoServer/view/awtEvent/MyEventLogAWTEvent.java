package com.mch.xoServer.view.awtEvent;

import java.awt.AWTEvent;

public class MyEventLogAWTEvent extends AWTEvent {
  private static final long serialVersionUID = 1L;

  public static final int EVENT_ID = AWTEvent.RESERVED_ID_MAX + 1;
  private String str;

  public MyEventLogAWTEvent(Object target, String str) {
    super(target, EVENT_ID);
    this.str = str;
  }

  public String getStr() {
    return (str);
  }
}
