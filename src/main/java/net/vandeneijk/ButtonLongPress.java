package net.vandeneijk;

class ButtonLongPress implements Runnable {
    private final Gui GUI = Gui.getInstance();
    private final DataStore DATA_STORE = DataStore.getInstance();
    private String clockAction;
    private long sleepTime = 500L;

    ButtonLongPress(String clockAction) {
        this.clockAction = clockAction;
    }

    /**
     * Keeps changing sleep time values as long a button stays pressed. Speed of change will increment.
     */
    public void run() {
        if (!this.DATA_STORE.isShutdown()) {
            switch(clockAction) {
                case "plusHour":
                    do {
                        DATA_STORE.setHourValue(DATA_STORE.getHourValue() + 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
                    break;
                case "plusMinute":
                    do {
                        DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() + 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
                    break;
                case "plusSecond":
                    do {
                        DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() + 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
                    break;
                case "minusHour":
                    do {
                        DATA_STORE.setHourValue(DATA_STORE.getHourValue() - 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
                    break;
                case "minusMinute":
                    do {
                        DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() - 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
                    break;
                case "minusSecond":
                    do {
                        DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() - 1);
                        defaultStatementsInCase();
                    } while(DATA_STORE.isButtonPressed());
            }
        }
    }

    /**
     * Calls the GUI to write the updated values and then sleep to delay values change for long button press.
     */
    private void defaultStatementsInCase() {
        GUI.writeTimeField();
        sleep();
    }

    /**
     * Calls sleep for this object's thread. After that it shortens the sleep period to a certain degree
     * if sleep is called again for this object.
     */
    private void sleep() {
        try {
            for(long count = 0L; this.DATA_STORE.isButtonPressed() && count < this.sleepTime; count += 20L) {
                Thread.sleep(20L);
            }
        } catch (InterruptedException iEx) {
            // For now, InterruptedException may be ignored if thrown.
        }

        if (this.sleepTime > 20L) {
            this.sleepTime = (long)((double)this.sleepTime / 1.3D);
        }
    }
}
