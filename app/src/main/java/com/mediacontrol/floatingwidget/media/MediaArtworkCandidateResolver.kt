package sw2.io.mediafloat.media

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.net.Uri
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaArtworkSource

internal class MediaArtworkCandidateResolver(
    private val contentResolver: ContentResolver
) {
    fun resolve(controller: MediaController, notificationArtworkByPackage: Map<String, MediaArtwork.BitmapSource>): List<MediaArtwork> {
        val metadata = controller.metadata
        return buildArtworkCandidates(
            metadataDisplayIconUri = resolveArtworkUriCandidate(metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI), MediaArtworkSource.MetadataDisplayIconUri),
            metadataArtUri = resolveArtworkUriCandidate(metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI), MediaArtworkSource.MetadataArtUri),
            metadataAlbumArtUri = resolveArtworkUriCandidate(metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI), MediaArtworkSource.MetadataAlbumArtUri),
            metadataDisplayIconBitmap = resolveArtworkBitmapCandidate(metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON), MediaArtworkSource.MetadataDisplayIconBitmap),
            metadataArtBitmap = resolveArtworkBitmapCandidate(metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART), MediaArtworkSource.MetadataArtBitmap),
            metadataAlbumArtBitmap = resolveArtworkBitmapCandidate(metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART), MediaArtworkSource.MetadataAlbumArtBitmap),
            notificationLargeIcon = notificationArtworkByPackage[controller.packageName]
        )
    }

    private fun resolveArtworkUriCandidate(rawUri: String?, source: MediaArtworkSource): MediaArtwork.UriSource? {
        val normalizedUri = rawUri?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val artworkBounds = runCatching {
            contentResolver.openInputStream(Uri.parse(normalizedUri))?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)
                if (options.outWidth > 0 && options.outHeight > 0) options.outWidth to options.outHeight else null
            }
        }.getOrNull() ?: return null
        return MediaArtwork.UriSource(source = source, uri = normalizedUri, widthPx = artworkBounds.first, heightPx = artworkBounds.second)
    }

    private fun resolveArtworkBitmapCandidate(bitmap: Bitmap?, source: MediaArtworkSource): MediaArtwork.BitmapSource? {
        val resolvedBitmap = bitmap?.takeIf { it.width > 0 && it.height > 0 } ?: return null
        return MediaArtwork.BitmapSource(source = source, bitmap = resolvedBitmap, widthPx = resolvedBitmap.width, heightPx = resolvedBitmap.height)
    }

    companion object {
        fun buildArtworkCandidates(
            metadataDisplayIconUri: MediaArtwork.UriSource?,
            metadataArtUri: MediaArtwork.UriSource?,
            metadataAlbumArtUri: MediaArtwork.UriSource?,
            metadataDisplayIconBitmap: MediaArtwork.BitmapSource?,
            metadataArtBitmap: MediaArtwork.BitmapSource?,
            metadataAlbumArtBitmap: MediaArtwork.BitmapSource?,
            notificationLargeIcon: MediaArtwork.BitmapSource?
        ): List<MediaArtwork> {
            return listOfNotNull(
                metadataDisplayIconUri,
                metadataArtUri,
                metadataAlbumArtUri,
                metadataDisplayIconBitmap,
                metadataArtBitmap,
                metadataAlbumArtBitmap,
                notificationLargeIcon
            )
        }
    }
}
