package fr.redstonneur1256.modlib.launcher.mixin;

import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A very basic logger adapter which does not log anything to file and simply
 * emits formatted log messages to the console print streams.
 * It only exists to remove unnecessary call to Platform class.
 */
public class ModLibLoggerAdapterConsole extends LoggerAdapterAbstract {

    /**
     * Date format for console messages
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Print stream for DEBUG-level messages, null by default
     */
    private PrintStream debug;

    /**
     * @param name Logger name
     */
    public ModLibLoggerAdapterConsole(String name) {
        super((name == null)? "" : name);
    }

    @Override
    public String getType() {
        return "Default Console Logger";
    }

    /**
     * Set output stream for DEBUG-level messages
     *
     * @param debug New PrintStream for debug messages
     * @return fluent
     */
    public ModLibLoggerAdapterConsole setDebugStream(PrintStream debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public void catching(Level level, Throwable t) {
        this.log(Level.WARN, "Catching {}: {}", t.getClass().getName(), t.getMessage(), t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        PrintStream out = this.getOutputStream(level);
        if (out != null) {
            FormattedMessage formatted = new FormattedMessage(message, params);
            out.printf("[%s] [%s/%s] %s%n", ModLibLoggerAdapterConsole.DATE_FORMAT.format(new Date()), this.getId(), level, formatted);
            if (formatted.hasThrowable()) {
                formatted.getThrowable().printStackTrace(out);
            }
        }
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        PrintStream out = this.getOutputStream(level);
        if (out != null) {
            out.printf("[%s] [%s/%s] %s%n", ModLibLoggerAdapterConsole.DATE_FORMAT.format(new Date()), this.getId(), level, message);
            t.printStackTrace(out);
        }
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        this.log(Level.WARN, "Throwing {}: {}", t.getClass().getName(), t.getMessage(), t);
        return t;
    }

    private PrintStream getOutputStream(Level level) {
        if (level == Level.FATAL || level == Level.ERROR || level == Level.WARN) {
            return System.err;
        } else if (level == Level.INFO) {
            return System.out;
        } else if (level == Level.DEBUG) {
            return this.debug;
        }
        return null;
    }

}
