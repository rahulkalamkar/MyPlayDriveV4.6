# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/hungama2/Android softwares/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-optimizationpasses 5

-keepattributes InnerClasses
-keepattributes Signature

-keep public class com.google.android.exoplayer.CodecCounters
-keep public class com.google.android.exoplayer.DummyTrackRenderer
-keep public class com.google.android.exoplayer.ExoPlayer{ *; }
-keep public class com.google.android.exoplayer.ExoPlayer$Listener{ *; }
-keep public class com.google.android.exoplayer.ExoPlayer$Factory{ *; }
-keep public class com.google.android.exoplayer.ExoPlaybackException
-keep public class com.google.android.exoplayer.MediaCodecAudioTrackRenderer{ *; }
-keep public class com.google.android.exoplayer.MediaCodecAudioTrackRenderer$EventListener{ *; }
-keep public class com.google.android.exoplayer.MediaCodecTrackRenderer
-keep public class com.google.android.exoplayer.MediaCodecTrackRenderer$EventListener{ *; }
-keep public class com.google.android.exoplayer.MediaCodecTrackRenderer$DecoderInitializationException{ *; }
-keep public class com.google.android.exoplayer.MediaCodecVideoTrackRenderer{ *; }
-keep public class com.google.android.exoplayer.MediaCodecVideoTrackRenderer$EventListener{ *; }
-keep public class com.google.android.exoplayer.MediaCodecUtil$DecoderQueryException{ *; }
-keep public class com.google.android.exoplayer.MediaFormat
-keep public class com.google.android.exoplayer.TimeRange
-keep public class com.google.android.exoplayer.TrackRenderer
-keep public class com.google.android.exoplayer.DefaultLoadControl{ *; }
-keep public class com.google.android.exoplayer.LoadControl

-keep public class com.google.android.exoplayer.dash.DashChunkSource
-keep public class com.google.android.exoplayer.dash.DashChunkSource$EventListener{ *; }
-keep public class com.google.android.exoplayer.dash.DefaultDashTrackSelector{ *; }
-keep public class com.google.android.exoplayer.dash.mpd.AdaptationSet{ *; }
-keep public class com.google.android.exoplayer.dash.mpd.MediaPresentationDescription{ *; }
-keep public class com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser
-keep public class com.google.android.exoplayer.dash.mpd.Period{ *; }
-keep public class com.google.android.exoplayer.dash.mpd.UtcTimingElement
-keep public class com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver{ *; }
-keep public interface com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver$UtcTimingCallback{ *; }

-keep public class com.google.android.exoplayer.drm.MediaDrmCallback
-keep public class com.google.android.exoplayer.drm.UnsupportedDrmException{ *; }
-keep public class com.google.android.exoplayer.drm.StreamingDrmSessionManager{ *; }
-keep public class com.google.android.exoplayer.drm.StreamingDrmSessionManager$EventListener{ *; }
-keep public class com.google.android.exoplayer.drm.DrmSessionManager

-keep public class com.google.android.exoplayer.metadata.Id3Parser
-keep public class com.google.android.exoplayer.metadata.GeobMetadata{ *; }
-keep public class com.google.android.exoplayer.metadata.PrivMetadata{ *; }
-keep public class com.google.android.exoplayer.metadata.TxxxMetadata{ *; }
-keep public class com.google.android.exoplayer.metadata.MetadataTrackRenderer{ *; }
-keep public interface com.google.android.exoplayer.metadata.MetadataTrackRenderer$MetadataRenderer{
    *;
}

-keep public class com.google.android.exoplayer.util.DebugTextViewHelper
-keep public interface com.google.android.exoplayer.util.DebugTextViewHelper$Provider{ *; }
-keep public class com.google.android.exoplayer.util.PlayerControl{ *; }
-keep public class com.google.android.exoplayer.util.Util{ *; }
-keep public class com.google.android.exoplayer.util.ManifestFetcher{ *; }
-keep public interface com.google.android.exoplayer.util.ManifestFetcher$ManifestCallback{ *; }


-keep public class com.google.android.exoplayer.audio.AudioTrack
-keep public class com.google.android.exoplayer.audio.AudioTrack$InitializationException{ *; }
-keep public class com.google.android.exoplayer.audio.AudioTrack$WriteException{ *; }
-keep public class com.google.android.exoplayer.audio.AudioCapabilities{ *; }


-keep public class com.google.android.exoplayer.chunk.ChunkSampleSource{ *; }
-keep public class com.google.android.exoplayer.chunk.ChunkSampleSource$EventListener{ *; }
-keep public class com.google.android.exoplayer.chunk.Format
-keep public class com.google.android.exoplayer.chunk.VideoFormatSelectorUtil{ *; }
-keep public class com.google.android.exoplayer.chunk.ChunkSource
-keep public interface com.google.android.exoplayer.chunk.BaseChunkSampleSourceEventListener{ *; }
-keep public class com.google.android.exoplayer.chunk.FormatEvaluator$AdaptiveEvaluator{ *; }

-keep public class com.google.android.exoplayer.hls.HlsChunkSource{ *; }
-keep public class com.google.android.exoplayer.hls.HlsMasterPlaylist{ *; }
-keep public class com.google.android.exoplayer.hls.HlsPlaylist
-keep public class com.google.android.exoplayer.hls.HlsPlaylistParser
-keep public class com.google.android.exoplayer.hls.HlsSampleSource{ *; }
-keep public class com.google.android.exoplayer.hls.HlsSampleSource$EventListener{ *; }

-keep public class com.google.android.exoplayer.text.Cue
-keep public interface com.google.android.exoplayer.text.TextRenderer{ *; }
-keep public class com.google.android.exoplayer.text.TextTrackRenderer{ *; }
-keep public class com.google.android.exoplayer.text.eia608.Eia608TrackRenderer{ *; }

-keep public class com.google.android.exoplayer.upstream.Allocator
-keep public class com.google.android.exoplayer.upstream.BandwidthMeter
-keep public class com.google.android.exoplayer.upstream.DataSource
-keep public class com.google.android.exoplayer.upstream.DefaultAllocator{ *; }
-keep public class com.google.android.exoplayer.upstream.DefaultUriDataSource{ *; }
-keep public class com.google.android.exoplayer.upstream.DefaultBandwidthMeter{ *; }
-keep public interface com.google.android.exoplayer.upstream.BandwidthMeter$EventListener{ *; }
-keep public class com.google.android.exoplayer.upstream.UriDataSource
-keep public class com.google.android.exoplayer.upstream.DefaultHttpDataSource

-keep public class com.google.android.exoplayer.smoothstreaming.DefaultSmoothStreamingTrackSelector
-keep public class com.google.android.exoplayer.smoothstreaming.SmoothStreamingChunkSource
-keep public class com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest
-keep public class com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest$StreamElement
-keep public class com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifestParser

-keep public class com.google.android.exoplayer.extractor.Extractor
-keep public class com.google.android.exoplayer.extractor.ExtractorSampleSource{ *; }

