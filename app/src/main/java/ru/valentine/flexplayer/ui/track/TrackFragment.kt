package ru.valentine.flexplayer.ui.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.FragmentTrackBinding
import ru.valentine.flexplayer.ui.base.LoadRequest
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.ui.main.MainViewModel
import timber.log.Timber

class TrackFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding: FragmentTrackBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_track,
                container,
                false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = TrackAdapter(MediaItemClickListener {
            Timber.i("Clicked item ${it.mediaId}")
            mainViewModel.playMedia(it)
        })

        binding.trackList.adapter = adapter

        mainViewModel.tracks.observe(viewLifecycleOwner) { itemRequest ->
            when (itemRequest) {
                is LoadRequest.Pending -> binding.progressIndicator.isVisible = true
                is LoadRequest.Success -> {
                    binding.progressIndicator.isVisible = false
                    adapter.submitList(itemRequest.data)
                    binding.emptyViewGroup.isVisible = itemRequest.data.isEmpty()
                }
                is LoadRequest.Error -> {
                    binding.progressIndicator.isVisible = false
                    adapter.submitList(emptyList())
                    binding.emptyViewGroup.isVisible = true
                }
            }
        }

        return binding.root
    }

}