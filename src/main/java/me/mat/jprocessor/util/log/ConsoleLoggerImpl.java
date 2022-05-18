package me.mat.jprocessor.util.log;

public class ConsoleLoggerImpl implements Logger {

    @Override
    public void info(String format, Object... args) {
        System.out.printf("[INFO]: " + format + "%n", args);
    }

    @Override
    public void warn(String format, Object... args) {
        System.out.printf("[WARN]: " + format + "%n", args);
    }

    @Override
    public void error(String format, Object... args) {
        System.out.printf("[ERR]: " + format + "%n", args);
    }

}
