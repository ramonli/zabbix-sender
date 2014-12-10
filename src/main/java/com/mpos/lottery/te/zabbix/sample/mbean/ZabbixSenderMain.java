package com.mpos.lottery.te.zabbix.sample.mbean;

import com.mpos.lottery.te.zabbix.ZabbixSender;

import java.io.IOException;
import java.util.UUID;

public class ZabbixSenderMain {

    public static void main(String[] args) throws IOException {
        ZabbixSender sender = new ZabbixSender("192.168.2.158", 10051);
        sender.asyncSend("logdog", "long_transaction", "[LONG]The UUID:" + UUID.randomUUID());
    }
}
