package fr.redstonneur1256.modlib.launcher.mixins;

import arc.Events;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.game.EventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mixin(Vars.class)
public class VarsMixin {

    public static @Shadow boolean loadedLogger;
    public static @Shadow boolean headless;

    @Inject(method = "loadLogger", at = @At("HEAD"), cancellable = true)
    private static void loadCustomLogger(CallbackInfo ci) {
        if (loadedLogger || Boolean.getBoolean("modlib.disableLogger")) return;

        ci.cancel();

        StringMap simpleClassNames = new StringMap();
        ObjectSet<String> hiddenClasses = ObjectSet.with("arc.util.Log");

        String[] levels = { "DEBUG", "INFO", "WARN", "ERROR", "NONE" };
        String[] colors = { "royal", "green", "yellow", "scarlet", "gray" };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        Seq<String> logBuffer = new Seq<>();
        Log.logger = (level, text) -> {
            synchronized (logBuffer) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                String className;
                int index = 5;
                do {
                    className = elements[index++].getClassName();
                } while (hiddenClasses.contains(className));
                String name = className;
                String caller = Log.level == Log.LogLevel.debug ? name : simpleClassNames.get(name, () -> name.substring(name.lastIndexOf('.') + 1));

                int ordinal = level.ordinal();
                String levelName = levels[ordinal];

                String console = Strings.format("[@][@/@][]: @", colors[ordinal], levelName, caller, text);

                System.out.printf("[%s] [%s/%s]: %s%n", formatter.format(LocalDateTime.now()), levelName, caller, text);

                if (!headless) {
                    if (Vars.ui == null || Vars.ui.consolefrag == null) {
                        logBuffer.add(console);
                    } else {
                        Vars.ui.consolefrag.addMessage(console);
                    }
                }
            }
        };

        Events.run(EventType.ClientLoadEvent.class, () -> {
            logBuffer.each(Vars.ui.consolefrag::addMessage);
            logBuffer.clear();
        });

        loadedLogger = true;
    }

}
