package ru.valentine.flexplayer.ui.player

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.FragmentPlayerBinding
import ru.valentine.flexplayer.service.playback.RepeatMode
import ru.valentine.flexplayer.util.SeekbarProgressUpdater
import timber.log.Timber


class PlayerFragment : Fragment() {

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var seekbarUpdater: SeekbarProgressUpdater
    private lateinit var glideRequest: RequestBuilder<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glideRequest = Glide.with(this).asDrawable()
            .error(R.drawable.ic_music_note)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentPlayerBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_player,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        seekbarUpdater = SeekbarProgressUpdater(
            binding.seekBar,
            binding.bookmark,
            binding.duration
        ) { position ->
            viewModel.seekTo(position)
        }

        val clickHandler = ClickListener()
        binding.playPause.setOnClickListener(clickHandler)
        binding.repeat.setOnClickListener(clickHandler)
        binding.shuffle.setOnClickListener(clickHandler)
        binding.next.setOnClickListener(clickHandler)
        binding.previous.setOnClickListener(clickHandler)

        viewModel.state.observe(viewLifecycleOwner) {
            onPlayerStateChanged(it, binding)
        }
        return binding.root
    }

    private fun onPlayerStateChanged(state: PlayerState, binding: FragmentPlayerBinding) {
        binding.playPause.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        binding.shuffle.setImageLevel(if (state.shuffleModeEnabled) 1 else 0)
        binding.repeat.setImageLevel(
            when (state.repeatMode) {
                RepeatMode.DISABLED -> 0
                RepeatMode.ALL -> 1
                RepeatMode.ONE -> 2
            }
        )

        if (state.currentTrack != null) {
            val media = state.currentTrack

            // Set the title and the description.
            binding.title.text = media.title
            binding.subtitle.text = media.artist

            // Update progress and labels
            seekbarUpdater.update(
                state.position,
                media.duration,
                state.lastPositionUpdateTime,
                state.isPlaying
            )

            // Update artwork.
            glideRequest.load(media.artworkUri).into(binding.albumArt)

        } else {
            binding.title.text = null
            binding.subtitle.text = null

            seekbarUpdater.update(0L, 0L, state.lastPositionUpdateTime, false)
        }

    }

    private inner class ClickListener : View.OnClickListener {

        override fun onClick(view: View) {
            when (view.id) {
                R.id.play_pause -> viewModel.togglePlayPause()
                R.id.previous -> viewModel.skipToPrevious()
                R.id.next -> viewModel.skipToNext()
                R.id.shuffle -> viewModel.toggleShuffleMode()
                R.id.repeat -> viewModel.toggleRepeatMode()
                else -> Timber.w("Unhandled click event for View with id: %s", view.id)
            }
        }
    }

}