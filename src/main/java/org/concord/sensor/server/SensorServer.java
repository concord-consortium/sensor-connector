package org.concord.sensor.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;

/**
 * Hello world!
 *
 */
public class SensorServer 
{
    public static void main( String[] args ) throws Exception
    {
    	// 11180 seems unassigned, high enough to not be privileged
    	// Attach only to localhost (for now), to avoid any firewall popups
    	Server server = new Server(new InetSocketAddress(InetAddress.getByName(null), 11180));
        server.setHandler(new SensorHandler());
        server.start();
        server.join();
    }
}
