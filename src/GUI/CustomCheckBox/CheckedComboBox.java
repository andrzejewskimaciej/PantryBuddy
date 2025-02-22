package GUI.CustomCheckBox;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CheckedComboBox<E extends CheckItem> extends JComboBox<E> {
    private boolean keepOpen;
    private final JPanel panel = new JPanel(new BorderLayout());

    public CheckedComboBox(ComboBoxModel<E> model) {
        super(model);
    }


    @Override
    public void updateUI() {
        setRenderer(null);
        super.updateUI();

        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
            ((ComboPopup) a).getList().addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    JList<?> list = (JList<?>) e.getComponent();
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        keepOpen = true;
                        updateItem(list.locationToIndex(e.getPoint()));
                    }
                }
            });
        }

        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            panel.removeAll();
            Component c = renderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (index < 0) {
                String txt = getCheckItemString(list.getModel());
                JLabel l = (JLabel) c;
                l.setText(txt.isEmpty() ? " " : txt);
                l.setOpaque(false);
                l.setForeground(list.getForeground());
                panel.setOpaque(false);
            } else {
                check.setSelected(value.isSelected());
                panel.add(check, BorderLayout.WEST);
                panel.setOpaque(true);
                panel.setBackground(c.getBackground());
            }
            panel.add(c);
            return panel;
        });
        initActionMap();
    }

    private void initActionMap() {
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        getInputMap(WHEN_FOCUSED).put(ks, "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    updateItem(((ComboPopup) a).getList().getSelectedIndex());
                }
            }
        });
    }

    private void updateItem(int index) {
        if (isPopupVisible() && index >= 0) {
            E item = getItemAt(index);
            item.setSelected(!item.isSelected());
            setSelectedIndex(-1);
            setSelectedItem(item);
        }
    }

    @Override
    public void setPopupVisible(boolean v) {
        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }
    }

    private static <E extends CheckItem> String getCheckItemString(ListModel<E> model) {
        return IntStream.range(0, model.getSize())
                .mapToObj(model::getElementAt)
                .filter(CheckItem::isSelected)
                .map(Objects::toString)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
