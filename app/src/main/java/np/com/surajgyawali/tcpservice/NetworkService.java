package np.com.surajgyawali.tcpservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static android.os.SystemClock.sleep;
import static android.support.constraint.Constraints.TAG;

public class NetworkService extends Service {

    private String USER_DATA_PREFS_NAME = "UserPrefs";
    private ServerSocket serverSocket;
    private Socket tempClientSocket;

    SharedPreferences sharedPreferences;
    Thread serverThread, udpThread = null;

    String message = "";
    Utilities utilities = new Utilities();


    public NetworkService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        sharedPreferences = getSharedPreferences(USER_DATA_PREFS_NAME, MODE_PRIVATE);
        Toast.makeText(this, "Get state : Starting Server...", Toast.LENGTH_SHORT).show();

        startServerThread();
        return START_STICKY;

    }


    private class UdpThread implements Runnable {

        String userPassword = utilities.getUserPassword(sharedPreferences);
        int    serverPort   = utilities.getUserPort(sharedPreferences);

        public void run() {
            while (true) {
                utilities.broadcastUDP(userPassword,serverPort);
                sleep(5000);

            }
        }

    }

    private class ServerThread implements Runnable {


        public void run() {

            Socket socket;
            int serverPort = utilities.getUserPort(sharedPreferences);
            try {

                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(serverPort)); // <-- now bind it

            } catch (IOException e) {

                e.printStackTrace();

            }

            if (null != serverSocket && !serverSocket.isClosed()) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        Log.i(TAG, "Listening on the port:" + serverPort);

                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                        Log.i(TAG, "Communication thread started");

                    } catch (IOException e) {
                        Log.i(TAG, "Exception" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;
        private InputStream inputStream;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;


            try {

                this.inputStream = this.clientSocket.getInputStream();
                Log.i(TAG, "\n\n\n____________________Getting input stream.________________\n\n\n");

            } catch (IOException e) {

                e.printStackTrace();

            }


        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                byte[] dataBuffer = new byte[1024];
                try {


                    int size = this.inputStream.read(dataBuffer);


                    String messageString = new String(dataBuffer, "UTF-8");

                    String smsChunk = messageString.substring(274, 574).trim();

                    String phoneNumber = messageString.substring(574, 600).trim();


                    messageString = "" + (messageString).trim();
                    Log.i(TAG, "\n\ninputString: " + messageString.trim() + " \n\n\n");
                    Log.i(TAG, "\n\nsmsString: " + smsChunk + " \n\n\n");
                    Log.i(TAG, "\n\nphoneNumber: " + phoneNumber + " \n\n\n");


                    Log.e(TAG, "\n\nSIZE: " + size);

                    if (size <= 0 || "Disconnect".contentEquals(dataBuffer.toString())) {
                        Thread.interrupted();
                        Log.i(TAG, "Client : Disconnected.");
                        break;
                    }

                    utilities.replyToClient("test", tempClientSocket);
                    utilities.sendSMS(smsChunk, phoneNumber, NetworkService.this);


                } catch (IOException e) {

                    Log.i(TAG, "Exception:getting data" + e);
                    e.printStackTrace();
                }

            }
        }

    }

    @Override
    public void onDestroy() {
        stopServerThread();
        super.onDestroy();
    }


    private void stopServerThread() {
        if (null != this.serverThread) {
            utilities.replyToClient(null, tempClientSocket);
            Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "thread status1."+ this.serverThread.isInterrupted());
            this.serverThread.interrupt();
            Log.i(TAG, "thread status2."+ this.serverThread.isInterrupted());

            this.serverThread = null;
            Toast.makeText(this, "server stopped.", Toast.LENGTH_SHORT).show();
        }

        if (null != tempClientSocket) {
        }

        stopUdpThread();

    }

    private void startServerThread() {

        startUdpThread();

        if (null != this.serverThread && this.serverThread.getState() == Thread.State.NEW) {

            this.serverThread.start();

        } else if (null != serverThread && serverSocket.isClosed()) {

            Toast.makeText(this, "closed", Toast.LENGTH_SHORT).show();
            Thread serverThread = new Thread(new NetworkService.ServerThread());
            serverThread.start();

        } else {

            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }


    }


    private void startUdpThread() {

        Log.i(TAG, "startUdpThread called");
        if (null != this.udpThread && this.udpThread.getState() == Thread.State.NEW) {

            this.udpThread.start();

        } else if (null != udpThread) {
            Thread udpThread = new Thread(new NetworkService.UdpThread());
            udpThread.start();
            Log.i(TAG, "udp started with new object creation.");

        } else {

            this.udpThread = new Thread(new UdpThread());
            this.udpThread.start();
        }

    }

    private void stopUdpThread() {

        if (null != this.udpThread) {
            udpThread.interrupt();
            Log.i(TAG,"udpthread interrupt called.");
            udpThread = null;
            Log.i(TAG,"udpthread assigned to null");
        }
    }

}
