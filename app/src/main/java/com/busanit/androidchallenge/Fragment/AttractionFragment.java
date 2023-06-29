package com.busanit.androidchallenge.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.busanit.androidchallenge.Activity.InfomationActivity;
import com.busanit.androidchallenge.BuildConfig;
import com.busanit.androidchallenge.GetInfoThread;
import com.busanit.androidchallenge.item;
import com.busanit.androidchallenge.LoadDialog;
import com.busanit.androidchallenge.LocationService;
import com.busanit.androidchallenge.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;


public class AttractionFragment extends Fragment {
    private TextView textView;
    private Handler handler = new Handler();
    private ItemAdapter adapter;
    private final LocationService locationService = new LocationService();
    private LoadDialog loadDialog;
    private ImageView loadImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_attraction, container, false);
        textView = rootView.findViewById(R.id.attractionText);
        ImageView location = rootView.findViewById(R.id.attractionLocation);
        RecyclerView attractionRecyclerView = rootView.findViewById(R.id.attractionRecyclerView);
        //리싸이클러 뷰 설정
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        attractionRecyclerView.setLayoutManager(manager);
        adapter = new ItemAdapter();
        attractionRecyclerView.setAdapter(adapter);

        //현재위치 버튼을 눌렀을 때 로딩화면 설정
        loadDialog = new LoadDialog(getContext());
        loadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadImage = loadDialog.findViewById(R.id.loadImage);
        Glide.with(this).load(R.drawable.loadingani).into(loadImage);


        // 상세보기 버튼 눌렀을 때 상세보기 액티비티(InfomationActivity)로
        adapter.setListener((holder, view, position) -> {
            item item = adapter.getItem(position);
            Intent intent = new Intent(getContext(), InfomationActivity.class);
            intent.putExtra("contentid", item.getContentid());
            intent.putExtra("contenttypeid", item.getContenttypeid());
            startActivity(intent);
        });

        //현재위치 버튼을 눌렀을 때 좌표값으로 현재 지역 알아오기, 근처의 관광지 정보 알아오기, 로딩화면 5초간 실행
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.setMessage("근처 10km 이내 숙박업소를 찾고있어요.\n10초 정도의 시간이 소요될 수 있어요.");
                locationService.startLocationService(getContext(),getActivity());
                GetInfoThread getInfoThread = new GetInfoThread();
                getInfoThread.setAdapter(adapter);
                getInfoThread.setHandler(handler);
                getInfoThread.setLat(locationService.getLat());
                getInfoThread.setLon(locationService.getLon());
                getInfoThread.start();
                LocationThread locationThread = new LocationThread();
                locationThread.start();
                LoadingThread loadingThread = new LoadingThread();
                loadingThread.start();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    //현재 위치로 현재 장소 지역명을 가져와서 표시 (네이버 reverse geocoding api)
    private class LocationThread extends Thread {
        @Override
        public void run() {
            String NAVER_API_KEY = BuildConfig.NAVER_KEY;
            String NAVER_API_KEY_ID = BuildConfig.NAVER_KEY_ID;

            StringBuilder urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=");
            try {
                urlBuilder.append(locationService.getLon() + URLEncoder.encode(",", "UTF-8") + locationService.getLat());
                urlBuilder.append("&output=json");
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", NAVER_API_KEY_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", NAVER_API_KEY);

                BufferedReader rd;
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();

                StringBuilder locationSb = new StringBuilder();
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray jsonArray = new JSONArray(jsonObject.getString("results"));
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(1);
                JSONObject region = (JSONObject) jsonObject1.get("region");
                JSONObject area1 = (JSONObject) region.get("area1");
                JSONObject area2 = (JSONObject) region.get("area2");
                JSONObject area3 = (JSONObject) region.get("area3");
                locationSb.append(area1.getString("name") + " ");
                locationSb.append(area2.getString("name") + " ");
                locationSb.append(area3.getString("name"));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("현재 위치 : " + locationSb.toString());
                    }
                });

            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 현재위치 버튼 클릭 시 로딩화면 5초간 띄우기
    private class LoadingThread extends Thread {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    loadDialog.show();
                }
            });
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDialog.dismiss();
                }
            },5000);
        }
    }
}