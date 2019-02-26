package net.vandeneijk;

class TickTockTimeColor implements Runnable {
    private final Gui GUI = Gui.getInstance();

    TickTockTimeColor() {
    }

    /**
     * Extra timer function to coordinate a gradual color fade/change when requested from the GUI.
     */
    public void run() {
        for(int i = 0; i < 20; ++i) {
            this.GUI.setTextColor();

            try {
                Thread.sleep(50L);
            } catch (InterruptedException iEx) {
                // For now, InterruptedException may be ignored if thrown.
            }
        }
    }
}
