package com.floatdesignlabs.android.atom.bluetooth;

import java.io.IOException;

import com.floatdesignlabs.android.atom.AtomActivity;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

class AcceptThread extends Thread {
	private AtomActivity mAtomActivity = new AtomActivity();
    private final BluetoothServerSocket mmServerSocket;
 
    public AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mAtomActivity.getBluetoothAdapter().listenUsingRfcommWithServiceRecord("Atom", mAtomActivity.getMyUUID());
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }
 
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket);
                try {
					mmServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                break;
            }
        }
    }
 
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
