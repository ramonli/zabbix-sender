package com.mpos.lottery.te.zabbix.sample.mbean;

public interface LongTransactionMBean {
  public static final String objectName = "com.mpos.lottery.te:type=LongTransctionMBean";

  public String getLongTransaction();

  public void setLongTransaction(String transMsg);
}
