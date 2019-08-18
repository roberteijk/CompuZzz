package net.vandeneijk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

class Gui {
    private static final Gui INSTANCE = new Gui();
    private final DataStore DATA_STORE = DataStore.getInstance();

    private JFrame frameMain = new JFrame("CompuZzz");
    private JButton plusHour = new JButton("+H");
    private JLabel infoFieldHour;
    private JButton minusHour = new JButton("-H");
    private JButton plusMinute = new JButton("+M");
    private JLabel infoFieldMinute;
    private JButton minusMinute = new JButton("-M");
    private JButton plusSecond = new JButton("+S");
    private JLabel infoFieldSecond;
    private JButton minusSecond = new JButton("-S");
    private JButton startButton = new JButton("Start");
    private JLabel infoField;

    private DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG); // Don't change to .ofPattern(). FormatStyle works on locale settings.
    private DateTimeFormatter tf = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM); // Don't change to .ofPattern(). FormatStyle works on locale settings.

    private Color timeColor = new Color(250, 0, 0);
    private Color buttonTextColor = new Color(0, 0, 0);
    private Color startButtonTextColor = new Color(200, 200, 200);

    private boolean guiAdditionFound;

    private Gui() {
        try {
            Class.forName("net.vandeneijk.GuiAddition");
            guiAdditionFound = true;
        } catch (ClassNotFoundException ex) {
            guiAdditionFound = false;
        }
        getIcon();
        setUpJFrame();
        DATA_STORE.setFrameMain(frameMain); // This is passed so GuiAddition (when available) can extract info from it.
    }

    static Gui getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a tempfile for the custom program icon. This way it's safe for an executable JAR.
     */
    private void getIcon() {
        try {
            InputStream in = this.getClass().getResourceAsStream("/images/SleepTimerOmega48.png");
            if (in == null) {
                return;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (FileNotFoundException fnfEx) {
                // An exception here means no program icon will be shown. There is already safety build in when
                // the icon gets called.
            }

            String convertString = tempFile.toURI().toString().substring(5).replace("/", "//");
            DATA_STORE.setImageIcon(new ImageIcon(convertString));
        } catch (IOException outer) {
            // An exception here means no program icon will be shown. There is already safety build in when
            // the icon gets called.
        }
    }

    /**
     * Starts a thread to control GUI color fade speed.
     */
    private void callTickTockTimeColor() {
        Thread threadForTickTockTimeColor = new Thread(new TickTockTimeColor());
        threadForTickTockTimeColor.setDaemon(true);
        threadForTickTockTimeColor.start();
    }

    /**
     * Sets all elements of the GUI.
     */
    private void setUpJFrame() {
        /**
         * Nested class to accommodate event related code to prevent redundant code.
         */
        class ButtonRoutine {
            private void longMousePress(String command){
                DATA_STORE.setButtonPressed(true);
                Thread threadForButtonLongPress = new Thread(new ButtonLongPress(command));
                threadForButtonLongPress.setDaemon(true);
                threadForButtonLongPress.start();
            }

            private void keyRegistration(JButton jButton) {
                jButton.registerKeyboardAction(plusHour.getActionForKeyStroke(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                        JComponent.WHEN_FOCUSED);

                jButton.registerKeyboardAction(plusHour.getActionForKeyStroke(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                        JComponent.WHEN_FOCUSED);
            }

            private void defaultPostButtonRoutine() {
                writeTimeField();
                callTickTockTimeColor();
            }

            private void defaultPostMouseRoutine() {
                callTickTockTimeColor();
            }
        }
        ButtonRoutine buttonRoutine = new ButtonRoutine();

        Font fontLargeBold = new Font("monospaced", Font.BOLD, 24);
        Font fontMediumBold = new Font("default", Font.BOLD, 15);
        Font fontMedium = new Font("monospaced", Font.PLAIN, 12);
        Font fontNormalBold = new Font("default", Font.BOLD, 12);
        Font fontSmall = new Font("monospaced", Font.PLAIN, 11);


        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));


        JPanel upperInMain = new JPanel();
        upperInMain.setMaximumSize(new Dimension(300, 150));
        upperInMain.setMinimumSize(new Dimension(300, 150));
        upperInMain.setLayout(new GridLayout(1, 1));
        panelMain.add(upperInMain);


        // Code for hour, minute and second buttons and display starts here.
        JPanel buttonColumn1 = new JPanel();
        buttonColumn1.setLayout(new GridLayout(3, 1));
        upperInMain.add(buttonColumn1);
        plusHour.setFont(fontNormalBold);
        plusHour.setForeground(buttonTextColor);
        buttonColumn1.add(plusHour);
        plusHour.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("plusHour");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(plusHour);
        plusHour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setHourValue(DATA_STORE.getHourValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        JPanel columnInfo1 = new JPanel();
        columnInfo1.setBackground(Color.BLACK);
        buttonColumn1.add(columnInfo1);
        infoFieldHour = new JLabel("00");
        infoFieldHour.setForeground(timeColor);
        infoFieldHour.setFont(fontLargeBold);
        columnInfo1.add(infoFieldHour);
        infoFieldHour.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    DATA_STORE.setHourValue(DATA_STORE.getHourValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                } else {
                    DATA_STORE.setHourValue(DATA_STORE.getHourValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        minusHour.setFont(fontNormalBold);
        minusHour.setForeground(buttonTextColor);
        buttonColumn1.add(minusHour);
        minusHour.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("minusHour");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(minusHour);
        minusHour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setHourValue(DATA_STORE.getHourValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });

        JPanel buttonColumn2 = new JPanel();
        buttonColumn2.setLayout(new GridLayout(3, 1));
        upperInMain.add(buttonColumn2);
        plusMinute.setFont(fontNormalBold);
        plusMinute.setForeground(buttonTextColor);
        buttonColumn2.add(plusMinute);
        plusMinute.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("plusMinute");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(plusMinute);
        plusMinute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        JPanel columnInfo2 = new JPanel();
        columnInfo2.setBackground(Color.BLACK);
        buttonColumn2.add(columnInfo2);
        infoFieldMinute = new JLabel("00");
        infoFieldMinute.setForeground(timeColor);
        infoFieldMinute.setFont(fontLargeBold);
        columnInfo2.add(infoFieldMinute);
        infoFieldMinute.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                } else {
                    DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        minusMinute.setFont(fontNormalBold);
        minusMinute.setForeground(buttonTextColor);
        buttonColumn2.add(minusMinute);
        minusMinute.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("minusMinute");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(minusMinute);
        minusMinute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setMinuteValue(DATA_STORE.getMinuteValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });

        JPanel buttonColumn3 = new JPanel();
        buttonColumn3.setLayout(new GridLayout(3, 1));
        upperInMain.add(buttonColumn3);
        plusSecond.setFont(fontNormalBold);
        plusSecond.setForeground(buttonTextColor);
        buttonColumn3.add(plusSecond);
        plusSecond.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("plusSecond");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(plusSecond);
        plusSecond.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        JPanel columnInfo3 = new JPanel();
        columnInfo3.setBackground(Color.BLACK);
        buttonColumn3.add(columnInfo3);
        infoFieldSecond = new JLabel("00");
        infoFieldSecond.setForeground(timeColor);
        infoFieldSecond.setFont(fontLargeBold);
        columnInfo3.add(infoFieldSecond);
        infoFieldSecond.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() + 1);
                    buttonRoutine.defaultPostButtonRoutine();
                } else {
                    DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });
        minusSecond.setFont(fontNormalBold);
        minusSecond.setForeground(buttonTextColor);
        buttonColumn3.add(minusSecond);
        minusSecond.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DATA_STORE.setButtonPressed(false);
                buttonRoutine.defaultPostMouseRoutine();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                buttonRoutine.longMousePress("minusSecond");
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        buttonRoutine.keyRegistration(minusSecond);
        minusSecond.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DATA_STORE.isButtonPressed()) {
                    DATA_STORE.setSecondValue(DATA_STORE.getSecondValue() - 1);
                    buttonRoutine.defaultPostButtonRoutine();
                }
            }
        });

        JPanel buttonColumn4 = new JPanel();
        buttonColumn4.setLayout(new GridLayout(1, 1));
        upperInMain.add(buttonColumn4);
        startButton.setFont(fontMediumBold);
        startButton.setForeground(startButtonTextColor);
        buttonColumn4.add(startButton);
        buttonRoutine.keyRegistration(startButton);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(DATA_STORE.getHourValue() <= 0 && DATA_STORE.getMinuteValue() <= 0 && DATA_STORE.getSecondValue() <= 0)) {
                    DATA_STORE.setShutdown(!DATA_STORE.isShutdown());
                }
                if (DATA_STORE.isShutdown()) {
                    DATA_STORE.setShutdownLdt();
                    writeInfoFields();
                } else {
                    writeInfoFields();
                }
            }
        });


        // Code for time information and copyright (below the buttons) starts here.
        infoField = new JLabel();
        infoField.setForeground(Color.WHITE);
        infoField.setFont(fontMedium);
        infoField.setHorizontalAlignment(JLabel.CENTER);
        infoField.setVerticalAlignment(JLabel.CENTER);

        JLabel authorField = new JLabel();
        authorField.setForeground(Color.LIGHT_GRAY);
        authorField.setFont(fontSmall);
        authorField.setHorizontalAlignment(JLabel.CENTER);
        authorField.setVerticalAlignment(JLabel.CENTER);
        JPanel lowerInMain = new JPanel();
        panelMain.add(lowerInMain);

        if (guiAdditionFound) { // Extra functionality if true. This done to hide some private code from Github.
            authorField.setText("click here for more information, © 2019");
            lowerInMain.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Class<?> guiAddition = Class.forName("net.vandeneijk.GuiAddition");
                        Method openInfoFrame = guiAddition.getMethod("openInfoFrame");
                        openInfoFrame.invoke(guiAddition);
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException varEx){
                        // If class and/or method are not found or the various exceptions are thrown, the extra
                        // GUI part will not load. This is by design and there is no need to recover from it. The
                        // extra GUI part contains private information that should not be distributed through Github
                        // etc.
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        } else authorField.setText("by Robert van den Eijk, © 2019");
        lowerInMain.setBackground(Color.BLACK);
        lowerInMain.setLayout(new GridLayout(2, 1));
        lowerInMain.add(infoField);
        lowerInMain.add(authorField);


        // The main frame is set here.
        frameMain.add(panelMain);
        frameMain.setSize(300, 225);
        frameMain.setResizable(false);
        
        DATA_STORE.setScreenSize(Toolkit.getDefaultToolkit().getScreenSize());
        if (DATA_STORE.getScreenSize().getHeight() < 1200) { // Determines the placement of main frame GUI elements depending on screen resolution.
            frameMain.setLocation(DATA_STORE.getScreenSize().width / 2 - frameMain.getSize().width / 2, DATA_STORE.getScreenSize().height / 4 - frameMain.getSize().height / 4);
        } else {
            frameMain.setLocation(DATA_STORE.getScreenSize().width / 2 - frameMain.getSize().width / 2, DATA_STORE.getScreenSize().height / 3 - frameMain.getSize().height / 3);
        }
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (DATA_STORE.getImageIcon() != null) frameMain.setIconImage(DATA_STORE.getImageIcon().getImage());
        frameMain.setVisible(true);
    }

    /**
     * Determines the text and time for the general infofield.
     * It also checks for fading / color changing GUI items. If necessary it calls for extra clock ticks to
     * make sure the fade / color change completes.
     */
    synchronized void writeInfoFields() {
        String preText;
        LocalDateTime momentOfShutdown;
        if (DATA_STORE.isShutdown()) {
            momentOfShutdown = DATA_STORE.getShutdownLdt();
            preText = "shutdown at ";
            if (timeColor.getGreen() < 250) callTickTockTimeColor();
        } else {
            long secondsFromHours = DATA_STORE.getHourValue() * 3600;
            long secondsFromMinutes = DATA_STORE.getMinuteValue() * 60;
            Duration durationUntilShutdown = Duration.ofSeconds(secondsFromHours + secondsFromMinutes + DATA_STORE.getSecondValue());
            momentOfShutdown = LocalDateTime.now().plus(durationUntilShutdown);
            preText = "setting for ";
            if (timeColor.getRed() < 250) callTickTockTimeColor();
        }
        infoField.setText(preText + df.format(momentOfShutdown) + " " + tf.format(momentOfShutdown));
    }

    /**
     * Changes the GUI time values between the buttons and then synchronizes the general infofield with the updated
     * values.
     */
    synchronized void writeTimeField() {
        String formattedHour = String.format("%02d", DATA_STORE.getHourValue());
        infoFieldHour.setText(formattedHour);
        String formattedMinute = String.format("%02d", DATA_STORE.getMinuteValue());
        infoFieldMinute.setText(formattedMinute);
        String formattedSecond = String.format("%02d", DATA_STORE.getSecondValue());
        infoFieldSecond.setText(formattedSecond);
        writeInfoFields();
    }

    /**
     * Changes the GUI color scheme and fades buttons depending the mode of operation. The timing is done by
     * TickTockTimeColor. Every tick from TickTockTimeColor will cause a small change in color. This method could
     * possibly be made more readable. The reason for the cluttered appearance is that the method accommodates for
     * for the fact that a fade or color change could be halfway when an opposing command is given by the user.
     * This is a case of organically writing a method to produce the smoothest user experience.
     */
    synchronized void setTextColor() {
        // This part determines the color.
        if (DATA_STORE.isShutdown()) {
            if (timeColor.getGreen() < 250) {
                if (timeColor.getRed() > 0) {
                    timeColor = new Color(timeColor.getRed() - 25, timeColor.getGreen(), 0);
                } else {
                    timeColor = new Color(timeColor.getRed(), timeColor.getGreen() + 25, 0);
                }
            }
            if (buttonTextColor.getRed() < 200) {
                buttonTextColor = new Color(buttonTextColor.getRed() + 10, buttonTextColor.getGreen() + 10, buttonTextColor.getBlue() + 10);
            }
            if (startButton.getText().equals("Start") && startButtonTextColor.getRed() < 200) {
                startButtonTextColor = new Color(startButtonTextColor.getRed() + 20, startButtonTextColor.getGreen() + 20, startButtonTextColor.getBlue() + 20);
            } else {
                startButton.setText("Stop");
                if (startButtonTextColor.getRed() > 0) {
                    startButtonTextColor = new Color(startButtonTextColor.getRed() - 20, startButtonTextColor.getGreen() - 20, startButtonTextColor.getBlue() - 20);
                }
            }
        } else {
            if (timeColor.getRed() < 250) {
                if (timeColor.getGreen() > 0) {
                    timeColor = new Color(timeColor.getRed(), timeColor.getGreen() - 25, 0);
                } else {
                    timeColor = new Color(timeColor.getRed() + 25, timeColor.getGreen(), 0);
                }
            }
            if (buttonTextColor.getRed() > 0) {
                buttonTextColor = new Color(buttonTextColor.getRed() - 10, buttonTextColor.getGreen() - 10, buttonTextColor.getBlue() - 10);
            }
            if (startButton.getText().equals("Stop") && startButtonTextColor.getRed() < 200) {
                startButtonTextColor = new Color(startButtonTextColor.getRed() + 20, startButtonTextColor.getGreen() + 20, startButtonTextColor.getBlue() + 20);
            } else {
                startButton.setText("Start");
                if (DATA_STORE.getHourValue() <= 0 && DATA_STORE.getMinuteValue() <= 0 && DATA_STORE.getSecondValue() <= 0 && startButtonTextColor.getRed() < 200) {
                    startButtonTextColor = new Color(startButtonTextColor.getRed() + 20, startButtonTextColor.getGreen() + 20, startButtonTextColor.getBlue() + 20);
                } else if (startButtonTextColor.getRed() > 0 && (DATA_STORE.getHourValue() > 0 || DATA_STORE.getMinuteValue() > 0 || DATA_STORE.getSecondValue() > 0)) {
                    startButtonTextColor = new Color(startButtonTextColor.getRed() - 20, startButtonTextColor.getGreen() - 20, startButtonTextColor.getBlue() - 20);
                }
            }
        }

        // This part writes the color determined above.
        infoFieldHour.setForeground(timeColor);
        infoFieldMinute.setForeground(timeColor);
        infoFieldSecond.setForeground(timeColor);
        plusHour.setForeground(buttonTextColor);
        minusHour.setForeground(buttonTextColor);
        plusMinute.setForeground(buttonTextColor);
        minusMinute.setForeground(buttonTextColor);
        plusSecond.setForeground(buttonTextColor);
        minusSecond.setForeground(buttonTextColor);
        startButton.setForeground(startButtonTextColor);
    }
}

