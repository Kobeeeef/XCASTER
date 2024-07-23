package org.kobe.xbot;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java Main <hostname>");
            return;
        }
        String hostname = args[0].trim();
        while (true) {
            try {
                InetAddress addr = Utilities.getLocalInetAddress();

                try (JmDNS jmdns = JmDNS.create(addr, hostname)) {

                    ServiceInfo serviceInfo = ServiceInfo.create("_xcaster._tcp.local.", "XCASTER - Service Broadcaster", 54321, "XCASTER by XBOT Robotics: Broadcasts hostname over the network.");
                    jmdns.registerService(serviceInfo);

                    System.out.println("mDNS service registered successfully.");
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            jmdns.unregisterAllServices();
                            System.out.println("mDNS service unregistered successfully.");
                        } catch (Exception e) {
                            System.err.println("Failed to unregister mDNS service.");
                            e.printStackTrace();
                        }
                    }));
                    // Keep the service alive
                    synchronized (Main.class) {
                        Main.class.wait();
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to set up mDNS. Retrying in 3 seconds...");
                e.printStackTrace();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }
}