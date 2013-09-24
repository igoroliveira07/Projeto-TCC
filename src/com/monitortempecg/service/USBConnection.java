package com.monitortempecg.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class USBConnection extends Thread {

	private static int baud_rate = 115200;
	private static UsbSerialDriver sDriver = null;

	public interface ConnectionListner {
		public void onReceiveMessage(Packet receivedPacket);

		public void onDisconnect();
	}

	protected Context parentContext;
	private ConnectionListner listner;

	protected Parser parser = new Parser();
	protected byte[] readData = new byte[4096];
	protected int iavailable, i;
	protected boolean connected = true;


	public USBConnection(Context parentContext) {
		this.parentContext = parentContext;
		this.listner = (ConnectionListner) parentContext;

	}

	public void run() {
		super.run();
		try {
			openConnection();
			while (connected) {
				readDataBlock();
				handleData();
			}
			closeConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		listner.onDisconnect();
	}

	private void handleData() throws IOException {
		if (iavailable < 1) {
			return;
		}
		for (i = 0; i < iavailable; i++) {
			Packet receivedPacket = parser.parse(readData[i] & 0x00ff);
			if (receivedPacket != null) {
				listner.onReceiveMessage(receivedPacket);
			}
		}

	}

	public void disconnect() {
		connected = false;
	}

	protected void openConnection() throws UnknownHostException, IOException {
		openCOM();
	}

	protected void readDataBlock()  {
		//Read data from driver. This call will return upto readData.length bytes.
		//If no data is received it will timeout after 200ms (as set by parameter 2)
		try {
			iavailable = sDriver.read(readData,200);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (iavailable == 0) iavailable = -1;
		//Log.d("USB", "Bytes read" + iavailable);
	}

	protected void sendBuffer(byte[] buffer) {
		//Write data to driver. This call should write buffer.length bytes 
		//if data cant be sent , then it will timeout in 500ms (as set by parameter 2)
		if (connected && sDriver != null) {
			try{
				sDriver.write(buffer,500);
			} catch (IOException e) {
				Log.e("USB", "Error Sending: " + e.getMessage(), e);
			}
		}
	}

	protected void closeConnection() throws IOException {
	  if (sDriver != null) {
	     try {
	          sDriver.close();
	     } catch (IOException e) {
	         // Ignore.
	     }
	     sDriver = null;
	  }
	}

	private void openCOM() throws IOException {
		// Get UsbManager from Android.
		UsbManager manager = (UsbManager) parentContext.getSystemService(Context.USB_SERVICE);
	
		// Find the first available driver.
		//**TODO: We should probably step through all available USB Devices
		//...but its unlikely to happen on a Phone/tablet running DroidPlanner.
		sDriver = UsbSerialProber.findFirstDevice(manager);
	
		if (sDriver == null) {
			Log.d("USB", "No Devices found");	
			throw new IOException();
		}
		else
		{	
		Log.d("USB", "Opening using Baud rate " + baud_rate);
	        try {
	                sDriver.open();
	                sDriver.setParameters(baud_rate, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
	            } catch (IOException e) {
	                Log.e("USB", "Error setting up device: " + e.getMessage(), e);
	                try {
	                    sDriver.close();
	                } catch (IOException e2) {
	                    // Ignore.
	                }
	                sDriver = null;
	                return;
	        }
		}
	}

	public void sendPacket(Packet packet) {
		// TODO Auto-generated method stub
		
	}

}
