package com.busanit.androidchallenge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Paint;

import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private Fragment attraction = new AttractionFragment();
    private Fragment restaurant = new RestaurantFragment();
    private Fragment hotel = new HotelFragment();
    MyPagerAdapter adapter;
    ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 앱 실행 시 로딩화면
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);


        //바인드
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        ViewPager2 pager = findViewById(R.id.pager);
        TabLayout tab = findViewById(R.id.tab);
        //툴바, 네비게이션 세팅
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //swipe 시 메뉴가 나오지 않게
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //탭, 뷰페이저 세팅
        fragments = new ArrayList<>();
        fragments.add(attraction);
        fragments.add(restaurant);
        fragments.add(hotel);
        adapter = new MyPagerAdapter(this, fragments);
        pager.setAdapter(adapter);



        List<String> tabElement = Arrays.asList("관광지", "음식점", "숙박업소");
        new TabLayoutMediator(tab, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(tabElement.get(position));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                textView.setLayoutParams(params);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tab.setCustomView(textView);
            }
        }).attach();
    }


    //네비게이션바 아이템
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ktoHome) {
            Intent intent = new Intent(this, KtoHomeActivity.class);
            intent.putExtra("webUrl", "https://www.visitkorea.or.kr/m.html");
            startActivity(intent);
        }
        return true;
    }

    //우측 상단 햄버거버튼
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.open_nav) {
            drawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //drawer가 열려 있을 때 back버튼 입력 시 drawer가 닫히게
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else super.onBackPressed();
    }

    //뷰페이저 어댑터
    private class MyPagerAdapter extends FragmentStateAdapter {
        ArrayList<Fragment> fragments;

        public MyPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Fragment> fragments) {
            super(fragmentActivity);
            this.fragments = fragments;
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

    }

}