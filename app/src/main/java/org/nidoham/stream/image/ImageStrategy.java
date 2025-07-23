package org.nidoham.stream.image;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.Priority;
import java.util.List;
import org.schabi.newpipe.extractor.Image;

public class ImageStrategy {
    
    public enum PreferredImageQuality {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
    
    private static PreferredImageQuality preferredImageQuality = PreferredImageQuality.MEDIUM;
    
    public static void setPreferredImageQuality(PreferredImageQuality quality) {
        preferredImageQuality = quality;
    }
    
    public static PreferredImageQuality getPreferredImageQuality() {
        return preferredImageQuality;
    }
    
    public static String choosePreferredImage(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        
        // Handle NONE quality - return first available image
        if (preferredImageQuality == PreferredImageQuality.NONE) {
            return images.stream()
                .filter(image -> image != null && image.getUrl() != null && !image.getUrl().isEmpty())
                .findFirst()
                .map(Image::getUrl)
                .orElse(null);
        }
        
        // Sort by quality and get preferred quality URL
        return images.stream()
            .filter(image -> image != null && image.getUrl() != null && !image.getUrl().isEmpty())
            .filter(image -> image.getWidth() > 0 && image.getHeight() > 0) // Valid dimensions
            .sorted((img1, img2) -> {
                return compareImagesByQuality(img1, img2);
            })
            .findFirst()
            .map(Image::getUrl)
            .orElse(null);
    }
    
    private static int compareImagesByQuality(Image img1, Image img2) {
        switch (preferredImageQuality) {
            case HIGH:
                // For HIGH quality, we want the largest image (highest resolution)
                int area1 = img1.getWidth() * img1.getHeight();
                int area2 = img2.getWidth() * img2.getHeight();
                return Integer.compare(area2, area1); // Descending order (largest first)
                
            case MEDIUM:
                // For MEDIUM quality, find the image closest to our target resolution
                int targetArea = 640 * 480; // 480p target
                int diff1 = Math.abs((img1.getWidth() * img1.getHeight()) - targetArea);
                int diff2 = Math.abs((img2.getWidth() * img2.getHeight()) - targetArea);
                return Integer.compare(diff1, diff2); // Ascending order (closest to target first)
                
            case LOW:
                // For LOW quality, we want the smallest image
                int lowArea1 = img1.getWidth() * img1.getHeight();
                int lowArea2 = img2.getWidth() * img2.getHeight();
                return Integer.compare(lowArea1, lowArea2); // Ascending order (smallest first)
                
            case NONE:
            default:
                return 0; // No preference
        }
    }
    
    // Alternative method that considers aspect ratio and quality more intelligently
    public static String choosePreferredImageAdvanced(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        
        // Handle NONE quality - return first available image
        if (preferredImageQuality == PreferredImageQuality.NONE) {
            return images.stream()
                .filter(image -> image != null && image.getUrl() != null && !image.getUrl().isEmpty())
                .findFirst()
                .map(Image::getUrl)
                .orElse(null);
        }
        
        return images.stream()
            .filter(image -> image != null && image.getUrl() != null && !image.getUrl().isEmpty())
            .filter(image -> image.getWidth() > 0 && image.getHeight() > 0)
            .sorted((img1, img2) -> {
                double score1 = calculateImageScore(img1);
                double score2 = calculateImageScore(img2);
                
                if (preferredImageQuality == PreferredImageQuality.HIGH) {
                    return Double.compare(score2, score1); // Higher score first
                } else {
                    return Double.compare(score1, score2); // Lower score first
                }
            })
            .findFirst()
            .map(Image::getUrl)
            .orElse(null);
    }
    
    private static double calculateImageScore(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int area = width * height;
        
        // Consider aspect ratio (prefer 16:9 or 4:3)
        double aspectRatio = (double) width / height;
        double aspectRatioScore = 1.0;
        
        // Prefer common video aspect ratios
        if (Math.abs(aspectRatio - 16.0/9.0) < 0.1 || Math.abs(aspectRatio - 4.0/3.0) < 0.1) {
            aspectRatioScore = 0.9; // Bonus for good aspect ratio
        }
        
        switch (preferredImageQuality) {
            case HIGH:
                // For HIGH quality, prioritize resolution but consider aspect ratio
                return area * aspectRatioScore;
                
            case MEDIUM:
                // For MEDIUM quality, find balance between size and target resolution
                int targetArea = 640 * 480;
                double sizeDiff = Math.abs(area - targetArea) / (double) targetArea;
                return sizeDiff * aspectRatioScore;
                
            case LOW:
                // For LOW quality, prefer smaller images
                return area * aspectRatioScore;
                
            case NONE:
            default:
                return 0.0;
        }
    }
    
    // Utility method to load image with Glide based on quality preference
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        RequestOptions options = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.NORMAL);
        
        // Adjust options based on quality preference
        switch (preferredImageQuality) {
            case HIGH:
                options = options.priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache high quality images
                break;
            case LOW:
                options = options.override(320, 240) // Lower resolution
                    .priority(Priority.LOW);
                break;
            case MEDIUM:
                options = options.override(640, 480) // Medium resolution
                    .priority(Priority.NORMAL);
                break;
            case NONE:
            default:
                // Use default options
                break;
        }
        
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .into(imageView);
    }
    
    // Debug method to help understand image selection
    public static void debugImageSelection(List<Image> images) {
        if (images == null || images.isEmpty()) {
            System.out.println("No images available");
            return;
        }
        
        System.out.println("Available images (Width x Height = Area):");
        for (int i = 0; i < images.size(); i++) {
            Image img = images.get(i);
            if (img != null) {
                int area = img.getWidth() * img.getHeight();
                System.out.println(String.format("Image %d: %dx%d = %d pixels", 
                    i, img.getWidth(), img.getHeight(), area));
            }
        }
        
        String selected = choosePreferredImage(images);
        System.out.println("Selected image URL: " + selected);
    }
}