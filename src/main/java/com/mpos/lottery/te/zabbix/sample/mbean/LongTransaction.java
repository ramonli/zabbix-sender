package com.mpos.lottery.te.zabbix.sample.mbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LongTransaction implements LongTransactionMBean {
  private Log logger = LogFactory.getLog(LongTransaction.class);
  private final static String NULL_MSG = "IT IS NULL";
  private String transMsg;

  @Override
  public String getLongTransaction() {
    String resp = this.transMsg;
    // reset message to NULL
    this.transMsg = NULL_MSG;
    if (logger.isDebugEnabled()) {
      logger.debug("Reset message to " + NULL_MSG);
    }

    return resp;
  }

  @Override
  public void setLongTransaction(String transMsg) {
    this.transMsg = transMsg;
  }

}
