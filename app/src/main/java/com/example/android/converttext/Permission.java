package com.example.android.converttext;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class Permission {
    public static interface ResultListener{
        public void onResult();
    }
    private ResultListener mListener;
    private Activity mActivity;

    public void setOnResultListener(ResultListener listener){
        mListener = listener;
    }
    public boolean requestPermissions(Activity context){
        mActivity = context;

        try {
            List<String > list = new ArrayList<String>();
            PackageManager pm = context.getPackageManager();

            PackageInfo info = pm.getPackageInfo(context.getPackageName(),PackageManager.GET_PERMISSIONS);

            String[] permissions = info.requestedPermissions;
            for(String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    list.add(permission);
            }

            if(list.size() > 0) {
                ActivityCompat.requestPermissions(context,list.toArray(new String[list.size()]) , 123);
                return false;
            }
            mListener.onResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        requestPermissions(mActivity);

    }
}

