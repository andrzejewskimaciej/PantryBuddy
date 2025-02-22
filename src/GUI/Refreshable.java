package GUI;

import javax.swing.*;

public interface Refreshable<T extends JComponent> {
    T getRefreshed();
}
