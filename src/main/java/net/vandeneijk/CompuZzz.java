package net.vandeneijk;

public class CompuZzz {

    /**
     * Starts the necessary objects for program operation. Nothing else happens here.
     * @param args
     */
    public static void main(String[] args) {
        Gui.getInstance();

        // Starts the main timer function in a separate daemon thread.
        Thread threadForTickTock = new Thread(new TickTock());
        threadForTickTock.setDaemon(true);
        threadForTickTock.start();
    }
}
