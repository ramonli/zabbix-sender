package com.mpos.lottery.te.zabbix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The java implementation of Zabbix sender. Refer to https://www.zabbix.org/wiki/Docs/protocols/zabbix_sender/2.0
 * 
 * @author Ramon
 */
public class ZabbixSender {
    private static Log logger = LogFactory.getLog(ZabbixSender.class);
    private String zabbixHost;
    private int zabbixPort;

    public ZabbixSender(String zabbixHost, int zabbixPort) {
        this.zabbixHost = zabbixHost;
        this.zabbixPort = zabbixPort;
    }

    /**
     * Send value to supplied item in asynchronized mode.
     * 
     * @param host
     *            The host which defined on Zabbix server.
     * @param itemKey
     *            The key of item.
     * @param moduleName
     *            The name of module which issues the message.
     * @param value
     *            The value of item.
     * @throws IOException
     *             if fail to send message over network.
     */
    public void asyncSend(final String host, final String itemKey, final String moduleName, final String value) {
        ExecutorService exeService = Executors.newFixedThreadPool(1);
        exeService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    send(host, itemKey, moduleName, value);
                } catch (IOException e) {
                    // simply ignore the error message.
                    logger.warn("[Zabbix-" + ZabbixSender.this.zabbixHost + ":" + ZabbixSender.this.zabbixPort + "]"
                                    + e.getMessage());
                }
            }
        });
        exeService.shutdown();
    }

    /**
     * Send value to supplied item in synchronized mode.
     * 
     * @param host
     *            The host which defined on Zabbix server.
     * @param itemKey
     *            The key of item.
     * @param moduleName
     *            The name of module which issue the monitoring message.
     * @param value
     *            The value of item.
     * @throws IOException
     *             if fail to send message over network.
     */
    private void send(String host, String itemKey, String moduleName, String value) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(zabbixHost, zabbixPort);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            String message = "[WARN][" + moduleName + "]" + value;
            String json = this.buildJSonString(host, itemKey, message);
            this.writeMessage(bos, json.getBytes());
            bos.close();

            if (logger.isDebugEnabled()) {
                logger.debug("Successfully send to zabbix[" + this.zabbixHost + ":" + this.zabbixPort + "] " + json);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    private String buildJSonString(String host, String item, String value) {
        return "{" + "\"request\":\"sender data\",\n" + "\"data\":[\n" + "{\n" + "\"host\":\"" + host + "\",\n"
                        + "\"key\":\"" + item + "\",\n" + "\"value\":\"" + value.replace("\\", "\\\\") + "\"}]}\n";
    }

    private void writeMessage(OutputStream out, byte[] data) throws IOException {
        int length = data.length;

        out.write(new byte[] { 'Z', 'B', 'X', 'D', '\1', (byte) (length & 0xFF), (byte) ((length >> 8) & 0x00FF),
            (byte) ((length >> 16) & 0x0000FF), (byte) ((length >> 24) & 0x000000FF), '\0', '\0', '\0', '\0' });

        out.write(data);
    }
}
