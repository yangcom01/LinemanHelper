package location;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.tianjininstitute.yang.linemanhelper.R;

/**
 * Created by b on 2016/5/26.
 * 创建定位类，可以进行定位获得经纬度
 * 创建Location类，实现定位资源接口和定位回调完成接口，实例化Location类后并调用locationInit可以完成定位功能
 */
public class Location implements AMapLocationListener, LocationSource{

    //AMapLocationListener，定位完成回调接口  方法onLocationChanged
    //LocationSource, 位置数据接口 激活定位与停止定位
    AMap aMap;
    Context mContext;
    private boolean isFirstLocation = true;
    private int type;
    private double latitude = 0;
    private double longitude = 0;
    private boolean error = false; //false 定位成功 ture 定位有问题
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker marker;
    private Circle circle;

    public Location(){};
    public Location(AMap aMap, Context mContext, boolean isFirstLocation){
        this.aMap = aMap;
        this.mContext = mContext;
        this.isFirstLocation = isFirstLocation;
    }

    public void locationInit(){
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        type = AMap.LOCATION_TYPE_LOCATE;
    }

    public void changeType(int type){
        this.type = type;
    }

    public LatLng getMyLocation(){
        return new LatLng(latitude,longitude); //返回维度和经度
    }

    public boolean getError(){ return  error;}

    /**
     * 定位完成后回调函数
     * */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        String t1 = Thread.currentThread().getName();
        if (mListener != null && aMapLocation !=null){
            if(aMapLocation.getErrorCode() == 0){
                longitude = aMapLocation.getLongitude();
                latitude = aMapLocation.getLatitude();
                LatLng latLng = new LatLng(latitude, longitude);
                error = false;
                if (isFirstLocation || type == AMap.LOCATION_TYPE_MAP_ROTATE){
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    isFirstLocation = false;
                }
                if (marker != null) marker.destroy();
                if (circle != null) circle.remove();

                //自定义定位成功后的小圆点
                marker = aMap.addMarker(new MarkerOptions().position(latLng)
                        .anchor(0.5f,0.5f)
                        .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.point6)));

                // 自定义定位成功后绘制圆形
                circle = aMap.addCircle(new CircleOptions().center(latLng)
                        .radius(10)
                        .fillColor(0x444169E1)
                        .strokeColor(0x884169E1)
                        .strokeWidth(3f));
            }else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ":" +
                        aMapLocation.getErrorInfo();
                Toast.makeText(this.mContext, errText, Toast.LENGTH_SHORT).show();
                error = true;
            }
            Thread.currentThread().getName();

        }

    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null){
            mLocationClient = new AMapLocationClient(this.mContext);
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听，定位完成后回调onLocationChanged
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
            String t1 = Thread.currentThread().getName();
        }
        String t1 = Thread.currentThread().getName();
    }

   /**
    * 停止定位
    */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null){
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }
}
