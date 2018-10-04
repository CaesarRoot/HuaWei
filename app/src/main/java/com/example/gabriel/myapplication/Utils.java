package com.example.gabriel.myapplication;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by h00447539 on 2018/6/15.
 */

public final class Utils {
    public static boolean requestPermission(Activity context, String permissionText) {
        if (ContextCompat.checkSelfPermission(context, permissionText) != PackageManager.PERMISSION_GRANTED) {
            //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
            ActivityCompat.requestPermissions(context, new String[]{ permissionText }, 0x11);
            return false;
        }
        return true;
    }


    // 判断本程序是否有相机权限
    public static boolean requestCameraPermission(Activity context) {
        return requestPermission(context, Manifest.permission.CAMERA);
    }

    // 判断本程序是否有存储权限
    public static boolean requestStoragePermission(Activity context) {
        return requestPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    // 判断本程序是否有录音权限
    public static boolean requestMicPermission(Activity context) {
        return requestPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    // 判断本程序是否有存储权限
    public static boolean requestReadStoragePermission(Activity context) {
        return requestPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void writeNewName(List<String> name, String fstPath, String fileName) {
        File file = null;
        File dirFile = null;
        PrintStream ps = null;
        if (name != null) {
            Log.d("MainActivity", "writeNewName: ");
            try {
                dirFile = new File(fstPath);
                Log.d("MainActivity", "writeNewName: " + fstPath + "/" + fileName);
                file = new File(fstPath + "/" + fileName);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }

                ps = new PrintStream(new FileOutputStream(file));
                for (int i = 0; i < name.size(); i++) {
                    ps.println(name.get(i));
                }
                ps.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != ps) {
                    ps.close();
                }
            }
        }

    }

    public static void writeNewName(ResultInfo info, String fstPath, String fileName) {
        File file = null;
        BufferedWriter bufferedWriter = null;
        File dirFile = null;
        PrintStream ps = null;
        FileWriter fileWriter = null;
        if (info != null) {
            try {
                dirFile = new File(fstPath);
                file = new File(fstPath + fileName);
                Log.d("MainActivity", "filepath: " + fstPath + fileName + "======" + file.getName());
                if (!file.exists()) {
                    file.createNewFile();
                }

                fileWriter = new FileWriter(file, true);
                bufferedWriter = new BufferedWriter(fileWriter);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }

                bufferedWriter.write("wav:" + info.getWavName() + "\n");
                bufferedWriter.write("lab:" + info.getLable() + "\n");
                bufferedWriter.write("rec:" + info.getRec() + "\n");
                bufferedWriter.write("correct:" + info.getCorrect() + "\n");
                bufferedWriter.write("wavtime:" + info.getWavTimeLeng() + "\n");
                bufferedWriter.write("rectime:" + info.getRecTime() + "\n\n");
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != ps) {
                    ps.close();
                }
            }
        }

    }

    public static void writeNewName(String info, String fstPath, String fileName) {
        File file = null;
        BufferedWriter bufferedWriter = null;
        File dirFile = null;
        PrintStream ps = null;
        FileWriter fileWriter = null;
        if (info != null) {
            try {
                dirFile = new File(fstPath);
                file = new File(fstPath + fileName);
                Log.d("MainActivity", "filepath: " + fstPath + fileName + "======" + file.getName());
                if (!file.exists()) {
                    file.createNewFile();
                }

                fileWriter = new FileWriter(file, true);
                bufferedWriter = new BufferedWriter(fileWriter);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }

                bufferedWriter.write(info + "\n");
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != ps) {
                    ps.close();
                }
            }
        }

    }
}
