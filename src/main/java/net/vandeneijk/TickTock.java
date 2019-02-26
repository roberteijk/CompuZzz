package net.vandeneijk;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class TickTock implements Runnable {
    private final Gui GUI = Gui.getInstance();
    private final DataStore DATA_STORE = DataStore.getInstance();

    TickTock() {}

    /**
     * Main timer function for the app. Will update the clock and poll for shutdown when set.
     */
    public void run() {
        while(true) {
            if (this.DATA_STORE.isShutdown()) {
                Long timeInSecondsToShutdown = ChronoUnit.SECONDS.between(LocalDateTime.now(), this.DATA_STORE.getShutdownLdt());
                int hoursToShutdown = (int)(timeInSecondsToShutdown / 3600L);
                int minutesToShutdown = (int)((timeInSecondsToShutdown - (long)(hoursToShutdown * 3600)) / 60L);
                int secondsToShutdown = (int)(timeInSecondsToShutdown - (long)(hoursToShutdown * 3600) - (long)(minutesToShutdown * 60));
                if (this.DATA_STORE.getHourValue() != hoursToShutdown) {
                    this.DATA_STORE.setHourValue(hoursToShutdown);
                    this.GUI.writeTimeField();
                }

                if (this.DATA_STORE.getMinuteValue() != minutesToShutdown) {
                    this.DATA_STORE.setMinuteValue(minutesToShutdown);
                    this.GUI.writeTimeField();
                }

                if (this.DATA_STORE.getSecondValue() != secondsToShutdown) {
                    this.DATA_STORE.setSecondValue(secondsToShutdown);
                    this.GUI.writeTimeField();
                }

                if (hoursToShutdown <= 0 && minutesToShutdown <= 0 && secondsToShutdown <= 0) {
                    try {
                        Runtime.getRuntime().exec("shutdown.exe -s -t 0");
                    } catch (IOException ioEx) {
                        // Program will close quietly without pc shutdown when exec throws an IOException.
                    }

                    System.exit(0);
                }
            }

            if (!this.DATA_STORE.isButtonPressed()) {
                this.GUI.writeInfoFields();
            }

            try {
                Thread.sleep(250L);
            } catch (InterruptedException iEx) {
                // For now, InterruptedException may be ignored if thrown.
            }
        }
    }
}
