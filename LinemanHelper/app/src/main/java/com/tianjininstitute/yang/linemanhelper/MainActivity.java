package com.tianjininstitute.yang.linemanhelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import databases.LocationDB;
import location.Location;


public class MainActivity extends Activity implements View.OnClickListener{

    AMap aMap;
    MapView mapView;
    Location location;
    int path_id_fir = 0;
    private Button main_patrolBtn, main_trajectoryBtn, main_pictureBtn, main_uploadBtn; // 四个按钮
    private int main_trajectoryType;                                                    //查看轨迹与清除轨迹标识
    private TextView main_latitudeTv, main_longitudeTv;
    private boolean main_patrolBtn_flag = true;                                         //巡线开始与结束的标识
    private boolean main_trajectoryBtn_flag = true;                                     //查看轨迹始与清除的标识
    boolean starting = true;
    boolean hasNewRecord = false;
    private int type_partrol = 2;                                                       //巡线的情况标识
    private LocationDB locationDB;
    private SQLiteDatabase dbWriter;
    private Cursor cursor;
    private Double latitude, longitude;
    private Polyline polyline = null;
    private List<Polyline> polylineList = new ArrayList<Polyline>();
    private Thread patrolThread;                                                       //


    /**
     * 处理定位情况,获得相应的经纬度
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.main_mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.getUiSettings().setCompassEnabled(true);
        aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        location = new Location(aMap, this, false);
        location.locationInit();
        if (location.getError()){
            LatLng position = new LatLng(0, 0);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position,17f);
            aMap.animateCamera(cameraUpdate);
        }

        Button main_locateBtn = (Button) findViewById(R.id.main_locateBtn);
        main_patrolBtn = (Button) findViewById(R.id.main_patrolBtn);
        main_trajectoryBtn = (Button) findViewById(R.id.main_trajectoryBtn);
        main_pictureBtn = (Button) findViewById(R.id.main_pictureBtn);
        main_uploadBtn = (Button) findViewById(R.id.main_uploadBtn);

        main_locateBtn.setOnClickListener(this);
        main_patrolBtn.setOnClickListener(this);
        main_trajectoryBtn.setOnClickListener(this);
        main_pictureBtn.setOnClickListener(this);
        main_uploadBtn.setOnClickListener(this);

        main_latitudeTv = (TextView) findViewById(R.id.main_latitudeTv);
        main_longitudeTv = (TextView) findViewById(R.id.main_longitudeTv);


        locationDB = new LocationDB(this);
        dbWriter = locationDB.getWritableDatabase();
    }


    /**
     * 处理按钮点击事件
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_patrolBtn:
                if (main_patrolBtn_flag == true){
                    type_partrol = 1;
                    startPatrol();
                }
                else {
                    type_partrol = 2;
                    main_patrolBtn_flag = true;
                    endPatrol();
                }
                break;
            case R.id.main_trajectoryBtn:
                if(!main_trajectoryBtn_flag){
                    main_trajectoryBtn_flag = true;
                    main_trajectoryBtn.setText("查看轨迹");
                    clearTrajectory();
                }
                else{
                    main_trajectoryBtn_flag = false;
                    main_trajectoryBtn.setText("清除轨迹");
                    watchTrajectory();
                }

                break;
            case R.id.main_pictureBtn:

                break;
            case R.id.main_uploadBtn:

                break;
            case R.id.main_locateBtn:
                Thread.currentThread().getName();
                main_latitudeTv.setText(location.getMyLocation().latitude + "");
                main_longitudeTv.setText(location.getMyLocation().longitude + "");
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getMyLocation(), 17f));
                break;
        }
    }

    /**
     * 开始巡线
     * */
    public void startPatrol(){
        main_patrolBtn.setText("结束巡线");
        if (location.getError()){
            Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity.this, "开始记录巡线数据，请不要退出此界面", Toast.LENGTH_SHORT).show();
//        patrolThread = new Thread(new StartRecord());
//        patrolThread.start();
        new Thread(new StartRecord()).start();
    }

    public class StartRecord implements Runnable{
        @Override
        public void run() {
            try {
                startRecord();
                Thread.currentThread().getName();
                main_patrolBtn_flag = false;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "记录出现异常", Toast.LENGTH_LONG).show();
                System.out.println("Error message!!!!!" + e.toString());
            }
        }
    }

    public void startRecord() {
        hasNewRecord = true;
        List<LatLng> history_list = new ArrayList<LatLng>();
        PolylineOptions polylineOptions = new PolylineOptions().width(10).addAll(history_list).color(0xFF4876FF);
        polyline = aMap.addPolyline(polylineOptions);

        while (true){
            if (!main_patrolBtn_flag) break;
            if (!location.getError()){
                LatLng patrolLocation = location.getMyLocation();
                latitude = patrolLocation.latitude;
                longitude = patrolLocation.longitude;
                ContentValues cvLocation = new ContentValues();
                cvLocation.put(LocationDB.COLUMN_NAME_LOCATION_LATITUDE,latitude);
                cvLocation.put(LocationDB.COLUMN_NAME_LOCATION_LONGITUDE, longitude);
                cvLocation.put(LocationDB.COLUMN_NAME_DATE, getTime());
                dbWriter.insert(LocationDB.TABLE_NAME_LATLNG, null, cvLocation);
                history_list.add(new LatLng(latitude,longitude));
                polyline.remove();
                polylineOptions = new PolylineOptions().width(10).addAll(history_list).color(0xFF4876FF);
                polyline = aMap.addPolyline(polylineOptions);
                polyline.setVisible(!main_trajectoryBtn_flag);
                Thread.currentThread().getName();
//                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getMyLocation(),aMap.getMaxZoomLevel()));
            }else {
                Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
            }

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String getTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd||HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        return str;
    }

    /**
     * 结束巡线
     * */
    public void endPatrol(){
        Log.e("结束巡线","endPatrol");
        main_patrolBtn.setText("开始巡线");
        Intent tranformTaMg = new Intent(getApplicationContext(),TrajectoryMGActivity.class);
        startActivity(tranformTaMg);
    }

    /**
     * 查看轨迹，
     * */
    public void watchTrajectory(){
//        patrolThread.suspend();
//        Cursor cursor = dbWriter.rawQuery("select latitude, longitude from latlng",null);
//        if (cursor.getCount() == 0){
//            Toast.makeText(MainActivity.this, "不存在历史轨迹", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        cursor.close();
//        List<LatLng> path_list = new ArrayList<LatLng>();
//        while(cursor.moveToNext()){
//            LatLng point = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude"))
//            ,cursor.getDouble(cursor.getColumnIndex("longitude")));
//            path_list.add(point);
//        }
//        PolylineOptions polylineOptions = new PolylineOptions().addAll(path_list).width(10).color(0xFF4876FF);
//        polylineList.add(aMap.addPolyline(polylineOptions));
//        cursor.close();

    }

    public void clearTrajectory(){
            if(polylineList.size() != 0){
                for(int i = 0; i < polylineList.size(); i ++){
                    polylineList.get(i).remove();
                }
            }
            if(polyline != null) {
                polyline.remove();
            }
        patrolThread.resume();
    }

    /**
     * 拍照
     * */
    public void takePicture(){

    }

    /**
     * 上传
     * */
    public void upload(){

    }






















    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
//        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getMyLocation(), 17f));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}



