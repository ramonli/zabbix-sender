package com.mpos.lottery.te.zabbix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
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

    public static void main(String[] args) throws IOException {
        ZabbixSender sender = new ZabbixSender("192.168.2.158", 10051);
        sender.asyncSend("logdog", "long_transaction", "[LONG]The UUID:" + UUID.randomUUID());
    }

    public ZabbixSender(String zabbixHost, int zabbixPort) {
        this.zabbixHost = zabbixHost;
        this.zabbixPort = zabbixPort;
    }

    /**
     * Send value to supplied item in asynchronized mode. The sending processed in a independent thread.
     * 
     * @param host
     *            The host which defined on Zabbix server.
     * @param itemKey
     *            The key of item.
     * @param value
     *            The value of item.
     * @throws IOException
     *             if fail to send message over network.
     */
    public void asyncSend(final String host, final String itemKey, final String value) {
        ExecutorService exeService = Executors.newFixedThreadPool(1);
        exeService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    send(host, itemKey, value);
                } catch (IOException e) {
                    // simply ignore the error message.
                    logger.warn(e.getMessage());
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
     * @param value
     *            The value of item.
     * @throws IOException
     *             if fail to send message over network.
     */
    private void send(String host, String itemKey, String value) throws IOException {
        Socket socket = new Socket(zabbixHost, zabbixPort);
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        String json = this.buildJSonString(host, itemKey, value);
        this.writeMessage(bos, json.getBytes());
        bos.close();

        if (logger.isDebugEnabled()) {
            logger.debug("Send value successfully");
        }
    }

    private String buildJSonString(String host, String item, String value) {
        String json = "{" + "\"request\":\"sender data\",\n" + "\"data\":[\n" + "{\n" + "\"host\":\"" + host + "\",\n"
                        + "\"key\":\"" + item + "\",\n" + "\"value\":\"" + value.replace("\\", "\\\\") + "\"}]}\n";
        if (logger.isDebugEnabled()) {
            logger.debug("[" + this.zabbixHost + ":" + this.zabbixPort + "] " + json);
        }
        return json;
    }

    private void writeMessage(OutputStream out, byte[] data) throws IOException {
        int length = data.length;

        out.write(new byte[] { 'Z', 'B', 'X', 'D', '\1', (byte) (length & 0xFF), (byte) ((length >> 8) & 0x00FF),
            (byte) ((length >> 16) & 0x0000FF), (byte) ((length >> 24) & 0x000000FF), '\0', '\0', '\0', '\0' });

        out.write(data);
    }
}
