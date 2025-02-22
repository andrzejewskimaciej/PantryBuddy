package GUI.CustomCheckBox;
import Style.Theme;
import basicDataTypes.Meal;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

public class CustomCheckedBox {

    public enum Type {category, area}

    public static ArrayList<String> addCategoryAreaToList(ComboBoxModel<CheckItem> catArea) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < catArea.getSize(); i++) {
            CheckItem item = catArea.getElementAt(i);
            if (item.isSelected()) {
                arr.add(item.toString());
            }
        }
        return arr;
    }

    public static Object[] createCCModelBox(Type t) {

        ArrayList<CheckItem> m = new ArrayList<>();
        if (t == Type.category) {
            Arrays.stream(Meal.getCategoriesList()).sorted().forEachOrdered(x -> m.add(new CheckItem(x, false)));
        } else {
            Arrays.stream(Meal.getAreasList()).sorted().forEachOrdered(x -> m.add(new CheckItem(x, false)));
        }
        ComboBoxModel<CheckItem> model = new DefaultComboBoxModel<>(m.toArray(CheckItem[]::new));
        CheckedComboBox<CheckItem> ccBox = new CheckedComboBox<>(model);
        ccBox.setPreferredSize(new Dimension(
                Theme.screenDimensions.width / 5,
                Theme.screenDimensions.height / 20
        ));
        ccBox.setMaximumSize(new Dimension(
                Theme.screenDimensions.width / 2,
                Theme.screenDimensions.height / 20
        ));

        return new Object[]{model, ccBox};
    }


    }


//    class WindowsCheckedComboBox<E extends CheckItem> extends CheckedComboBox<E> {
//        private transient ActionListener listener;
//
//        protected WindowsCheckedComboBox(ComboBoxModel<E> model) {
//            super(model);
//        }
//
//        @Override public void updateUI() {
//            setRenderer(null);
//            removeActionListener(listener);
//            super.updateUI();
//            listener = e -> {
//                if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
//                    keepOpen = true;
//                    updateItem(getSelectedIndex());
//                }
//            };
//            addActionListener(listener);
//
//            JLabel label = new JLabel(" ");
//            JCheckBox check = new JCheckBox(" ");
//            setRenderer((list, value, index, isSelected, cellHasFocus) -> {
//                if (index < 0) {
//                    String txt = getCheckItemString(list.getModel());
//                    label.setText(txt.isEmpty() ? " " : txt);
//                    return label;
//                } else {
//                    check.setText(Objects.toString(value, ""));
//                    check.setSelected(value.isSelected());
//                    if (isSelected) {
//                        check.setBackground(list.getSelectionBackground());
//                        check.setForeground(list.getSelectionForeground());
//                    } else {
//                        check.setBackground(list.getBackground());
//                        check.setForeground(list.getForeground());
//                    }
//                    return check;
//                }
//            });
//            initActionMap();
//        }
//
//
//
//}
