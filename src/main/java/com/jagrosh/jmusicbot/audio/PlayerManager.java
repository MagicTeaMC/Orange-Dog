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
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.jagrosh.jmusicbot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.YTDLPSourceManager;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.maoyue.lavabilibili.BilibiliAudioSourceManager;
import tw.maoyue.lavaodysee.OdyseeAudioSourceManager;

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

    public void init() {
        TransformativeAudioSourceManager.createTransforms(bot.getConfig().getTransforms()).forEach(this::registerSourceManager);

        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(/*allowSearch:*/ true, new Client[] {
                new MusicWithThumbnail(),
                new AndroidVrWithThumbnail(),
                new WebWithThumbnail(),
                new MWebWithThumbnail(),
                new WebEmbeddedWithThumbnail(),
                new AndroidMusicWithThumbnail(),
                new Tv(),
                new TvHtml5EmbeddedWithThumbnail(),
                new IosWithThumbnail()
        });

        Function<Void, AudioPlayerManager> audioPlayerManagerFunction = (v) -> this;

        SpotifySourceManager spotifyly = new SpotifySourceManager(bot.getConfig().getSpotifyClientId(), bot.getConfig().getSpotifyClientSecret(), bot.getConfig().getSpDc(), "us", audioPlayerManagerFunction, new DefaultMirroringAudioTrackResolver(new String[]{"ytsearch:"}));

        lyricsManager.registerLyricsManager(spotifyly);

        registerSourceManager(yt);
        registerSourceManager(new SpotifySourceManager(null, bot.getConfig().getSpotifyClientId(), bot.getConfig().getSpotifyClientSecret(), "us", this));
        registerSourceManager(new BilibiliAudioSourceManager());
        registerSourceManager(new OdyseeAudioSourceManager());
        registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        registerSourceManager(new YandexMusicSourceManager("y0_AgAAAABEEHcTAAG8XgAAAAEKQo81AACsCV7u0e1EfoQw5NEaIUX--zquxQ"));
        registerSourceManager(new BandcampAudioSourceManager());
        registerSourceManager(new VimeoAudioSourceManager());
        registerSourceManager(new TwitchStreamAudioSourceManager());
        registerSourceManager(new BeamAudioSourceManager());
        registerSourceManager(new GetyarnAudioSourceManager());
        registerSourceManager(new NicoAudioSourceManager());
        registerSourceManager(new YTDLPSourceManager());

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
