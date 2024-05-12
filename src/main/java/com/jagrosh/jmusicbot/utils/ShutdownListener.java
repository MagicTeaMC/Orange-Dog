package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.NowplayingHandler;
import net.dv8tion.jda.api.JDA;

import java.util.Scanner;

public class ShutdownListener extends Thread {

    private volatile boolean running = true;

    private JDA jda;
    private NowplayingHandler nowplaying;

    public ShutdownListener() {
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            String input = scanner.nextLine();
            if ("stop".equalsIgnoreCase(input)) {
                System.out.println("Stopping bot...");
                if (jda != null) {
                    jda.getGuilds().stream().forEach(g ->
                    {
                        g.getAudioManager().closeAudioConnection();
                        AudioHandler ah = (AudioHandler) g.getAudioManager().getSendingHandler();
                        if (ah != null) {
                            ah.stopAndClear();
                            ah.getPlayer().destroy();
                            nowplaying.updateTopic(g.getIdLong(), ah, true);
                        }
                    });
                }
                if (jda != null) {
                    jda.shutdown();
                }
                System.exit(0);
            }
        }
        scanner.close();
    }

    public void shutdown() {
        running = false;
    }
}
