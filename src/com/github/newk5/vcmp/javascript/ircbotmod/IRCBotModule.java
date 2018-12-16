package com.github.newk5.vcmp.javascript.ircbotmod;

import com.eclipsesource.v8.V8;
import com.github.newk5.vcmp.javascript.ircbotmod.injectables.IRCUser;
import com.github.newk5.vcmp.javascript.ircbotmod.injectables.IRCWrapper;
import com.github.newk5.vcmp.javascript.plugin.module.Module;
import io.alicorn.v8.V8JavaAdapter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class IRCBotModule extends Plugin {

    public static ThreadPoolExecutor pool;
    private static V8 v8 = com.github.newk5.vcmp.javascript.plugin.internals.Runtime.v8;

    public IRCBotModule(PluginWrapper wrapper) {
        super(wrapper);
        this.pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Extension
    public static class IRCBot implements Module {

        @Override
        public void inject() {
            V8JavaAdapter.injectClass("IRCBot", PircBotX.class, v8);
            V8JavaAdapter.injectClass("IRCConfig", Configuration.Builder.class, v8);
            V8JavaAdapter.injectClass("IRCUser", IRCUser.class, v8);
            V8JavaAdapter.injectClass(BasicThreadFactory.class, v8);
            V8JavaAdapter.injectObject("IRCWrapper", new IRCWrapper(), v8);

        }

        @Override
        public String javascript() {
            InputStream in = IRCBotModule.class.getResourceAsStream("module.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String code = reader.lines().collect(Collectors.joining("\n"));

            return code;
        }

    }
}
