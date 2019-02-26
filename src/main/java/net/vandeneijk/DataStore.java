package net.vandeneijk;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * All centrally available program data and states are saved within this singleton. For invariant safekeeping and
 * transparent method calls, some processing takes place.
 */
class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private LocalDateTime shutdownLdt;
    private boolean shutdown;
    private boolean buttonPressed;
    private int secondValue = 0;
    private int minuteValue = 0;
    private int hourValue = 0;
    private Dimension screenSize;
    private ImageIcon imageIcon;
    private Frame frameMain;

    private DataStore() {}

    // All getters:

    static DataStore getInstance() {
        return INSTANCE;
    }

    synchronized LocalDateTime getShutdownLdt() {
        return this.shutdownLdt;
    }

    synchronized boolean isShutdown() {
        return this.shutdown;
    }

    synchronized boolean isButtonPressed() {
        return this.buttonPressed;
    }

    synchronized int getSecondValue() {
        return this.secondValue;
    }

    synchronized int getMinuteValue() {
        return this.minuteValue;
    }

    synchronized int getHourValue() {
        return this.hourValue;
    }

    synchronized Dimension getScreenSize(){
        return screenSize;
    }

    synchronized ImageIcon getImageIcon(){
        return imageIcon;
    }

    synchronized Frame getFrameMain(){
        return frameMain;
    }

    // All setters:

    /**
     * Sets the shutdown LocalDateTime. In order to do this, it calculates a Duration in seconds from the set
     * hours, minutes and seconds. This is added to now.
     */
    synchronized void setShutdownLdt() {
        long secondsFromHours = (long)(this.getHourValue() * 3600);
        long secondsFromMinutes = (long)(this.getMinuteValue() * 60);
        Duration durationUntilShutdown = Duration.ofSeconds(secondsFromHours + secondsFromMinutes + (long)this.getSecondValue());
        this.shutdownLdt = LocalDateTime.now().plus(durationUntilShutdown);
    }

    synchronized void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    synchronized void setButtonPressed(boolean buttonPressed) {
        this.buttonPressed = buttonPressed;
    }

    /**
     * Sets the seconds value with overflow to minutes.
     * @param secondValue
     */
    synchronized void setSecondValue(int secondValue) {
        if (secondValue >= 0 && secondValue <= 59) {
            this.secondValue = secondValue;
        } else if (secondValue > 59) {
            if (this.hourValue < 99 || this.minuteValue < 59) {
                this.secondValue = 0;
                this.setMinuteValue(this.minuteValue + 1);
            }
        } else if (secondValue < 0 && (this.hourValue > 0 || this.minuteValue > 0)) {
            this.secondValue = 59;
            this.setMinuteValue(this.minuteValue - 1);
        }
    }

    /**
     * Sets the minutes value with overflow to hours.
     * @param minuteValue
     */
    synchronized void setMinuteValue(int minuteValue) {
        if (minuteValue >= 0 && minuteValue <= 59) {
            this.minuteValue = minuteValue;
        } else if (minuteValue > 59) {
            if (this.hourValue < 99) {
                this.minuteValue = 0;
                this.setHourValue(this.hourValue + 1);
            }
        } else if (minuteValue < 0 && this.hourValue > 0) {
            this.minuteValue = 59;
            this.setHourValue(this.hourValue - 1);
        }
    }

    synchronized void setHourValue(int hourValue) {
        if (hourValue >= 0 && hourValue <= 99) {
            this.hourValue = hourValue;
        }
    }

    synchronized void setScreenSize(Dimension screenSize){
        this.screenSize = screenSize;
    }

    synchronized void setImageIcon(ImageIcon imageIcon){
        this.imageIcon = imageIcon;
    }

    synchronized void setFrameMain(Frame frameMain){
        this.frameMain = frameMain;
    }
}

