/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.audio;

import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import me.allvaa.lpsources.bilibili.BilibiliAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.maoyue.lavaodysee.OdyseeAudioSourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.function.Function;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlayerManager extends DefaultAudioPlayerManager {
    private final Bot bot;
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);
    LyricsManager lyricsManager = new LyricsManager();

    public PlayerManager(Bot bot) {
        this.bot = bot;
    }

    String clientId = "f5c61bf239ec41e18756db119436c418";
    String clientSecret = "2c2e6b4926204f2cb1ad0b2d8c2fca51";
    String spDc = "AQASquIDa6JW3xiRbm3tvM9JFXt8gglWPHelVa89aAIbRp5JNVV7BX2RDF0dU5fb27Ei6WRFCcnHO9gZDNAUYhSjd5n_PQ0nk-p1dPOmMdehfSXZOsYHpl5heMkfjtyJ7qlA_LYbOvqb07gajDdFrcKkTi2BZUXYadb3OWezd9CJJJ7gWWmeQbtO0h0P6i_KStVmBVAGGTGOaDQiBROW8mF5Ljoc";

    public void init() {
        TransformativeAudioSourceManager.createTransforms(bot.getConfig().getTransforms()).forEach(this::registerSourceManager);

        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(/*allowSearch:*/ true, new Client[] {
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new WebEmbeddedWithThumbnail(),
                new AndroidTestsuiteWithThumbnail(),
                new TvHtml5EmbeddedWithThumbnail(),
                new AndroidMusicWithThumbnail(),
                new IosWithThumbnail()
        });

        String token = null;
        try
        {
            token = Files.readString(OtherUtil.getPath("youtubetoken.txt"));
        }
        catch (NoSuchFileException e)
        {
            /* ignored */
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to read YouTube OAuth2 token file: {}",e.getMessage());
        }
        LOGGER.debug("Using YouTube OAuth2 refresh token {}", token);
        try
        {
            yt.useOauth2(token, false);
        }
        catch (Exception e)
        {
            LOGGER.warn("Failed to authorize with YouTube. If this issue persists, delete the youtubetoken.txt file to reauthorize.", e);
        }

        Function<Void, AudioPlayerManager> audioPlayerManagerFunction = (v) -> this;

        SpotifySourceManager spotifyly = new SpotifySourceManager(clientId, clientSecret, spDc, "us", audioPlayerManagerFunction, new DefaultMirroringAudioTrackResolver(new String[]{"ytsearch:"}));

        lyricsManager.registerLyricsManager(spotifyly);

        registerSourceManager(yt);
        registerSourceManager(new SpotifySourceManager(null, clientId, clientSecret, "us", this));
        registerSourceManager(new BilibiliAudioSourceManager());
        registerSourceManager(new OdyseeAudioSourceManager());
        registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        registerSourceManager(new YandexMusicSourceManager("y0_AgAAAABEEHcTAAG8XgAAAAEKQo81AACsCV7u0e1EfoQw5NEaIUX--zquxQ"));
        registerSourceManager(new DeezerAudioSourceManager("a25a28ccd212536fed8e6002f51787c569338909e3f9a2e364dd41b26d0bdd003282aa658570852c1114bf675592abbcc6d65dbbe6f3791137095d008f3600bfce269eccb6133bb2e1c240a2311603a78d7b0524ac61629a575489fcf0be67d8"));
        registerSourceManager(new BandcampAudioSourceManager());
        registerSourceManager(new VimeoAudioSourceManager());
        registerSourceManager(new TwitchStreamAudioSourceManager());
        registerSourceManager(new BeamAudioSourceManager());
        registerSourceManager(new GetyarnAudioSourceManager());
        registerSourceManager(new NicoAudioSourceManager());
        registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        AudioSourceManagers.registerLocalSource(this);

        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public LyricsManager getLyricsManager() {
        return lyricsManager;
    }

    public Bot getBot() {
        return bot;
    }

    public boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }

    public AudioHandler setUpHandler(Guild guild) {
        AudioHandler handler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        } else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}
