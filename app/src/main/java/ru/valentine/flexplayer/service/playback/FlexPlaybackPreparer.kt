package ru.valentine.flexplayer.service.playback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ShuffleOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.MediaId.Builder.CATEGORY_ALL
import ru.valentine.flexplayer.core.media.MediaId.Builder.TYPE_TRACKS
import ru.valentine.flexplayer.core.media.parse
import ru.valentine.flexplayer.service.browser.BrowserTree
import ru.valentine.flexplayer.service.browser.tree.AudioTrack
import kotlin.random.Random

class FlexPlaybackPreparer(
        private val scope: CoroutineScope,
        private val player: ExoPlayer,
        private val browserTree: BrowserTree,
) : MediaSessionConnector.PlaybackPreparer {


    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE or
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
    }

    override fun onPrepare(playWhenReady: Boolean) {
        //val reloadStrategy = settings.queueReload
        prepareFromMediaId(
                mediaId = MediaId(TYPE_TRACKS, CATEGORY_ALL),//settings.lastQueueMediaId ?: MediaId(TYPE_TRACKS, CATEGORY_ALL),
                //startPlaybackPosition = when {
                //    reloadStrategy.reloadTrack -> settings.lastQueueIndex
                //    else -> 0
                //},
                startPlaybackPosition = 0,
                //playbackPosition = when {
                //    reloadStrategy.reloadPosition -> settings.lastPlayedPosition
                //    else -> C.TIME_UNSET
                //},
                playbackPosition = C.TIME_UNSET,
                playWhenReady = playWhenReady
        )
    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        val queueMediaId = mediaId.parse()
        //settings.lastQueueMediaId = queueMediaId

        prepareFromMediaId(queueMediaId, C.POSITION_UNSET, C.TIME_UNSET, playWhenReady)
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
        throw UnsupportedOperationException()
    }

    private suspend fun loadPlayableChildrenOf(parentId: MediaId): List<AudioTrack> = try {
        val children = browserTree.getChildren(parentId).first()
        children.filterIsInstance<AudioTrack>()

    } catch (e: Exception) {
        emptyList()
    }

    private fun prepareFromMediaId(
            mediaId: MediaId,
            startPlaybackPosition: Int,
            playbackPosition: Long,
            playWhenReady: Boolean
    ) = scope.launch(Dispatchers.Default) {
        val parentId = mediaId.copy(track = null)

        val playQueue = loadPlayableChildrenOf(parentId)
        val firstIndex = when {
            mediaId.track != null -> playQueue.indexOfFirst { it.id == mediaId }
            else -> C.POSITION_UNSET
        }

        preparePlayer(
                playQueue,
                firstShuffledIndex = firstIndex,
                startIndex = startPlaybackPosition,
                playbackPosition = playbackPosition,
                playWhenReady
        )
    }

    /**
     * Prepare playback of a given [playQueue]
     * and start playing the index at [startIndex] when ready.
     *
     * @param playQueue The items to be played. All media should be playable and have a media uri.
     * @param firstShuffledIndex The index of the item that should be the first when playing shuffled.
     * This should be a valid index in [playQueue], otherwise an index is chosen randomly.
     * @param startIndex The index of the item that should be played when the player is ready.
     * This should be a valid index in [playQueue],
     * otherwise playback will be set to start at the first index in the queue (shuffled or not).
     */
    private suspend fun preparePlayer(
            playQueue: List<AudioTrack>,
            firstShuffledIndex: Int,
            startIndex: Int,
            playbackPosition: Long,
            playWhenReady: Boolean
    ) {
        if (playQueue.isNotEmpty()) withContext(Dispatchers.Main) {
            val queueItems = playQueue.map { track ->
                MediaItem.Builder()
                        .setMediaId(track.id.encoded)
                        .setUri(track.mediaUri)
                        .setTag(track)
                        .build()
            }

            // Defines a shuffle order for the loaded media sources that is predictable.
            // The random seed is built from an unique queue identifier,
            // so that queue can be rebuilt with the same order.
            val randomSeed = 12L//settings.queueIdentifier

            // Create a shuffle order that starts with the track at the specified "first index".
            // If that index is invalid, just randomly shuffle the play queue.
            val predictableShuffleOrder = if (firstShuffledIndex in playQueue.indices) {
                val shuffledIndices =
                        createShuffledIndices(firstShuffledIndex, playQueue.size, randomSeed)
                ShuffleOrder.DefaultShuffleOrder(shuffledIndices, randomSeed)
            } else {
                ShuffleOrder.DefaultShuffleOrder(playQueue.size, randomSeed)
            }

            // Start playback at a given position if specified, otherwise at first shuffled index.
            val targetPlaybackPosition = when (startIndex) {
                in playQueue.indices -> startIndex
                else -> predictableShuffleOrder.firstIndex
            }

            player.setMediaItems(queueItems, targetPlaybackPosition, playbackPosition)
            player.setShuffleOrder(predictableShuffleOrder)
            player.prepare()

            player.playWhenReady = playWhenReady
        }
    }

    private fun createShuffledIndices(firstIndex: Int, length: Int, randomSeed: Long): IntArray {
        val shuffled = IntArray(length)

        if (length > 0) {
            val random = Random(randomSeed)
            shuffled[0] = firstIndex

            for (i in 1..firstIndex) {
                val swapIndex = random.nextInt(1, i + 1)
                shuffled[i] = shuffled[swapIndex]
                shuffled[swapIndex] = i - 1
            }

            for (i in (firstIndex + 1) until length) {
                val swapIndex = random.nextInt(1, i + 1)
                shuffled[i] = shuffled[swapIndex]
                shuffled[swapIndex] = i
            }
        }

        return shuffled
    }

}