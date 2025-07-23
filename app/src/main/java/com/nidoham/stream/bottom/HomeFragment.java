package com.nidoham.stream.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.nidoham.stream.databinding.FragentHomeBinding;
import com.nidoham.stream.views.SearchFragment;
import com.nidoham.stream.views.MoreFragment;
import com.nidoham.stream.views.MusicFragment;
import com.nidoham.stream.views.YouTubeFragment;

public class HomeFragment extends Fragment {
    private FragentHomeBinding binding;
    private static final String[] TAB_TITLES = new String[]{"Search", "YouTube", "Music", "More"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure ViewPager2
        binding.viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setUserInputEnabled(false);

        // Set up ViewPager2 with inline FragmentStateAdapter
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Create new Fragment instance for each tab
                switch (position) {
                    case 0:
                        return new SearchFragment();
                    case 1:
                        return new YouTubeFragment();
                    case 2:
                        return new MusicFragment();
                    case 3:
                        return new MoreFragment();
                    default:
                        return new SearchFragment(); // Fallback
                }
            }

            @Override
            public int getItemCount() {
                return TAB_TITLES.length; // 4 tabs
            }
        });

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Handle tab clicks to ensure new fragment instances
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Force new fragment instance on reselect
                binding.viewPager.setCurrentItem(tab.getPosition(), false);
                binding.viewPager.getAdapter().notifyItemChanged(tab.getPosition());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}