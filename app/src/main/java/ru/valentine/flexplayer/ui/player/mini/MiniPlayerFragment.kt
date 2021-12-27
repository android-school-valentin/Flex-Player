package ru.valentine.flexplayer.ui.player.mini

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
import ru.valentine.flexplayer.databinding.FragmentMiniPlayerBinding
import timber.log.Timber


class MiniPlayerFragment : Fragment() {

    private val viewModel: MiniPlayerViewModel by viewModel()
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
    ): View? {
        val binding: FragmentMiniPlayerBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_player,
            container,
            false
        )

        val clickHandler = ClickListener()
        binding.miniPlayPause.setOnClickListener(clickHandler)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.state.observe(viewLifecycleOwner) {
            onPlayerStateChanged(it, binding)
        }

        return binding.root
    }

    private fun onPlayerStateChanged(state: MiniPlayerState, binding: FragmentMiniPlayerBinding) {
        binding.miniPlayPause.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        if (state.currentTrack != null) {
            val media = state.currentTrack
            // Set the title and the description.
            binding.miniTitle.text = media.title
            binding.miniSubtitle.text = media.artist
            glideRequest.load(media.albumArt).into(binding.miniAlbumArt)

        } else {
            binding.miniTitle.text = null
        }
    }

    private inner class ClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.mini_play_pause -> viewModel.togglePlayPause()
                else -> Timber.w("Unhandled click event for View with id: %s", view.id)
            }
        }
    }

}