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
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static android.os.SystemClock.sleep;
import static android.support.constraint.Constraints.TAG;

public class NetworkService extends Service {

    private String USER_DATA_PREFS_NAME = "UserPrefs";
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    public boolean isRunning = false;

    SharedPreferences sharedPreferences;
    Thread serverThread=null, udpThread = null;
    CommunicationThread commThread = null;

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
            while (isRunning) {

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

                while (isRunning) {

                    try {

                        socket = serverSocket.accept();


                        Log.i(TAG, "Listening on the port:" + serverPort);
                        commThread = new CommunicationThread(socket);

                        new Thread(commThread).start();
                        Log.i(TAG, "Communication thread started");

                    }


                    catch (InterruptedIOException e){
                        e.printStackTrace();
                        Log.i(TAG,"Thread interrupted");
                        return;
                    }

                    catch (IOException e) {

                        Log.i(TAG, "Exception" + e);
                        e.printStackTrace();
                    }


                }
            }
        }
    }


    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private InputStream inputStream;

        private CommunicationThread(Socket clientSocket) {

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

            while (isRunning) {

                byte[] dataBuffer = new byte[1024];
                byte[] smsData = new byte[300];
                byte[] phoneData = new byte[300];

                try {

                    int size = this.inputStream.read(dataBuffer);

                    if ( !isRunning ) {
                        Thread.interrupted();
                        Log.i(TAG, "Client : Disconnected.");
                        break;
                    }

                    else if (size == 1024){
                        System.arraycopy(dataBuffer, 274, smsData, 0, 300);
                        System.arraycopy(dataBuffer, 574, phoneData, 0, 300);


                        String messageString = new String(dataBuffer, "UTF-8");
                        String smsChunk = new String(smsData,"UTF-8").trim();
                        String phoneNumber = new String(phoneData,"UTF-8").trim();


                        messageString = "" + (messageString).trim();
                        Log.i(TAG, "\n\nSIZE: " + size);
                        Log.i(TAG, "\n\ninputString: " + messageString.trim() + " \n\n\n");
                        Log.i(TAG, "\n\nsmsString: " + smsChunk + " \n\n\n");
                        Log.i(TAG, "\n\nphoneNumber: " + phoneNumber + " \n\n\n");


//                        utilities.replyToClient("12", tempClientSocket);
//                        utilities.sendSMS(smsChunk, phoneNumber, NetworkService.this);
                            //this method can be used to sendSMS to client
                        //i was stuck when client couldn't receive any messages from server's end
                    }

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

        Log.i(TAG, "running" + isRunning);

        isRunning = false;

        if (null != this.serverThread) {

            utilities.replyToClient(null, this.tempClientSocket);

//            Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
//
            Log.i(TAG, "thread Interrupted: "+ this.serverThread.isInterrupted());

            this.serverThread.interrupt();

            Log.i(TAG, "thread Interrupted: "+ this.serverThread.isInterrupted());

            Log.i(TAG, "running" + isRunning);

            if (null != this.serverSocket) {

                try {

                    this.serverSocket.close();
                    this.serverSocket = null;

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            Toast.makeText(this, "server stopped.", Toast.LENGTH_SHORT).show();
        }
        if (null != this.commThread) {

                this.commThread = null;

                if (null != this.tempClientSocket) {

                try {

                    this.tempClientSocket.close();
                    this.tempClientSocket = null;


                } catch (IOException e) {

                    e.printStackTrace();
                }

            }

        }


    }

    private void startServerThread() {

        isRunning = true;

        startUdpThread();


        if (null != this.serverThread && this.serverThread.getState() == Thread.State.NEW) {

            if (!this.udpThread.isAlive()) {

                this.serverThread.start();
            }

        } else {

            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }
//
//        if (this.serverThread == null){
//
//            this.serverThread = new Thread(new UdpThread());
//            this.serverThread.start();
//
//        }
//
//        else if ( this.serverThread.getState() == Thread.State.NEW) {
//
//                this.serverThread.start();
//        }

    }


    private void startUdpThread() {

        Log.i(TAG, "startUdpThread called");

        if (this.udpThread == null){

            this.udpThread = new Thread(new UdpThread());
            this.udpThread.start();

        }

        else if ( this.udpThread.getState() == Thread.State.NEW) {

            if (!this.udpThread.isAlive()) {

                this.udpThread.start();
            }
        }

    }

    private void stopUdpThread() {

        if (null != this.udpThread) {

            Log.i(TAG, "udp thread Interrupted: "+ this.udpThread.isInterrupted());

            this.udpThread.interrupt();

            Log.i(TAG, "udp thread Interrupted: "+ this.udpThread.isInterrupted());

            this.udpThread = null;

        }
    }

    public void forwardSms(String textMessage){
        Log.i(TAG,"forwardSms called with  parameter : " + textMessage);

        if (tempClientSocket != null) {
            utilities.replyToClient(textMessage, tempClientSocket);
        }
    }

}
