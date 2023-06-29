package com.busanit.androidchallenge;

import android.os.Handler;

import com.busanit.androidchallenge.Fragment.ItemAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;


public class GetInfoThread extends Thread{

    private Handler handler = new Handler();
    private ItemAdapter adapter;
    private double lat, lon;


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setAdapter(ItemAdapter adapter) {
        this.adapter = adapter;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }



    @Override
    public void run() {
        ArrayList<item> items = new ArrayList<>();
        ArrayList<item> sortedItems = new ArrayList<>();
        String TRAVEL_API_KEY = BuildConfig.TRAVEL_API_KEY;

        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B551011/KorService1/locationBasedList1?");
        try {
            urlBuilder.append(URLEncoder.encode("serviceKey", "UTF-8") + "=" + TRAVEL_API_KEY);
            urlBuilder.append("&numOfRows=3000&pageNo=1&MobileOS=AND&MobileApp=AndroidChallenge&_type=xml&listYN=Y&arrange=O&mapX=" + lon + "&mapY=" + lat + "&radius=10000&contentTypeId=12");
            Document document = Jsoup.connect(urlBuilder.toString()).parser(Parser.xmlParser()).get();
            Elements elements = document.select("item");
            // 아이템을 가져와서
            for (int i = 0; i < elements.size(); i++) {
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
            for (int i = 0; i < items.size(); i++) {
                dist[i] = Double.parseDouble(items.get(i).getDist());
            }
            Arrays.sort(dist);
            for (int i = 0; i < dist.length; i++) {
                for (int j = 0; j < items.size(); j++) {
                    if (dist[i] == Double.parseDouble(items.get(j).getDist())) {
                        sortedItems.add(items.get(j));
                        //거리가 같은 아이템이 있을시 중복출력 방지
                        items.remove(j);
                        break;
                    }
                }
            }
            //단위 표시
            for (int i = 0; i < sortedItems.size(); i++) {
                if (Double.parseDouble(sortedItems.get(i).getDist()) >= 1000) {
                    sortedItems.get(i).setDist(String.format("%.2f",
                            Double.parseDouble(sortedItems.get(i).getDist()) / 1000) + "km");
                } else {
                    sortedItems.get(i).setDist(String.format("%.1f",
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
