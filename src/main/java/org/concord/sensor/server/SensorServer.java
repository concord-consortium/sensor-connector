package org.concord.sensor.server;

import java.awt.AWTException;
import java.awt.BorderLayout;
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

import org.eclipse.jetty.server.Server;

public class SensorServer extends JFrame
{
	private static final long serialVersionUID = 1L;

	public static void main( String[] args ) throws Exception
    {
    	SensorHandler handler = new SensorHandler();
    	// 11180 seems unassigned, high enough to not be privileged
    	// Attach only to localhost (for now), to avoid any firewall popups
    	Server server = new Server(new InetSocketAddress(InetAddress.getByName(null), 11180));
        server.setHandler(handler);
        server.start();
        
        InfoFrame infoFrame = new InfoFrame(handler);
        
        // Minimize to tray (Windows)/title bar (OS X) after starting
        infoFrame.iconify("Sensor Server", "The sensor server is running in the background. To exit, right-click this icon and select 'Exit'.", TrayIcon.MessageType.INFO);

        // TODO Add status, sensor info to tray/title bar menu or tooltip?
    }
    
    
}

class InfoFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private SensorHandler handler;
	private MenuItem showItem;
	private MenuItem hideItem;

	private TrayIcon trayIcon;

	public InfoFrame(SensorHandler handler) {
		this.handler = handler;

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
    	setExtendedState(JFrame.ICONIFIED);
    	setVisible(false);
		toggleMenuItems(false);
	}
	
	public void iconify(String title, String message, TrayIcon.MessageType messageType) {
		iconify();
		
	}

	private void setupContent() {
		final JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		
		JLabel statusLabel = new JLabel("Status: ");
		JLabel unitsLabel = new JLabel("Units: ");
		JLabel readingLabel = new JLabel("Current reading: ");
		
		final JLabel statusValue = new JLabel(handler.getCurrentState());
		final JLabel unitsValue = new JLabel("");
		final JLabel readingValue = new JLabel("0");

		panel.add(statusLabel);
		panel.add(statusValue);
		
		panel.add(unitsLabel);
		panel.add(unitsValue);
		
		panel.add(readingLabel);
		panel.add(readingValue);
		
		setTitle("Sensor Server");
		getContentPane().add(panel, BorderLayout.CENTER);
		pack();
		setVisible(true);
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Update the status, last polled values, units
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
			}
		}, 0, 500);
	}

	private void setupTray() {
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/cc-lightbulb_16x16.png"));
			PopupMenu popup = new PopupMenu();
			trayIcon = new TrayIcon(image, "Sensor Server", popup);

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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	@Override
            public void windowClosing(WindowEvent e) {
        		iconify("Sensor Server", "The sensor server is still running. Right-click and select Exit to shut it down entirely", TrayIcon.MessageType.INFO);
            }
            @Override
            public void windowOpened(WindowEvent e) {
            	deiconify();
            }
            @Override
            public void windowIconified(WindowEvent e) {
            	iconify();
            }
        });
	}
	
	private void toggleMenuItems(boolean isFrameShowing) {
		showItem.setEnabled(!isFrameShowing);
		hideItem.setEnabled(isFrameShowing);
	}
}
