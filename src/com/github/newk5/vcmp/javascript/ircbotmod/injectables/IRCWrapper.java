package com.github.newk5.vcmp.javascript.ircbotmod.injectables;

import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ResultUndefined;
import com.github.newk5.vcmp.javascript.ircbotmod.entities.BotWrapper;
import com.github.newk5.vcmp.javascript.ircbotmod.listeners.IRCBotListener;
import static com.github.newk5.vcmp.javascript.plugin.internals.Runtime.console;
import io.alicorn.v8.annotations.JSIgnore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.pircbotx.Colors;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pmw.tinylog.Logger;

public class IRCWrapper {

    private static Map<String, BotWrapper> bots = new ConcurrentHashMap<>();
    private static CopyOnWriteArrayList<String> botNames = new CopyOnWriteArrayList<>();
    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public IRCWrapper() {
    }

    public String color(String c) {
        return Colors.lookup(c);
    }

    public V8Function getCallBack(V8Object m, String functionName) {
        Object callback = m.get(functionName);
        if (callback != null && callback instanceof V8Function) {
            return (V8Function) callback;
        }
        if (callback instanceof V8ResultUndefined) {
            return null;
        }
        return null;
    }

    public void run(Builder b, V8Object events) {

        if (botNames.addIfAbsent(b.getName().toLowerCase())) {
            b.addListener(new IRCBotListener());
            PircBotX bot = new PircBotX(b.buildConfiguration());
            BotWrapper bw = new BotWrapper(bot, getCallBack(events, "onMessage"));
            bw.setOnConnectCallback(getCallBack(events, "onConnect"));
            bots.put(b.getName(), bw);

            pool.submit(() -> {

                try {
                    bot.startBot();
                } catch (Exception ex) {
                    Logger.error(ex);
                    if (ex.getCause() != null) {
                        console.error(ex.getCause().toString());
                    } else {
                        console.error(ex.getMessage());
                    }
                }

            });
        }
    }

    public void echo(String botName, String channel, String msg) {
        pool.submit(() -> {
            PircBotX b = IRCWrapper.get(botName);
            if (b != null && b.isConnected()) {
                b.sendRaw().rawLineNow("PRIVMSG " + channel + " " + msg);
            }
        });

    }

    public static PircBotX get(String botName) {
        BotWrapper bw = bots.get(botName);
        if (bw != null) {
            return bw.getInstance();
        }
        return null;
    }

    @JSIgnore
    public static V8Function getCallBack(String botName) {
        BotWrapper bw = bots.get(botName);
        if (bw != null) {
            return bw.getOnMessageCallback();
        }
        return null;
    }

    @JSIgnore
    public static BotWrapper getBotWrapper(String botName) {
        BotWrapper bw = bots.get(botName);
        return bw;
    }

    public static boolean botExists(String name) {
        return botNames.contains(name);
    }
}
