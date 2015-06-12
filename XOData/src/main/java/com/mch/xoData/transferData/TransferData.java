package com.mch.xoData.transferData;

import java.io.Serializable;

// Objects from this class will be passed around on the socket
public class TransferData<T> implements Serializable {
  protected static final long serialVersionUID = 1112122200L;

  // The message fields (can be null also)
  private TransferType type;
  private T message;
  private String from;
  private String to;

  public TransferData(TransferType type, T message, String from, String to) {
    this.type = type;
    this.message = message;
    this.from = from;
    this.to = to;
  }

  public TransferType getType() {
    return type;
  }

  public T getMessage() {
    return message;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }
}
