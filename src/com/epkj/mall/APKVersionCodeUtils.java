package com.epkj.mall;

import android.content.Context;
import android.content.pm.PackageManager;

public class APKVersionCodeUtils {
    /**
     * ��ȡ��ǰ����apk�İ汾
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //��ȡ����汾�ţ���ӦAndroidManifest.xml��android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * ��ȡ�汾������
     *
     * @param context ������
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

}