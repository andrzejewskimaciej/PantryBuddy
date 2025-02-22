package GUI;

import Features.SearchByName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class IngredientOnList extends JPanel{

    private static JPanel info = new JPanel();
    private static final ArrayList<JButton> ingredientsButtonList = new ArrayList<>();

    public IngredientOnList(String name) {
        super(new BorderLayout(8, 8));

        setMaximumSize(new Dimension(460, 60));
        setMinimumSize(new Dimension(460, 60));
        setPreferredSize(new Dimension(460, 60));

        info = new JPanel();
        info.setLayout(new FlowLayout());

        JButton nameButton = new JButton(name);
        ingredientsButtonList.add(nameButton);
        //nameButton.setForeground(Color.black);
        nameButton.setFont(new Font("Arial", Font.BOLD, 16));
        nameButton.setToolTipText(name);
        info.add(nameButton);

        //nameButton.addActionListener(this);

        add(info, BorderLayout.CENTER);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 2, 0, 2), null));

    }

    public static ArrayList<JButton> getIngredientsButtonList() {
        return ingredientsButtonList;
    }

    public static boolean removeIngredientButton(ArrayList<JButton> ingredientsButtonList, ActionEvent e) {
        for (JButton jButton : ingredientsButtonList) {
            if (jButton.equals(e.getSource())) {
                //SearchByName.removeIngredientFromList(jButton.getText());
                info.remove(jButton);
                info.revalidate();
                return true;
            }
        }
        return false;
    }




}
