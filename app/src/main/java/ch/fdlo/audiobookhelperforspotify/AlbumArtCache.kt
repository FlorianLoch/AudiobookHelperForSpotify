package ch.fdlo.audiobookhelperforspotify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import java.io.File

class AlbumArtCache(private val ctx: Context, private val spotifyRemote: SpotifyAppRemote) {
    fun fetchCoverForAlbum(imageURI: String): Deferred<Bitmap> {
        // the actual imageURI contains characters that would need to be escaped for constructing a filename with it
        val friendlyImageURI = Base64.encodeToString(imageURI.toByteArray(), Base64.NO_PADDING + Base64.NO_WRAP)

        val promise = CompletableDeferred<Bitmap>()
        val cacheFile = File(ctx.cacheDir, friendlyImageURI)

        if (cacheFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
            promise.complete(bitmap)
        }
        else {
            val imageURICooked = ImageUri(imageURI)
            with (spotifyRemote.imagesApi.getImage(imageURICooked)) {
                setResultCallback {
                    promise.complete(it)

                    val ok = it.compress(Bitmap.CompressFormat.PNG, 100 /* n. a. with PNG */ , cacheFile.outputStream())

                    if (ok) {
                        Log.d("ABHfS", "Successfully wrote album art ($imageURI) to cache (${cacheFile.absolutePath})!")
                    } else {
                        Log.d("ABHfS", "Could NOT write album art ($imageURI) to cache!")
                    }
                }
                setErrorCallback {
                    Log.e("ABHfS", "Could not get cover art from Spotify!", it)

                    promise.completeExceptionally(it)
                }
                // TODO Handler error callback! Also check this at all other positions where we use the spotify api
            }
        }

        return promise
    }
}
