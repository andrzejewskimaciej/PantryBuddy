package Style;

import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;

import javax.swing.*;
import java.awt.*;

public abstract class Theme {

    public final static Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
    public final static Color inactiveColor = Color.darkGray;
    public final static Color activeColor = Color.BLUE;
    public final static Color favouriteYellow = new Color(255, 196, 0);

    public static void setTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

}
