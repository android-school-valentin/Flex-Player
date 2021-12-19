package ru.valentine.flexplayer.ui.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.FragmentMainBinding
import ru.valentine.flexplayer.ui.album.AlbumFragment
import ru.valentine.flexplayer.ui.recommendation.RecommendationFragment
import ru.valentine.flexplayer.ui.saved.SavedFragment
import ru.valentine.flexplayer.ui.track.TrackFragment
import timber.log.Timber

class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )

        binding.toolbar.run {
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        val pagerAdapter = TabAdapter(this)
        binding.fragmentPager.adapter = pagerAdapter
        binding.fragmentPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabHost, binding.fragmentPager, false) { tab, position ->
            tab.icon = pagerAdapter.getIcon(position)
            tab.contentDescription = pagerAdapter.getTitle(position)
        }.attach()

        mainViewModel.tracks.observe(viewLifecycleOwner) {
            Timber.i(it.toString())
        }

        return binding.root
    }

    private class TabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        private val context = fragment.requireContext()

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> TrackFragment()
            1 -> AlbumFragment()
            2 -> SavedFragment()
            3 -> RecommendationFragment()
            else -> throw IllegalStateException("No fragment for this position")
        }

        fun getTitle(position: Int): String? = when (position) {
            0 -> context.getString(R.string.label_tracks)
            1 -> context.getString(R.string.label_albums)
            2 -> context.getString(R.string.label_saved)
            3 -> context.getString(R.string.label_recommendations)
            else -> null
        }

        fun getIcon(position: Int): Drawable? = when (position) {
            0 -> ContextCompat.getDrawable(context, R.drawable.ic_music_note)
            1 -> ContextCompat.getDrawable(context, R.drawable.ic_music_album)
            2 -> ContextCompat.getDrawable(context, R.drawable.ic_save)
            3 -> ContextCompat.getDrawable(context, R.drawable.ic_favorite)
            else -> null
        }

    }

}