package com.nidoham.stream.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.nidoham.stream.R;
import com.nidoham.stream.databinding.ItemVideosBinding;

import org.nidoham.stream.image.ImageStrategy;
import org.nidoham.stream.util.DurationFormatter;
import org.nidoham.stream.util.TimeAgoFormatter;
import org.nidoham.stream.util.Utils;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView Adapter for displaying a list of StreamInfoItem objects.
 * Utilizes ListAdapter with DiffUtil for efficient list updates and animations.
 */
public class VideoAdapter extends ListAdapter<StreamInfoItem, VideoAdapter.VideoViewHolder> {

    private final OnVideoClickListener listener;

    /**
     * Interface for handling click events on video items and their download buttons.
     */
    public interface OnVideoClickListener {
        void onVideoClick(StreamInfoItem video);
        void onDownloadClick(StreamInfoItem video);
    }

    /**
     * Constructor for the VideoAdapter.
     * @param listener The click listener for video items and download actions.
     */
    public VideoAdapter(OnVideoClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVideosBinding binding = ItemVideosBinding.inflate(inflater, parent, false);
        return new VideoViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        StreamInfoItem video = getItem(position);
        holder.bind(video);
    }

    /**
     * ViewHolder for individual video items.
     */
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemVideosBinding binding;
        private final OnVideoClickListener listener;

        public VideoViewHolder(@NonNull ItemVideosBinding binding, OnVideoClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        /**
         * Binds the StreamInfoItem data to the views in the item layout.
         * @param video The StreamInfoItem to bind.
         */
        public void bind(StreamInfoItem video) {
            if (video == null) {
                return;
            }

            // Set preferred image quality for thumbnails
            ImageStrategy.setPreferredImageQuality(ImageStrategy.PreferredImageQuality.HIGH);

            // Get thumbnail and channel avatar URLs with null checks
            String thumbnailUrl = null;
            String channelAvatar = null;

            List<Image> thumbnails = video.getThumbnails();
            if (thumbnails != null && !thumbnails.isEmpty()) {
                thumbnailUrl = ImageStrategy.choosePreferredImage(thumbnails);
            }

            List<Image> uploaderAvatars = video.getUploaderAvatars();
            if (uploaderAvatars != null && !uploaderAvatars.isEmpty()) {
                channelAvatar = ImageStrategy.choosePreferredImage(uploaderAvatars);
            }

            // Load images using ImageStrategy
            ImageStrategy.loadImage(binding.ivThumbnail.getContext(), thumbnailUrl, binding.ivThumbnail);
            ImageStrategy.loadImage(binding.ivChannelLogo.getContext(), channelAvatar, binding.ivChannelLogo);

            // Set Video Title
            String title = video.getName();
            binding.tvTitle.setText(title != null ? title : "Unknown Title");

            // Set Video Duration
            long duration = video.getDuration();
            if (duration > 0) {
                binding.tvDuration.setText(DurationFormatter.formatSeconds(duration));
                binding.tvDuration.setVisibility(View.VISIBLE);
            } else {
                binding.tvDuration.setVisibility(View.GONE);
            }

            // Set Channel Name and Views/Upload Date
            String channelName = video.getUploaderName();
            long viewCount = video.getViewCount();
            String uploadTime = "";

            if (video.getUploadDate() != null && video.getUploadDate().date() != null) {
                uploadTime = TimeAgoFormatter.format(video.getUploadDate().date().getTime());
            }

            // Set channel name
            binding.tvTitle.setText(channelName != null && !channelName.trim().isEmpty() 
                ? channelName 
                : "Unknown Channel");

            // Build channel info string
            String channelInfo = "";
            if (viewCount >= 0) {
                channelInfo += Utils.formatViewCount(viewCount);
            }
            if (!uploadTime.isEmpty()) {
                if (!channelInfo.isEmpty()) {
                    channelInfo += " • ";
                }
                channelInfo += uploadTime;
            }
            binding.tvChannelInfo.setText(channelInfo.isEmpty() ? "No info available" : channelInfo);

            // Click listener for the whole item
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(video);
                }
            });

            // Click listener for the download icon
            binding.ivDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(video);
                }
            });
        }
    }

    /**
     * DiffUtil.ItemCallback for calculating differences between old and new lists.
     * This is crucial for efficient RecyclerView updates.
     */
    private static final DiffUtil.ItemCallback<StreamInfoItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StreamInfoItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull StreamInfoItem oldItem, @NonNull StreamInfoItem newItem) {
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem;
                    }
                    return Objects.equals(oldItem.getUrl(), newItem.getUrl());
                }

                @Override
                public boolean areContentsTheSame(@NonNull StreamInfoItem oldItem, @NonNull StreamInfoItem newItem) {
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem;
                    }
                    return Objects.equals(oldItem.getName(), newItem.getName()) &&
                           Objects.equals(oldItem.getUploaderName(), newItem.getUploaderName()) &&
                           oldItem.getViewCount() == newItem.getViewCount() &&
                           oldItem.getDuration() == newItem.getDuration() &&
                           Objects.equals(oldItem.getUploadDate(), newItem.getUploadDate()) &&
                           Objects.equals(oldItem.getThumbnails(), newItem.getThumbnails()) &&
                           Objects.equals(oldItem.getUploaderAvatars(), newItem.getUploaderAvatars());
                }
            };
}