package gis.gui;

import javax.swing.*;

public class DialogManager {
    public static void showErrorDialog(JFrame frame, String message){
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfoDialog(JFrame frame, String message){
        JOptionPane.showMessageDialog(frame, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
