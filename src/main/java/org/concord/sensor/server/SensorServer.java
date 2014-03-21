package org.concord.sensor.server;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

public class SensorServer extends JFrame
{
	private static final long serialVersionUID = 1L;
	static final String MAC_EXIT_TEXT = "To exit, click this icon and select 'Exit'.";
	static final String WIN_EXIT_TEXT = "To exit, right-click this icon and select 'Exit'.";

	public static void main( String[] args ) throws Exception
    {
		BasicConfigurator.configure();
		Logger.getLogger("org.concord.sensor").setLevel(Level.ERROR);

    	SensorHandler handler = new SensorHandler();
    	// 11180 seems unassigned, high enough to not be privileged
    	// Attach only to localhost (for now), to avoid any firewall popups
    	Server server = new Server(new InetSocketAddress(InetAddress.getByName(null), 11180));
        server.setHandler(handler);
        server.start();
        
        InfoFrame infoFrame = new InfoFrame(handler);
        
        // Minimize to tray (Windows)/title bar (OS X) after starting
    	String exitText = isMac() ? MAC_EXIT_TEXT : WIN_EXIT_TEXT;
    	infoFrame.iconify("The sensor server is running in the background. " + exitText, TrayIcon.MessageType.INFO);

        // TODO Add status, sensor info to tray/title bar menu or tooltip?
    }
	
	static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac");
	}
}

class InfoFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private SensorHandler handler;
	private MenuItem showItem;
	private MenuItem hideItem;

	private TrayIcon trayIcon;

	private boolean locallyDispatchedClose = false;

	public InfoFrame(SensorHandler handler) {
		this.handler = handler;

		setVisible(false);
		setupBehavior();
		setupTray();
		
		setupContent();
	}
	
	public void deiconify() {
		setExtendedState(JFrame.NORMAL);
    	setVisible(true);
		toggleMenuItems(true);
	}

	public void iconify() {
		locallyDispatchedClose  = true;
		toggleMenuItems(false);
    	dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	public void iconify(String message, TrayIcon.MessageType messageType) {
		iconify();
		
		trayIcon.displayMessage(null, message, messageType);
	}

	private void setupContent() {
		setBackground(Color.WHITE);
		setTitle("Sensor Server");

		final JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		
		JLabel statusLabel = new JLabel("Status: ");
		JLabel interfaceLabel = new JLabel("Connected interface: ");
		JLabel unitsLabel = new JLabel("Units: ");
		JLabel readingLabel = new JLabel("Current reading: ");
		
		final JLabel statusValue = new JLabel(handler.getCurrentState());
		final JLabel interfaceValue = new JLabel("");
		final JLabel unitsValue = new JLabel("");
		final JLabel readingValue = new JLabel("0");

		panel.add(statusLabel);
		panel.add(statusValue);
		
		panel.add(interfaceLabel);
		panel.add(interfaceValue);
		
		panel.add(unitsLabel);
		panel.add(unitsValue);
		
		panel.add(readingLabel);
		panel.add(readingValue);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		pack();
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!isVisible()) { return; }
				// Update the status, interface, last polled values, units
				interfaceValue.setText(handler.getCurrentInterface());
				
				statusValue.setText(handler.getCurrentState());
				
				String[] units = handler.getUnits();
				String unit = "";
				if (units.length > 1) {
					unit = units[1];
				}
				unitsValue.setText(unit);
				
				float[] lastPolledData = handler.getLastPolledData();
				String reading = "0";
				if (lastPolledData.length > 1) {
					reading = "" + lastPolledData[1];
				}
				readingValue.setText(reading);
				
				panel.revalidate();
				panel.repaint();
			}
		}, 0, 500);
	}

	private void setupTray() {
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/cc-lightbulb_64x64.png"));
			PopupMenu popup = new PopupMenu();
			trayIcon = new TrayIcon(image, "Sensor Server", popup);
			trayIcon.setImageAutoSize(true);

			showItem = new MenuItem("Show");
			hideItem = new MenuItem("Hide");
			
			showItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					deiconify();
				}
			});
			hideItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					iconify();
				}
			});
			
			popup.add(showItem);
			popup.add(hideItem);
			
			hideItem.setEnabled(false);
			
			MenuItem exitItem = new MenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tray.remove(trayIcon);
					System.exit(0);
				}
			});
			popup.add(exitItem);
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("Can't add to tray");
			}
		} else {
			System.err.println("Tray unavailable");
		}
	}

	private void setupBehavior() {
        setAlwaysOnTop(true);
        
        // Minimize if the window frame close button is hit
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        addWindowStateListener(new WindowAdapter() {
        	@Override
        	public void windowStateChanged(WindowEvent e) {
        		if (e.getNewState() == JFrame.ICONIFIED) {
        			iconify(); // FIXME This doesn't hide the dock window on OS X...
        		} else if (e.getNewState() == JFrame.NORMAL) {
        			deiconify();
        		}
        	}
        });
        addWindowListener(new WindowAdapter() {
        	@Override
            public void windowClosing(WindowEvent e) {
        		if (!locallyDispatchedClose) {
        			toggleMenuItems(false);
        	    	String exitText = SensorServer.isMac() ? SensorServer.MAC_EXIT_TEXT : SensorServer.WIN_EXIT_TEXT;
        			trayIcon.displayMessage(null, "The sensor server is still running. " + exitText, TrayIcon.MessageType.INFO);
        		}
        		locallyDispatchedClose = false;
            }
        });
	}
	
	private void toggleMenuItems(boolean isFrameShowing) {
		showItem.setEnabled(!isFrameShowing);
		hideItem.setEnabled(isFrameShowing);
	}
}
