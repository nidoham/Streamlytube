package org.nidoham.stream.image;

import java.util.List;
import org.schabi.newpipe.extractor.Image;

public class HighQualityImageHelper {
    private final String url;

    public HighQualityImageHelper(List<Image> images) {
        String bestUrl = "";
        int maxScore = 0;
        for (Image img : images) {
            int currentScore = img.getWidth() * img.getHeight();
            if (maxScore < currentScore) {
                maxScore = currentScore;
                bestUrl = img.getUrl();
            }
        }
        this.url = bestUrl;
    }

    public String getUrl() {
        return url;
    }
}