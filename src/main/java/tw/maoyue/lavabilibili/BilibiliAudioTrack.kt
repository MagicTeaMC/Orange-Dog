package tw.maoyue.lavabilibili

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import org.apache.http.client.methods.HttpGet
import tw.maoyue.lavabilibili.BilibiliAudioSourceManager.Companion.BASE_URL
import java.net.URI

class BilibiliAudioTrack(audioTrackInfo: AudioTrackInfo, val type: TrackType, val /*can be bvid or sid*/ id: String, val cid: Long?, private val sourceManager: BilibiliAudioSourceManager) : DelegatedAudioTrack(audioTrackInfo) {
    override fun process(executor: LocalAudioTrackExecutor) {
        val stream = PersistentHttpStream(sourceManager.httpInterface, URI(getPlaybackURL()), null)
        processDelegate(MpegAudioTrack(trackInfo, stream), executor)
    }

    private fun getPlaybackURL(): String = when (type) {
        TrackType.VIDEO -> {
            val response = sourceManager.httpInterface.execute(HttpGet("${BASE_URL}x/player/playurl?bvid=$id&cid=$cid&fnval=16"))
            val responseJson = JsonBrowser.parse(response.entity.content)

            responseJson
                    .get("data")
                    .get("dash")
                    .get("audio")
                    .values()
                    // find the highest quality possible
                    .sortedByDescending {
                        it.get("id").`as`(Int::class.java)
                    }[0]
                    .get("baseUrl").`as`(String::class.java)
        }
        TrackType.AUDIO -> {
            val response = sourceManager.httpInterface.execute(HttpGet("${BASE_URL}audio/music-service-c/web/url?sid=$id&privilege=2&quality=2"))
            val responseJson = JsonBrowser.parse(response.entity.content)

            responseJson
                    .get("data")
                    .get("cdns")
                    .values()[0].`as`(String::class.java)
        }
    }

    override fun makeShallowClone(): AudioTrack {
        return BilibiliAudioTrack(trackInfo, type, id, cid, sourceManager)
    }

    override fun getSourceManager(): AudioSourceManager {
        return sourceManager
    }

    enum class TrackType {
        VIDEO,
        AUDIO
    }
}