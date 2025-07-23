package com.nidoham.stream.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class MusicFragment extends Fragment {
    
   /* private FragmentMusicBinding binding;
    private MusicAdapter musicAdapter;
    private List<MusicTrack> musicTracks;*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*binding = FragmentMusicBinding.inflate(inflater, container, false);
        
        setupRecyclerView();
        loadMusicTracks();*/
        
        return null; // binding.getRoot();
    }
    
    /*private void setupRecyclerView() {
        musicTracks = new ArrayList<>();
        musicAdapter = new MusicAdapter(musicTracks, this::onTrackClick);
        binding.musicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.musicRecyclerView.setAdapter(musicAdapter);
    }
    
    private void loadMusicTracks() {
        // Sample music tracks - replace with actual music API integration
        musicTracks.add(new MusicTrack("Blinding Lights", "The Weeknd", "After Hours", "3:20", "https://example.com/cover1.jpg"));
        musicTracks.add(new MusicTrack("Shape of You", "Ed Sheeran", "÷ (Divide)", "3:53", "https://example.com/cover2.jpg"));
        musicTracks.add(new MusicTrack("Bad Guy", "Billie Eilish", "When We All Fall Asleep", "3:14", "https://example.com/cover3.jpg"));
        musicTracks.add(new MusicTrack("Watermelon Sugar", "Harry Styles", "Fine Line", "2:54", "https://example.com/cover4.jpg"));
        musicTracks.add(new MusicTrack("Levitating", "Dua Lipa", "Future Nostalgia", "3:23", "https://example.com/cover5.jpg"));
        musicTracks.add(new MusicTrack("Good 4 U", "Olivia Rodrigo", "SOUR", "2:58", "https://example.com/cover6.jpg"));
        musicTracks.add(new MusicTrack("Stay", "The Kid LAROI & Justin Bieber", "F*CK LOVE 3", "2:21", "https://example.com/cover7.jpg"));
        musicTracks.add(new MusicTrack("Heat Waves", "Glass Animals", "Dreamland", "3:58", "https://example.com/cover8.jpg"));
        
        musicAdapter.notifyDataSetChanged();
    }
    
    private void onTrackClick(MusicTrack track) {
        // Handle music track click - implement music playback
        // You can integrate with MediaPlayer or ExoPlayer here
        // For now, just show a toast or log
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), 
                "Playing: " + track.getTitle() + " by " + track.getArtist(), 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }*/
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
    }
}