package com.hxgc;

import android.Manifest;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dcloud.feature.sdk.DCUniMPSDK;

public class ToolModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;
    public ToolModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "ToolModule";
    }

    @ReactMethod
    public void navigate(String params) {
        try{
            Log.d("chh","params:"+params);
            DCUniMPSDK.getInstance().startApp(reactContext,"__UNI__02EBE11",params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void write(String key, ReadableMap obj) {
        try{
            Log.d("chh","key:"+key +" obj:"+obj.toString());
            if(key!=null&&obj!=null){
                String s = GsonUtils.toJson(obj.toHashMap());
                Log.d("chh","key:"+key +" s:"+s);
                reactContext.sendBroadcast(new Intent("action.user.data")
                        .putExtra("key",key)
                        .putExtra("value",s));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void getLocation(Promise promise) {
        Log.d("chh","getLocation");
        WritableNativeMap map = new WritableNativeMap();
        if(PermissionUtils.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)){
            getLocationOption(map,promise);
        }else{
            requestPermission(map,promise);
        }
    }

    //????????????
    private void requestPermission(WritableNativeMap map,Promise promise){
        PermissionUtils.permission(PermissionConstants.LOCATION, PermissionConstants.STORAGE)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> granted) {
                        if(granted.contains(Manifest.permission.ACCESS_COARSE_LOCATION)){
                            getLocationOption(map,promise);
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if(deniedForever.contains(Manifest.permission.ACCESS_COARSE_LOCATION)||denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)){
                            map.putString("latitude","");
                            map.putString("longitude","");
                            promise.reject("400","?????????????????????",map);
                        }
                    }
                }).request();
    }

    //????????????
    //??????AMapLocationClient?????????
    public AMapLocationClient mLocationClient = null;
    //??????AMapLocationClientOption??????
    public AMapLocationClientOption mLocationOption = null;
    private void getLocationOption(WritableNativeMap map,Promise promise){
        Log.d("chh","getLocation");
        //???????????????
        mLocationClient = new AMapLocationClient(reactContext);

        //?????????AMapLocationClientOption??????
        mLocationOption = new AMapLocationClientOption();
        //?????????????????????AMapLocationMode.Battery_Saving?????????????????????
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //???????????????????????????
        mLocationOption.setOnceLocation(true);
        //??????????????????
        mLocationOption.setLocationCacheEnable(false);
        //??????????????????????????????????????????
        mLocationClient.setLocationOption(mLocationOption);
        //????????????
        mLocationClient.startLocation();

        //????????????????????????
        mLocationClient.setLocationListener(amapLocation -> {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //??????????????????amapLocation?????????????????????
                    double latitude = amapLocation.getLatitude();//????????????
                    double longitude = amapLocation.getLongitude();//????????????
                    map.putString("latitude",String.valueOf(latitude));
                    map.putString("longitude",String.valueOf(longitude));
                    promise.resolve(map);
                    Log.d("chh","latitude:"+latitude +"   longitude:"+longitude);
                    mLocationClient.stopLocation();//??????????????????????????????????????????????????????
                }else {
                    //???????????????????????????ErrCode????????????????????????????????????????????????errInfo???????????????????????????????????????
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                    map.putString("latitude","");
                    map.putString("longitude","");
                    promise.reject("400","?????????????????????",map);
                }
            }else{
                map.putString("latitude","");
                map.putString("longitude","");
                promise.reject("400","?????????????????????",map);
            }
        });
    }
}
