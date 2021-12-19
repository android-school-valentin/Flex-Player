package ru.valentine.flexplayer.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.FragmentAlbumBinding
import ru.valentine.flexplayer.ui.base.LoadRequest
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.ui.main.MainFragmentDirections
import ru.valentine.flexplayer.ui.main.MainViewModel
import timber.log.Timber


class AlbumFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding: FragmentAlbumBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_album,
                container,
                false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = AlbumAdapter(MediaItemClickListener {
            findNavController().navigate(MainFragmentDirections.toAlbumDetailFragment(it.mediaId!!))
        })

        binding.albumGrid.adapter = adapter

        binding.albumGrid.layoutManager = GridLayoutManager(requireContext(), 2)

        mainViewModel.albums.observe(viewLifecycleOwner) { albumRequest ->
            when (albumRequest) {
                is LoadRequest.Pending -> binding.progressIndicator.isVisible = true
                is LoadRequest.Success -> {
                    Timber.e(albumRequest.data.toString())
                    binding.progressIndicator.isVisible = false
                    adapter.submitList(albumRequest.data)
                    binding.emptyViewGroup.isVisible = albumRequest.data.isEmpty()
                }
                is LoadRequest.Error -> {
                    binding.progressIndicator.isVisible = true
                    adapter.submitList(emptyList())
                    binding.emptyViewGroup.isVisible = true
                }
            }
        }

        return binding.root
    }

}