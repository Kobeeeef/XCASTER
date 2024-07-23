package org.kobe.xbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static JmDNS jmdns;
    private static InetAddress previousAddress;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <hostname>");
            return;
        }
        String hostname = args[0].trim();

        setupMDNS(hostname);
        startAddressMonitor(hostname);
    }

    private static void setupMDNS(String hostname) {
        try {
            InetAddress addr = Utilities.getLocalInetAddress();
            previousAddress = addr;
            jmdns = JmDNS.create(addr, hostname);
            ServiceInfo serviceInfo = ServiceInfo.create("_xcaster._tcp.local.", "XCASTER - Service Broadcaster", 54321, "XCASTER by XBOT Robotics: Broadcasts hostname over the network.");
            jmdns.registerService(serviceInfo);

            logger.info("mDNS service registered successfully.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutdown hook running, unregistering services.");
                    jmdns.unregisterAllServices();
                    jmdns.close();
                    logger.info("mDNS service unregistered successfully.");
                } catch (Exception e) {
                    logger.error("Failed to unregister mDNS service.", e);
                }
            }));
        } catch (IOException e) {
            logger.error("Failed to set up mDNS.", e);
        }
    }

    private static void startAddressMonitor(String hostname) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    InetAddress currentAddress = Utilities.getLocalInetAddress();
                    if (!currentAddress.equals(previousAddress)) {
                        logger.info("Network address changed from {} to {}. Restarting mDNS.", previousAddress, currentAddress);
                        restartMDNS(hostname, currentAddress);
                    }
                } catch (IOException e) {
                    previousAddress = null;
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000); // Check every millisecond
    }

    private static void restartMDNS(String hostname, InetAddress newAddress) {
        try {
            if (jmdns != null) {
                jmdns.unregisterAllServices();
                jmdns.close();
                logger.info("Previous mDNS service unregistered successfully.");
            }
            previousAddress = newAddress;
            jmdns = JmDNS.create(newAddress, hostname);
            ServiceInfo serviceInfo = ServiceInfo.create("_xcaster._tcp.local.", "XCASTER - Service Broadcaster", 54321, "XCASTER by XBOT Robotics: Broadcasts hostname over the network.");
            jmdns.registerService(serviceInfo);

            logger.info("New mDNS service registered successfully.");
        } catch (IOException e) {
            logger.error("Failed to restart mDNS service.", e);
        }
    }
}
