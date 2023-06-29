package com.busanit.androidchallenge.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.busanit.androidchallenge.Activity.InfomationActivity;
import com.busanit.androidchallenge.BuildConfig;
import com.busanit.androidchallenge.item;
import com.busanit.androidchallenge.LoadDialog;
import com.busanit.androidchallenge.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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


public class RestaurantFragment extends Fragment {
    private TextView textView;
    private Handler handler = new Handler();
    private double lat, lon;
    private ItemAdapter adapter;
    LoadDialog loadDialog;
    ImageView loadImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)  inflater.inflate(R.layout.fragment_restaurant, container, false);

        textView = rootView.findViewById(R.id.restaurantText);
        ImageView location = rootView.findViewById(R.id.restaurantLocation);

        //리싸이클러 뷰 세팅
        RecyclerView restaurantRecyclerView = rootView.findViewById(R.id.restaurantRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        restaurantRecyclerView.setLayoutManager(manager);
        adapter = new ItemAdapter();
        restaurantRecyclerView.setAdapter(adapter);

        //현재위치 버튼을 눌렀을 때 로딩화면 설정
        loadDialog = new LoadDialog(getContext());
        loadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadImage = loadDialog.findViewById(R.id.loadImage);
        Glide.with(this).load(R.drawable.loadingani).into(loadImage);

        // 상세보기 버튼 눌렀을 때 상세보기 액티비티(InfomationActivity)로
        adapter.setListener(new OnInfoButtonClickListener() {
            @Override
            public void onButtonClick(ItemAdapter.ViewHolder holder, View view, int position) {
                item item = adapter.getItem(position);
                Intent intent = new Intent(getContext(), InfomationActivity.class);
                intent.putExtra("contentid", item.getContentid());
                intent.putExtra("contenttypeid",item.getContenttypeid());
                startActivity(intent);
            }
        });

        //현재위치 버튼 클릭 시
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
                GetInfoThread getInfoThread = new GetInfoThread();
                getInfoThread.start();
                LocationThread locationThread = new LocationThread();
                locationThread.start();
                LoadingThread loadingThread = new LoadingThread();
                loadingThread.start();


            }
        });
        return rootView;
    }

    // 현재 위치 좌표값 가져오기
    private void startLocationService() {
        LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},101);
            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location!=null){
            lat = location.getLatitude();
            lon = location.getLongitude();

        }
        GPSListener listener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
        Toast.makeText(getContext(), "근처 10km 이내 음식점을 찾고있어요.\n10초 정도의 시간이 소요될 수 있어요.", Toast.LENGTH_LONG).show();

    }

    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
    }

    // 현재 위치를 기반으로 리싸이클러 뷰에 표시할 반경 10km 이내의 음식점 정보 가져오기 (한국관광공사 api)
    private class GetInfoThread extends Thread{

        @Override
        public void run() {
            ArrayList<item> items = new ArrayList<>();
            ArrayList<item> sortedItems = new ArrayList<>();
            String TRAVEL_API_KEY = BuildConfig.TRAVEL_API_KEY;

            StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B551011/KorService1/locationBasedList1?");
            try {
                urlBuilder.append(URLEncoder.encode("serviceKey","UTF-8") + "=" + TRAVEL_API_KEY);
                urlBuilder.append("&numOfRows=3000&pageNo=1&MobileOS=AND&MobileApp=AndroidChallenge&_type=xml&listYN=Y&arrange=O&mapX="+lon+"&mapY="+lat+"&radius=10000&contentTypeId=39");
                Document document = Jsoup.connect(urlBuilder.toString()).parser(Parser.xmlParser()).get();
                Elements elements =document.select("item");
                // 아이템을 가져와서
                for(int i = 0 ; i < elements.size(); i++){
                    Element element = elements.get(i);
                    item item = new item();
                    item.setContentid(element.select("contentid").text());
                    item.setDist(element.select("dist").text());
                    item.setFirstimage2(element.select("firstimage2").text());
                    item.setTitle(element.select("title").text());
                    item.setContenttypeid(element.select("contenttypeid").text());
                    items.add(item);
                }
                //거리를 뽑아 낸 뒤, 거리순으로 정렬 후 세팅
                double[] dist = new double[items.size()];
                for(int i = 0 ; i <items.size(); i++){
                    dist[i] = Double.parseDouble(items.get(i).getDist());
                }
                Arrays.sort(dist);
                for(int i = 0; i< dist.length ;i++){
                    for(int j = 0; j< items.size(); j++){
                        if(dist[i]==Double.parseDouble(items.get(j).getDist())){
                            sortedItems.add(items.get(j));
                            //거리가 같은 아이템이 있을시 중복출력 방지
                            items.remove(j);
                            break;
                        }
                    }
                }
                //단위 표시
                for(int i = 0; i<sortedItems.size(); i++){
                    if(Double.parseDouble(sortedItems.get(i).getDist())>=1000){
                        sortedItems.get(i).setDist(String.format("%.2f",
                                Double.parseDouble(sortedItems.get(i).getDist())/1000) + "km");
                    } else { sortedItems.get(i).setDist(String.format("%.1f",
                            Double.parseDouble(sortedItems.get(i).getDist())) + "m");
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setItems(sortedItems);
                        adapter.notifyDataSetChanged();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //현재 위치로 현재 장소 지역명을 가져와서 표시 (네이버 reverse geocoding api)
    private class LocationThread extends Thread {
        @Override
        public void run() {

            String NAVER_API_KEY = BuildConfig.NAVER_KEY;
            String NAVER_API_KEY_ID = BuildConfig.NAVER_KEY_ID;

            StringBuilder urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=");
            try {
                urlBuilder.append(lon+URLEncoder.encode(",","UTF-8")+lat);
                urlBuilder.append("&output=json");
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID",NAVER_API_KEY_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", NAVER_API_KEY);

                BufferedReader rd;
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300){
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = rd.readLine()) != null){
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();

                StringBuilder locationSb= new StringBuilder();
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray jsonArray = new JSONArray(jsonObject.getString("results"));
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(1);
                JSONObject region = (JSONObject) jsonObject1.get("region");
                JSONObject area1 = (JSONObject) region.get("area1");
                JSONObject area2 = (JSONObject) region.get("area2");
                JSONObject area3 = (JSONObject) region.get("area3");
                locationSb.append(area1.getString("name")+" ");
                locationSb.append(area2.getString("name")+" ");
                locationSb.append(area3.getString("name"));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("현재 위치 : " +locationSb.toString());
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