package com.mpos.lottery.te.zabbix.sample.mbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.management.ManagementFactory;
import java.util.UUID;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class ZabbixMBeanServer {
  private static Log logger = LogFactory.getLog(ZabbixMBeanServer.class);
  private static int sleepSeconds = 15;

  public static void main(String[] args) throws Exception {
    ZabbixMBeanServer server = new ZabbixMBeanServer();
    server.registerMBeans();
    for (;;) {
      try {
        Thread.currentThread().sleep(sleepSeconds * 1000);
        logger.info("Sleep " + sleepSeconds + " seconds");
      } catch (Exception e) {
      }

      // get current default MBeanServer
      String transMsg = "Long Transaction[" + UUID.randomUUID() + "]";
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectInstance o = mbs.getObjectInstance(new ObjectName(LongTransactionMBean.objectName));
      // :(, in my understanding the attribute name should be 'longTransactin', however....
      mbs.setAttribute(new ObjectName(LongTransactionMBean.objectName), new Attribute(
              "LongTransaction", transMsg));
    }
  }

  protected void registerMBeans() {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName oName = new ObjectName(LongTransactionMBean.objectName);
      LongTransactionMBean mbean = new LongTransaction();
      if (!mbs.isRegistered(oName))
        mbs.registerMBean(mbean, oName);
      if (logger.isDebugEnabled())
        logger.debug("Register MBean(" + mbean + ") with name(" + oName + ") successfully.");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
