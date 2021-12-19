package ru.valentine.flexplayer.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bottomSheet: BottomSheetBehavior<*>

    private val bottomSheetCollapsingCallback = BottomSheetCollapsingCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        bottomSheet = BottomSheetBehavior.from(binding.playerContainer)
    }

    override fun onResume() {
        super.onResume()
        bottomSheet.addBottomSheetCallback(bottomSheetCollapsingCallback)
    }

    override fun onPause() {
        bottomSheet.removeBottomSheetCallback(bottomSheetCollapsingCallback)
        super.onPause()
    }

    private inner class BottomSheetCollapsingCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = 1 - slideOffset
            binding.fragmentMiniPlayer.alpha = alpha
            binding.fragmentMiniPlayer.visibility =
                if (alpha == 0f) View.INVISIBLE else View.VISIBLE
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
    }

}