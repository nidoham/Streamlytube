package org.schabi.newpipe.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.nidoham.stream.App;
import com.nidoham.stream.R;

import org.nidoham.stream.model.VideoItem;
import androidx.core.text.HtmlCompat;
import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;
import org.schabi.newpipe.extractor.utils.Utils;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ExtractorHelper {
    private static final String TAG = "ExtractorHelper";
    private static final InfoCache CACHE = InfoCache.getInstance();

    private ExtractorHelper() {
        // No instance
    }

    private static void checkServiceId(final int serviceId) {
        if (serviceId == Constants.NO_SERVICE_ID) {
            throw new IllegalArgumentException("serviceId is NO_SERVICE_ID");
        }
    }

    public static Single<SearchInfo> searchFor(final int serviceId, final String searchString,
                                               final List<String> contentFilter,
                                               final String sortFilter) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                SearchInfo.getInfo(
                        NewPipe.getService(serviceId),
                        NewPipe.getService(serviceId)
                                .getSearchQHFactory()
                                .fromQuery(searchString, contentFilter, sortFilter)
                )
        ).subscribeOn(Schedulers.io());
    }

    public static Single<InfoItemsPage<InfoItem>> getMoreSearchItems(
            final int serviceId,
            final String searchString,
            final List<String> contentFilter,
            final String sortFilter,
            final Page page) {
        checkServiceId(serviceId);
        return Single.fromCallable(() -> {
            SearchQueryHandler queryHandler = NewPipe.getService(serviceId)
                    .getSearchQHFactory()
                    .fromQuery(searchString, contentFilter, sortFilter);
            return SearchInfo.getMoreItems(
                    NewPipe.getService(serviceId),
                    queryHandler,
                    page
            );
        }).subscribeOn(Schedulers.io());
    }

    public static Single<List<String>> suggestionsFor(final int serviceId, final String query) {
        checkServiceId(serviceId);
        return Single.fromCallable(() -> {
            final SuggestionExtractor extractor = NewPipe.getService(serviceId)
                    .getSuggestionExtractor();
            return extractor != null
                    ? extractor.suggestionList(query)
                    : Collections.<String>emptyList();
        }).subscribeOn(Schedulers.io());
    }

    public static Single<StreamInfo> getStreamInfo(final int serviceId, final String url,
                                                   final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.CacheType.STREAM,
                Single.fromCallable(() -> StreamInfo.getInfo(NewPipe.getService(serviceId), url))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<ChannelInfo> getChannelInfo(final int serviceId, final String url,
                                                     final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.CacheType.CHANNEL,
                Single.fromCallable(() ->
                        ChannelInfo.getInfo(NewPipe.getService(serviceId), url))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<ChannelTabInfo> getChannelTab(final int serviceId,
                                                       final ListLinkHandler listLinkHandler,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId,
                listLinkHandler.getUrl(), InfoCache.CacheType.CHANNEL_TAB,
                Single.fromCallable(() ->
                        ChannelTabInfo.getInfo(NewPipe.getService(serviceId), listLinkHandler))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<InfoItemsPage<InfoItem>> getMoreChannelTabItems(
            final int serviceId,
            final ListLinkHandler listLinkHandler,
            final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                ChannelTabInfo.getMoreItems(NewPipe.getService(serviceId),
                        listLinkHandler, nextPage)).subscribeOn(Schedulers.io());
    }

    public static Single<CommentsInfo> getCommentsInfo(final int serviceId,
                                                       final String url,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.CacheType.COMMENTS,
                Single.fromCallable(() ->
                        CommentsInfo.getInfo(NewPipe.getService(serviceId), url))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<InfoItemsPage<CommentsInfoItem>> getMoreCommentItems(
            final int serviceId,
            final CommentsInfo info,
            final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                CommentsInfo.getMoreItems(NewPipe.getService(serviceId), info, nextPage))
                .subscribeOn(Schedulers.io());
    }

    public static Single<PlaylistInfo> getPlaylistInfo(final int serviceId,
                                                       final String url,
                                                       final boolean forceLoad) {
        checkServiceId(serviceId);
        return checkCache(forceLoad, serviceId, url, InfoCache.CacheType.PLAYLIST,
                Single.fromCallable(() ->
                        PlaylistInfo.getInfo(NewPipe.getService(serviceId), url))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<InfoItemsPage<StreamInfoItem>> getMorePlaylistItems(final int serviceId,
                                                                             final String url,
                                                                             final Page nextPage) {
        checkServiceId(serviceId);
        return Single.fromCallable(() ->
                PlaylistInfo.getMoreItems(NewPipe.getService(serviceId), url, nextPage))
                .subscribeOn(Schedulers.io());
    }

    public static Single<KioskInfo> getKioskInfo(final int serviceId,
                                                 final String url,
                                                 final boolean forceLoad) {
        return checkCache(forceLoad, serviceId, url, InfoCache.CacheType.KIOSK,
                Single.fromCallable(() -> KioskInfo.getInfo(NewPipe.getService(serviceId), url))
                        .subscribeOn(Schedulers.io()));
    }

    public static Single<InfoItemsPage<StreamInfoItem>> getMoreKioskItems(final int serviceId,
                                                                          final String url,
                                                                          final Page nextPage) {
        return Single.fromCallable(() ->
                KioskInfo.getMoreItems(NewPipe.getService(serviceId), url, nextPage))
                .subscribeOn(Schedulers.io());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Cache
    //////////////////////////////////////////////////////////////////////////*/

    private static <I extends Info> Single<I> checkCache(final boolean forceLoad,
                                                         final int serviceId,
                                                         @NonNull final String url,
                                                         @NonNull final InfoCache.CacheType cacheType,
                                                         @NonNull final Single<I> loadFromNetwork) {
        checkServiceId(serviceId);
        final Single<I> actualLoadFromNetwork = loadFromNetwork
                .doOnSuccess(info -> {
                    CACHE.putInfo(serviceId, url, info, cacheType);
                    if (App.DEBUG) {
                        Log.d(TAG, "Saved to cache: " + (info != null ? info.getName() : "null"));
                    }
                    // Show Toast for cache save
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(App.getAppContext(),
                            "Cached info: " + (info != null ? info.getName() : "null"),
                            Toast.LENGTH_SHORT).show()
                    );
                });

        final Single<I> load;
        if (forceLoad) {
            CACHE.removeInfo(serviceId, url, cacheType);
            load = actualLoadFromNetwork;
        } else {
            load = Maybe.concat(ExtractorHelper.loadFromCache(serviceId, url, cacheType),
                            actualLoadFromNetwork.toMaybe())
                    .firstElement()
                    .toSingle();
        }

        return load.subscribeOn(Schedulers.io());
    }

    private static <I extends Info> Maybe<I> loadFromCache(
            final int serviceId,
            @NonNull final String url,
            @NonNull final InfoCache.CacheType cacheType) {
        checkServiceId(serviceId);
        return Maybe.defer(() -> {
            //noinspection unchecked
            final I info = (I) CACHE.getInfo(serviceId, url, cacheType);
            if (App.DEBUG) {
                Log.d(TAG, "Loaded from cache: " + (info != null ? info.getName() : "null"));
            }
            // Show Toast for cache hit/miss
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(App.getAppContext(),
                    info != null ? "Loaded from cache: " + info.getName() : "Cache miss for: " + url,
                    Toast.LENGTH_SHORT).show()
            );
            return info == null ? Maybe.empty() : Maybe.just(info);
        }).subscribeOn(Schedulers.io());
    }

    public static boolean isCached(final int serviceId,
                                   @NonNull final String url,
                                   @NonNull final InfoCache.CacheType cacheType) {
        return loadFromCache(serviceId, url, cacheType).blockingGet() != null;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static void showMetaInfoInTextView(@Nullable final List<MetaInfo> metaInfos,
                                              final TextView metaInfoTextView,
                                              final View metaInfoSeparator,
                                              final CompositeDisposable disposables) {
        final Context context = metaInfoTextView.getContext();
        if (metaInfos == null || metaInfos.isEmpty()
                || !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "show_meta_info_key", true)) {
            metaInfoTextView.setVisibility(View.GONE);
            metaInfoSeparator.setVisibility(View.GONE);
        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final MetaInfo metaInfo : metaInfos) {
                if (!Utils.isNullOrEmpty(metaInfo.getTitle())) {
                    stringBuilder.append("<b>").append(metaInfo.getTitle()).append("</b>");
                }

                String content = metaInfo.getContent().getContent().trim();
                if (!content.isEmpty()) {
                    if (stringBuilder.length() > 0) stringBuilder.append(": ");
                    stringBuilder.append(content);
                }

                for (int i = 0; i < metaInfo.getUrls().size(); i++) {
                    if (i == 0) {
                        stringBuilder.append("<br>");
                    } else {
                        stringBuilder.append("<br>");
                    }

                    stringBuilder
                            .append("<a href=\"").append(metaInfo.getUrls().get(i)).append("\">")
                            .append(capitalizeIfAllUppercase(metaInfo.getUrlTexts().get(i).trim()))
                            .append("</a>");
                }
                stringBuilder.append("<br><br>");
            }

            metaInfoSeparator.setVisibility(View.VISIBLE);
            metaInfoTextView.setVisibility(View.VISIBLE);
            
            final Spanned html = HtmlCompat.fromHtml(
                stringBuilder.toString(), 
                HtmlCompat.FROM_HTML_MODE_LEGACY
            );
            metaInfoTextView.setText(html);
            metaInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private static String capitalizeIfAllUppercase(final String text) {
        if (text == null || text.isEmpty()) return text;
        
        boolean allUppercase = true;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLowerCase(text.charAt(i))) {
                allUppercase = false;
                break;
            }
        }

        if (allUppercase) {
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
        return text;
    }
    
    public static String getStreamUrl(StreamInfoItem item) {
        try {
            return item.getUrl();
        } catch (Exception e) {
            return "";
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Video Quality URLs
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * Get all available video stream URLs for a video
     * 
     * @param serviceId Service ID (e.g. YouTube)
     * @param url Video URL
     * @return Single emitting list of video stream URLs
     */
    public static Single<List<VideoStream>> getAllVideoStreams(final int serviceId, final String url) {
        return getStreamInfo(serviceId, url, false)
            .map(streamInfo -> {
                List<VideoStream> streams = new ArrayList<>();
                
                // Add muxed streams (video + audio)
                streams.addAll(streamInfo.getVideoStreams());
                
                // Add video-only streams
                streams.addAll(streamInfo.getVideoOnlyStreams());
                
                return streams;
            })
            .subscribeOn(Schedulers.io());
    }

    /**
     * Get all available audio stream URLs for a video
     * 
     * @param serviceId Service ID (e.g. YouTube)
     * @param url Video URL
     * @return Single emitting list of audio stream URLs
     */
    public static Single<List<AudioStream>> getAllAudioStreams(final int serviceId, final String url) {
        return getStreamInfo(serviceId, url, false)
            .map(streamInfo -> streamInfo.getAudioStreams())
            .subscribeOn(Schedulers.io());
    }

    /**
     * Get all available stream URLs (video and audio) for a video
     * 
     * @param serviceId Service ID (e.g. YouTube)
     * @param url Video URL
     * @return Single emitting list of stream URLs
     */
    public static Single<VideoItem> getAllStreamUrls(final int serviceId, final String url) {
        return getStreamInfo(serviceId, url, false)
            .map(streamInfo -> {
                VideoItem item = new VideoItem();
                
                List<VideoStream> vs = new ArrayList<>();
                List<AudioStream> as = new ArrayList<>();
            
                for(VideoStream stream : streamInfo.getVideoStreams()){
                    vs.add(stream);
                }
            
                for(VideoStream stream : streamInfo.getVideoOnlyStreams()){
                    vs.add(stream);
                }
                
                for(AudioStream stream : streamInfo.getAudioStreams()){
                    as.add(stream);
                }
                
                
                item.setTitle(streamInfo.getName());
                item.setUploader(streamInfo.getUploaderName());
                item.setThumbnails(streamInfo.getThumbnails());
                item.setDuration(streamInfo.getDuration());
                item.setViewCount(streamInfo.getLikeCount());
                item.setDescription(streamInfo.getDescription().toString());
                item.setVideoStreams(vs);
                item.setAudioStreams(as);
                item.setUploadDate(streamInfo.getUploadDate().toString());
                
                return item;
            })
            .subscribeOn(Schedulers.io());
    }

    /**
     * Get sorted video streams by quality (highest first)
     * 
     * @param serviceId Service ID (e.g. YouTube)
     * @param url Video URL
     * @return Single emitting sorted list of video streams
     */
    public static Single<List<VideoStream>> getSortedVideoStreams(final int serviceId, final String url) {
        return getAllVideoStreams(serviceId, url)
            .map(streams -> {
                Collections.sort(streams, (s1, s2) -> 
                    Integer.compare(s2.getHeight(), s1.getHeight()));
                return streams;
            });
    }

    /**
     * Get sorted audio streams by bitrate (highest first)
     * 
     * @param serviceId Service ID (e.g. YouTube)
     * @param url Video URL
     * @return Single emitting sorted list of audio streams
     */
    public static Single<List<AudioStream>> getSortedAudioStreams(final int serviceId, final String url) {
        return getAllAudioStreams(serviceId, url)
            .map(streams -> {
                Collections.sort(streams, (s1, s2) -> 
                    Integer.compare(s2.getAverageBitrate(), s1.getAverageBitrate()));
                return streams;
            });
    }
}