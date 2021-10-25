package sk.dudak.upb.ecdc.task;

import javafx.concurrent.Task;

import java.util.function.Supplier;

public class CryptTask extends Task<Boolean> {

    private final Supplier<Boolean> cryptCall;

    public CryptTask(Supplier<Boolean> cryptCall) {
        this.cryptCall = cryptCall;
    }

    @Override
    protected Boolean call() throws Exception {
        return this.cryptCall.get();
    }
}
