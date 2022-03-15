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

    //申请权限
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
                            promise.reject("400","获取经纬度失败",map);
                        }
                    }
                }).request();
    }

    //获取定位
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private void getLocationOption(WritableNativeMap map,Promise promise){
        Log.d("chh","getLocation");
        //初始化定位
        mLocationClient = new AMapLocationClient(reactContext);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //获取一次定位结果：
        mLocationOption.setOnceLocation(true);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        //设置定位回调监听
        mLocationClient.setLocationListener(amapLocation -> {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    double latitude = amapLocation.getLatitude();//获取纬度
                    double longitude = amapLocation.getLongitude();//获取经度
                    map.putString("latitude",String.valueOf(latitude));
                    map.putString("longitude",String.valueOf(longitude));
                    promise.resolve(map);
                    Log.d("chh","latitude:"+latitude +"   longitude:"+longitude);
                    mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                    map.putString("latitude","");
                    map.putString("longitude","");
                    promise.reject("400","获取经纬度失败",map);
                }
            }else{
                map.putString("latitude","");
                map.putString("longitude","");
                promise.reject("400","获取经纬度失败",map);
            }
        });
    }
}
