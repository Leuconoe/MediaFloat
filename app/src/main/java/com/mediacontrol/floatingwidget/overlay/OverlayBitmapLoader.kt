package sw2.io.mediafloat.overlay

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import sw2.io.mediafloat.model.MediaArtwork

internal class OverlayBitmapLoader(
    private val contentResolver: ContentResolver
) {
    fun load(artwork: MediaArtwork, targetSizePx: Int): Bitmap? {
        return when (artwork) {
            is MediaArtwork.BitmapSource -> artwork.bitmap?.centerCropSquare(targetSizePx)
            is MediaArtwork.UriSource -> decodeUriThumbnail(artwork.uri, targetSizePx)
        }
    }

    private fun decodeUriThumbnail(uri: String, targetSizePx: Int): Bitmap? {
        val parsedUri = Uri.parse(uri)
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(parsedUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: return null

        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            return null
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOptions, targetSizePx, targetSizePx)
        }
        val decodedBitmap = contentResolver.openInputStream(parsedUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        return decodedBitmap.centerCropSquare(targetSizePx)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, requestedWidth: Int, requestedHeight: Int): Int {
        var inSampleSize = 1
        var halfHeight = options.outHeight / 2
        var halfWidth = options.outWidth / 2
        while (halfHeight / inSampleSize >= requestedHeight && halfWidth / inSampleSize >= requestedWidth) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun Bitmap.centerCropSquare(targetSizePx: Int): Bitmap {
        val squareSize = minOf(width, height)
        val startX = ((width - squareSize) / 2).coerceAtLeast(0)
        val startY = ((height - squareSize) / 2).coerceAtLeast(0)
        val croppedBitmap = Bitmap.createBitmap(this, startX, startY, squareSize, squareSize)
        return if (croppedBitmap.width == targetSizePx && croppedBitmap.height == targetSizePx) {
            croppedBitmap
        } else {
            Bitmap.createScaledBitmap(croppedBitmap, targetSizePx, targetSizePx, true)
        }
    }
}
