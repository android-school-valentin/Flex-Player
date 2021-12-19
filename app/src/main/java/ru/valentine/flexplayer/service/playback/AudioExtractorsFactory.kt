package ru.valentine.flexplayer.service.playback

import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.flac.FlacExtractor
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.extractor.ogg.OggExtractor
import com.google.android.exoplayer2.extractor.ts.Ac3Extractor
import com.google.android.exoplayer2.extractor.ts.AdtsExtractor
import com.google.android.exoplayer2.extractor.wav.WavExtractor

class AudioExtractorsFactory : ExtractorsFactory {

    override fun createExtractors() = arrayOf(
            // Most used audio file extensions .mp3 and .wav
            Mp3Extractor(),
            WavExtractor(),
            // .aac audio files
            AdtsExtractor(),
            // .ogg and .oga audio files
            OggExtractor(),
            // .ac3 (Dolby Digital) audio files
            Ac3Extractor(),
            // Apple .m4a (which is in fact the same format as MPEG-4)
            Mp4Extractor(),
            // .flac audio files for API 27+
            FlacExtractor()
    )
}