package com.busanit.androidchallenge.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.busanit.androidchallenge.BuildConfig;
import com.busanit.androidchallenge.LoadDialog;
import com.busanit.androidchallenge.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;

public class InfomationActivity extends AppCompatActivity {

    TextView infoTitle, infoAddress, infoText;
    private String mapx,mapy,contentid,contenttypeid,infotitleStr, infoaddressStr, infotextStr, url;
    private ImageView infoImageView;
    Handler handler = new Handler();
    LoadDialog loadDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomation);

        //처음 액티비티 실행 시 로딩화면 세팅
        loadDialog = new LoadDialog(this);
        loadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView loadImage = loadDialog.findViewById(R.id.loadImage);
        Glide.with(this).load(R.drawable.loadingani).into(loadImage);

        // 로딩화면 표시
        LoadingThread loadingThread = new LoadingThread();
        loadingThread.start();

        infoTitle = findViewById(R.id.infoTitle);
        infoAddress = findViewById(R.id.infoAddress);
        infoText = findViewById(R.id.infoText);
        infoImageView = findViewById(R.id.infoImageView);

        //각 프래그먼트에서 보낸 인텐트로부터 받아온 정보 저장
        contentid = getIntent().getStringExtra("contentid");
        contenttypeid = getIntent().getStringExtra("contenttypeid");

        //지도 버튼을 눌렀을 때 좌표값을 인텐트에 넣은 뒤 맵 액티비티 실행
        Button button = findViewById(R.id.btnMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapActivity.class);
                intent.putExtra("mapx",mapx);
                intent.putExtra("mapy",mapy);
                startActivity(intent);
            }
        });

        //프래그먼트에서 받아온 정보를 이용해 해당 컨텐츠의 상세정보 가져오기
        GetInfomationThread getInfomationThread = new GetInfomationThread();
        getInfomationThread.start();


    }


    private class GetInfomationThread extends Thread {
        @Override
        public void run() {
            String TRAVEL_API_KEY = BuildConfig.TRAVEL_API_KEY;
            StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B551011/KorService1/detailCommon1?");
            try {
                urlBuilder.append(URLEncoder.encode("serviceKey","UTF-8") +
                        "=" + TRAVEL_API_KEY);
                urlBuilder.append("&MobileOS=AND&MobileApp=AndroidChallenge&_type=xml&contentId=" +contentid
                        +"&contentTypeId="+contenttypeid+"&defaultYN=Y&firstImageYN=Y&areacodeYN=N&catcodeYN=N&addrinfoYN=Y&mapinfoYN=Y&overviewYN=Y&numOfRows=10&pageNo=1");
                Document document = Jsoup.connect(urlBuilder.toString()).parser(Parser.xmlParser()).get();
                Elements elements =document.select("item");
                for (int i = 0 ;  i< elements.size(); i++){
                    Element element = elements.get(i);
                    infotitleStr = element.select("title").text();
                    infoaddressStr = element.select("addr1").text() + " "
                            + element.select("addr2").text();
                    infotextStr = element.select("overview").text();
                    url = element.select("firstimage").text();
                    mapx= element.select("mapx").text();
                    mapy= element.select("mapy").text();

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    infoTitle.setText(infotitleStr);
                    infoAddress.setText(infoaddressStr);
                    infoText.setText(infotextStr);
                    Glide.with(getApplicationContext()).load(url).error(R.drawable.kto)
                            .fallback(R.drawable.kto).into(infoImageView);
                }
            });
        }
    }

    //4초후 로딩화면 종료
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
            },4000);
        }
    }
}