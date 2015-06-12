package com.mch.xoData.transferData;

public enum TransferType {
  // the Standard message types
  LOGIN (0),
  LOGOUT (1),
  WHOISIN (2),
  INVITE (3),
  MARK (4),
  WIN (5),
  SAVE (6),
  LOAD (7);

  private final int code;
  
  private TransferType(int code) {
    this.code = code;
  }
  
  public int getCode() {
    return code;
  }
}
