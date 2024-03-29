package frontend.panels.pages;

import frontend.FrontendParameters;
import frontend.widgets.PresetTextArea;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * File: Page2.java
 * ----------------------------------------------
 * The panel that represents an interactive page.
 * It allows user to configure clock signals from
 * "clock_hub.sv" module that will be connected
 * to the DUT clock ports.
 */
public class Page2 extends JPanel implements FrontendParameters {

    /* The name of the page.
    It is used for gain access to this page
    from the ActionEvent interface. */
    private static final String PAGE_NAME = "Specify clocks";

    /* The spacing between the top border of application window
    and first row of the displayed text. */
    private static final int TOP_ALIGNMENT = 60;

    /* The vertical spacing between displayed components. */
    private static final int VERTICAL_COMPONENTS_ALIGNMENT = 25;

    /* The horizontal spacing between displayed components. */
    private static final int HORIZONTAL_COMPONENTS_ALIGNMENT = 80;

    /* The panels that allows user to specify connection between
    DUT and "clock_hub" modules. */
    private HashMap<String, ClockSpecificationPanel> clockSpecPanels = new HashMap<>();

    /* The dialog texts that will be displayed on the application window. */
    private static final String PAGE_TEXT_WITH_CLK =
            "DUT file is read. \n" +
            "Specify the clock signals, that must be connected to the DUT.";
    private static final String PAGE_TEXT_WITHOUT_CLK =
            "DUT file is read. This page customizes module clocks. \n" +
            "But your DUT doesn't contain any clock inputs. Skip this page.";

    /**
     * The class constructor.
     */
    public Page2() {
        setBounds(0, TOP_ALIGNMENT, APP_WIDTH, IP_HEIGHT - TOP_ALIGNMENT);
        setBackground(BACKGROUND_COLOR);
        setLayout(new FlowLayout(
                FlowLayout.CENTER,
                HORIZONTAL_COMPONENTS_ALIGNMENT,
                VERTICAL_COMPONENTS_ALIGNMENT));
    }

    /**
     * Returns a HashMap object that sets correspondence
     * between DUT's clock's ports and clocks hub ports.
     *
     * @return The HashMap object.
     *         Key is DUT's port.
     *         Value is clocks hub port.
     */
    public HashMap<String, String> getClocksHashMap() {
        HashMap<String, String> clocksHashMap = new HashMap<>();

        for (String dutClockName: clockSpecPanels.keySet()) {
            clocksHashMap.put(dutClockName, clockSpecPanels.get(dutClockName).getSpecifiedClock());
        }

        return clocksHashMap;
    }

    /**
     * Sets names of the DUT's clock's ports.
     *
     * @param dutClocks The list of the clocks of DUT module.
     *                  It is automatically formed from a DUT file.
     */
    public void setDutClocks(ArrayList<String> dutClocks) {
        removeAll();

        if (dutClocks.size() != 0)
            add(new PresetTextArea(PAGE_TEXT_WITH_CLK));
        else
            add(new PresetTextArea(PAGE_TEXT_WITHOUT_CLK));

        /* Deletes an old list of the clocks. */
        clockSpecPanels = new HashMap<>();

        /* Sets a new list of the clocks. */
        for (String dutClockName: dutClocks) {
            ClockSpecificationPanel clockSpecificationPanel = new ClockSpecificationPanel(dutClockName);
            clockSpecPanels.put(dutClockName, clockSpecificationPanel);
            add(clockSpecificationPanel);
        }
    }

    /**
     * Returns a name of the page.
     *
     * @return The String value of the page's name.
     */
    public String getName() {
        return PAGE_NAME;
    }


    /** Allows to configure clock signals that will be connected to the DUT clock ports. */
    private static final class ClockSpecificationPanel extends JPanel implements FrontendParameters {

        final JTextField textField = new JTextField("50000"); {
            textField.setBorder(BorderFactory.createBevelBorder(1));
            textField.setFont(PAGE_FONT);
            textField.setBackground(GREY);
            textField.setForeground(FONT_COLOR);
        }

        /**
         * The class constructor.
         *
         * @param clockName The name of the custom clock.
         */
        public ClockSpecificationPanel(String clockName) {
            setBackground(BACKGROUND_COLOR);
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 25));
            setClockLabel(clockName);
            add(textField);
            setClockLabel("kHz");
        }

        /**
         * Adds JLabel object that represents the label
         * of custom clock to the panel.
         *
         * @param text The label of the custom clock.
         */
        private void setClockLabel(String text) {
            final JLabel label = new JLabel(text);
            label.setFont(PAGE_FONT);
            label.setForeground(FONT_COLOR);
            add(label);
        }

        /**
         * Returns the clock that was specified by user.
         *
         * @return The String value of clock name.
         */
        public String getSpecifiedClock() {
            return String.valueOf(textField.getText());
        }
    }

}