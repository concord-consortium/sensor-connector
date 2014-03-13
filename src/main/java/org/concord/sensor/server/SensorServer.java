package org.concord.sensor.server;

import org.eclipse.jetty.server.Server;

/**
 * Hello world!
 *
 */
public class SensorServer 
{
    public static void main( String[] args ) throws Exception
    {
    	Server server = new Server(8088);
        server.setHandler(new SensorHandler());
        server.start();
        server.join();
    }
}
