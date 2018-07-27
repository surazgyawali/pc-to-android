package np.com.surajgyawali.tcpservice;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;
import static np.com.surajgyawali.tcpservice.HomeActivity.USER_DATA_PREFS_NAME;


public class Utilities  {

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */


    public String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {

        } // for now eat exceptions
        return "";
    }


    public Integer getUserPort(SharedPreferences sharedPreferences) {

        Integer port = sharedPreferences.getInt("port", 5000);
        Log.i("getUserPort():", port.toString());
        return port;
    }


    public String getUserPassword(SharedPreferences sharedPreferences) {

        String password = sharedPreferences.getString("password", "My password");
        return password;
    }

    /**
     * Get connected access point name
     *
     * @return name or empty string
     */
    public String getAccessPointName() {
        return "TO DO";

    }

    public void sendSMS(String smsMessage, String phoneNumber, Context context) {


        SmsManager smsManager = SmsManager.getDefault();

        if (smsMessage.length() <= 0) {

            Log.i(TAG, "Empty Message.");
            return;
        }

        if (phoneNumber.length() <= 9) {
            Log.i(TAG, "Invalid Phone Number.");
            return;
        }

        try {

            smsManager.sendTextMessage(phoneNumber, null, smsMessage, null, null);
            Log.e("sendSMS", "Sms Sent." + smsMessage + "\"");

        } catch (Exception e) {

            Log.e("sendSMS", "Sms not sent try again." + smsMessage + "\"");
            e.printStackTrace();
        }
    }


    public void replyToClient(String message, Socket tempClientSocket) {
        byte[] dataBuffer = new byte[1024];
        try {

            if (null != tempClientSocket) {
                if (null != message) {
//                    dataBuffer = dataBuffer + message.getBytes();
                    System.arraycopy(message.getBytes(), 0, dataBuffer, 0, message.length());

                } else {
                    dataBuffer = null;
                }

                OutputStream outputStream;

                outputStream = tempClientSocket.getOutputStream();
                outputStream.write(dataBuffer);
                Log.i(TAG, "Reply to client.:" + new String(dataBuffer, "UTF-8").trim());
                Log.i(TAG, "Reply size:" + dataBuffer.length);
            }

        } catch (Exception e) {
            Log.i(TAG, "Error Replying to client");
            e.printStackTrace();
        }
    }

    public void broadcastUDP(String messageString, int serverPort) {

        Log.i(TAG, "broadcastUDP called.");

        try {

            DatagramSocket datagramSocket = new DatagramSocket();

            InetAddress localBroadcastAddress = InetAddress.getByName("255.255.255.255");//my broadcast ip
            int messageLength = messageString.length();
            byte[] messageStringBytes = messageString.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(messageStringBytes, messageLength, localBroadcastAddress, serverPort);

            datagramSocket.send(datagramPacket);
            datagramSocket.close();
            Log.i(TAG, "Udp broadcast success!! Message: " + messageString + " length: " + messageLength);
        } catch (Exception e) {

            Log.i(TAG, "error  " + e.toString());

        }

    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public void checkForSmsPermission(Context context, Activity activity) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, context.getString(R.string.permission_not_granted));
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        } else {

            // Permission already granted. Enable the message button.
            Log.d(TAG, context.getString(R.string.permission_granted));

        }
    }

}
