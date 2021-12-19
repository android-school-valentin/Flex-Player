package ru.valentine.flexplayer.ui.album.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.FragmentAlbumDetailBinding
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.ui.track.TrackAdapter
import timber.log.Timber

class AlbumDetailFragment : Fragment() {

    private val viewModel: AlbumDetailViewModel by viewModel()
    private val args: AlbumDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setAlbumId(args.albumId)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Timber.i("VIEW CREATED")

        val binding: FragmentAlbumDetailBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_album_detail,
                container,
                false
        )

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val adapter = TrackAdapter(MediaItemClickListener {
            viewModel.playTrack(it)
        })

        binding.albumTrackList.adapter = adapter

        viewModel.album.observe(viewLifecycleOwner) { album ->
            binding.title.text = album.description.title
            binding.albumArtistName.text = album.description.subtitle
            Glide.with(binding.albumArt)
                .load(album.description.iconUri)
                .placeholder(R.drawable.ic_music_album)
                .into(binding.albumArt)
        }

        viewModel.albumTracks.observe(viewLifecycleOwner) { tracks ->
            adapter.submitList(tracks)
        }

        return binding.root

    }

}