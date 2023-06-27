package com.busanit.androidchallenge;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    double mapx, mapy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //인텐트로부터 전달 받은 좌표값 저장
        mapx = Double.parseDouble(getIntent().getStringExtra("mapx"));
        mapy = Double.parseDouble(getIntent().getStringExtra("mapy"));

        //네이버지도 가져오기
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        //전달 받은 좌표값으로 마커를 찍은 뒤 네이버지도 세팅
        naverMap.setCameraPosition(new CameraPosition(new LatLng(mapy, mapx),15));
        Marker marker = new Marker();
        marker.setPosition(new LatLng(mapy, mapx));
        marker.setIcon(MarkerIcons.BLACK);
        marker.setIconTintColor(Color.BLUE);
        marker.setMap(naverMap);

    }
}