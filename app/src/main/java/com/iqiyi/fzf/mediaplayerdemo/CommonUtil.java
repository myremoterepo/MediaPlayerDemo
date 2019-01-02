package com.iqiyi.fzf.mediaplayerdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class CommonUtil {
    private static final String TAG = CommonUtil.class.getSimpleName();
    public static String getIp(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (cm != null) {
                networkInfo = cm.getActiveNetworkInfo();
            }
            int type = networkInfo == null ? -1 : networkInfo.getType();
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networkInterface = en.nextElement();
                if (networkInterface != null) {
                    Log.d(TAG, "networkInterface: " + networkInterface.getName() + ", p2p " + networkInterface.isPointToPoint()
                            + ", loopback " + networkInterface.isLoopback() + ", up " + networkInterface.isUp() + ", type " + type);
                }
                if (networkInterface == null || networkInterface.isLoopback() || networkInterface.isPointToPoint()
                        || !networkInterface.isUp()) {
                    continue;
                }
                if (type == ConnectivityManager.TYPE_WIFI && !networkInterface.getName().startsWith("wlan")) {
                    continue;
                } else if (type == ConnectivityManager.TYPE_ETHERNET && !networkInterface.getName().startsWith("eth")) {
                    continue;
                } else if (type != ConnectivityManager.TYPE_WIFI && type != ConnectivityManager.TYPE_ETHERNET) {
                    continue;
                }

                Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
                while (enumeration.hasMoreElements()) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        Log.d(TAG, "from ConnectivityManager get ip address: " + inetAddress.toString());
                        return inetAddress.getHostAddress();
                    }
                }
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                if (ipAddress != 0) {
                    String ip = (ipAddress & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." + ((ipAddress >> 16) & 0xFF) + "." + (ipAddress >> 24 & 0xFF);
                    Log.d(TAG, "from WifiManager get ip address: " + ip);
                    return ip;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "get ip address failed");
        return "Guest";
    }

    public static String getSSID(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null) {
            return "";
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getSSID() == null) {
            return "";
        }
        return wifiInfo.getSSID().replace("\"", "");
    }

    public static int getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if ((info == null) || (!(info.isConnected()))) {
            return -1;
        }

        return info.getType();
    }

}
