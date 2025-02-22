package GUI;

import Style.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Frame extends JFrame {

    public Frame()  {
        setTitle("PantryBuddy");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(
                Theme.screenDimensions.width*8/10,
                Theme.screenDimensions.height*8/10
        );
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            setIconImage(ImageIO.read(new File("data/pantry_icon.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.setContentPane(new Tabs());

        setVisible(true);

        LetsCookPane.lock.lock();
        LetsCookPane.condition.signal();
        LetsCookPane.lock.unlock();
    }
}
