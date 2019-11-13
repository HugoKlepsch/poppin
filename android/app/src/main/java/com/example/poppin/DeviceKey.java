package com.example.poppin;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class DeviceKey {
    final static private String accountKeyStoragePath = "account_id";
    static private byte[] deviceKey = null;

    /**
     *
     * @return
     */
    static private byte[] generateAccountCredentials(Context context) {
        byte[] accountId;
        Random r = new Random();
        accountId = new byte[256];

        r.nextBytes(accountId);

        try {
            File f = new File(context.getFilesDir(), accountKeyStoragePath);
            FileOutputStream fOut = new FileOutputStream(f);
            fOut.write(accountId);
            fOut.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return accountId;
    }

    public static byte[] getDeviceKey(Context context) {
        if (deviceKey == null) {
            loadAccountCredentials(context);
        }
        return deviceKey;
    }


    /**
     *
     */
    private static void loadAccountCredentials(Context context) {

        FileInputStream fIn;
        deviceKey = new byte[256];

        try {
            byte[] bytes = new byte[256];
            File f = new File(context.getFilesDir(), accountKeyStoragePath);
            fIn = new FileInputStream(f);
            fIn.read(bytes);
            System.arraycopy(bytes, 0, deviceKey, 0, 256);

        } catch (FileNotFoundException e) {
            byte[] bytes;
            bytes = generateAccountCredentials(context);
            System.arraycopy(bytes, 0, deviceKey, 0, 256);

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
