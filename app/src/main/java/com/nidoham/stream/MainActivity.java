package com.nidoham.stream;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nidoham.stream.bottom.*;
import com.nidoham.stream.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private BottomNavPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupViewPager();
        setupBottomNavigation();
    }
    
    private void setupViewPager() {
        adapter = new BottomNavPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false); // Disable swipe to prevent conflicts
        
        // Sync ViewPager2 with BottomNavigationView
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.bottomNavigation.setSelectedItemId(getMenuItemId(position));
            }
        });
    }
    
    private int getMenuItemId(int position) {
        switch (position) {
            case 0: return R.id.nav_home;
            case 1: return R.id.nav_download;
            case 2: return R.id.nav_library;
            case 3: return R.id.nav_account;
            default: return R.id.nav_home;
        }
    }
    
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int position = getPositionForItemId(itemId);
            binding.viewPager.setCurrentItem(position, false);
            return true;
        });
    }
    
    private int getPositionForItemId(int itemId) {
        if (itemId == R.id.nav_home) return 0;
        if (itemId == R.id.nav_download) return 1;
        if (itemId == R.id.nav_library) return 2;
        if (itemId == R.id.nav_account) return 3;
        return 0; // Default to home
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy(); // Fix: Corrected from 'superburgo.super.onDestroy()'
        binding = null;
    }
    
    private static class BottomNavPagerAdapter extends FragmentStateAdapter {
        
        public BottomNavPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new HomeFragment();
                case 1: return new DownloadFragment();
                case 2: return new LibraryFragment();
                case 3: return new AccountFragment();
                default: return new HomeFragment();
            }
        }
        
        @Override
        public int getItemCount() {
            return 4;
        }
    }
}