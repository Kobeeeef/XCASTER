package org.kobe.xbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static JmDNS jmdns;
    private static InetAddress previousAddress;
    private static Timer timer;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <hostname> (username) <password>");
            return;
        }
        String hostname = args[0].trim();
        String username = args.length >= 2 ? args[1].trim() : null;
        String password = args.length >= 3 ? args[2].trim() : null;
        setupMDNS(hostname, username, password);
        startAddressMonitor(hostname);
    }

    private static void setupMDNS(String hostname, String username, String password) {
        try {
            InetAddress addr = Utilities.getLocalInetAddress();
            previousAddress = addr;
            jmdns = JmDNS.create(addr, hostname);
            Map<String, String> props = new HashMap<>();
            props.put("hostname", hostname);
            props.put("username", username);
            props.put("password", password);
            ServiceInfo serviceInfo = ServiceInfo.create("_xcaster._tcp.local.", "XCASTER - Service Broadcaster", 54321, 0,0, props);
            jmdns.registerService(serviceInfo);

            logger.info("mDNS service registered successfully.");
            logger.info("\nUsername: {}\nPassword: {}", username, password);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutdown hook running, unregistering services.");
                    if (jmdns != null) {
                        jmdns.unregisterAllServices();
                        jmdns.close();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    logger.info("Resources released successfully.");
                } catch (Exception e) {
                    logger.error("Failed to release resources.", e);
                }
            }));
        } catch (IOException e) {
            logger.error("Failed to set up mDNS.", e);
            System.exit(1);
        }
    }

    private static void startAddressMonitor(String hostname) {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    InetAddress currentAddress = Utilities.getLocalInetAddress();
                    if (!currentAddress.equals(previousAddress)) {
                        logger.info("Network address changed from {} to {}. Restarting mDNS.", previousAddress, currentAddress);
                        System.exit(0); // Ensure shutdown hook runs
                    }
                } catch (IOException e) {
                    logger.error("Failed to set up mDNS.", e);
                    System.exit(1); // Ensure shutdown hook runs
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000); // Check every second
    }
}
