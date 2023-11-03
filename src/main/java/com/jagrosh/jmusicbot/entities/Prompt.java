/*
 * Copyright 2018 John Grosh (jagrosh)
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
package com.jagrosh.jmusicbot.entities;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Prompt
{
    private final String title;
    private final String noguiMessage;

    private final boolean nogui = true;
    private boolean noprompt;
    private Scanner scanner;

    public Prompt(String title)
    {
        this(title, null);
    }

    public Prompt(String title, String noguiMessage)
    {
        this(title, noguiMessage, "true".equalsIgnoreCase(System.getProperty("noprompt")));
    }

    private Prompt(String title, String noguiMessage, boolean noprompt)
    {
        this.title = title;
        this.noguiMessage = noguiMessage == null ? "無法開啟視窗，如果您的伺服器或電腦沒有螢幕，請使用 -Dnogui=true 來啟動機器人" : noguiMessage;
        this.noprompt = noprompt;
    }

    public boolean isNoGUI()
    {
        return nogui;
    }

    public void alert(Level level, String context, String message)
    {
        Logger log = LoggerFactory.getLogger(context);
        switch(level)
        {
            case INFO:
                log.info(message);
                break;
            case WARNING:
                log.warn(message);
                break;
            case ERROR:
                log.error(message);
                break;
            default:
                log.info(message);
                break;
        }
    }

    public String prompt(String content)
    {
        if(noprompt)
            return null;
        if(scanner==null)
            scanner = new Scanner(System.in);
        try
        {
            System.out.println(content);
            if(scanner.hasNextLine())
                return scanner.nextLine();
            return null;
        }
        catch(Exception e)
        {
            alert(Level.ERROR, title, "無法從命令列讀取輸入");
            e.printStackTrace();
            return null;
        }
    }

    public static enum Level
    {
        INFO, WARNING, ERROR;
    }
}