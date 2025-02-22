package GUI;

import GUI.AddMealPane.AddNewMealRecipePane;

import javax.swing.*;
import java.awt.*;

public class Tabs extends JTabbedPane {
    public Tabs() {
        super(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
        setFont(getFont().deriveFont(Font.PLAIN, (float) (getFont().getSize()*1.3)));
        JPanel letCookPanel = new JPanel(new GridLayout(1, 1));
        letCookPanel.add(new LetsCookPane());

        addTab("\uD83C\uDF73 Let's cook", letCookPanel);
        JPanel browseMealsPanel = new JPanel(new GridLayout(1, 1));
        browseMealsPanel.add(new SearchPane());
        addTab("\uD83D\uDD0D Browse meals", browseMealsPanel);
        JPanel addRecipePanel = new JPanel(new GridLayout(1, 1));
        addRecipePanel.add(new AddNewMealRecipePane());
        addTab("➕ Add recipe", addRecipePanel);
        JPanel favouriteMealsPanel = new JPanel(new GridLayout(1, 1));
        FavoritesPane favoritesPane = new FavoritesPane();
        favouriteMealsPanel.add(favoritesPane);
        addTab("\uD83D\uDFCA Favourite meals", favouriteMealsPanel);
        addChangeListener(_ -> {
            if (((JPanel)getSelectedComponent()).getComponent(0) instanceof Refreshable<?> refreshable){
                refreshable.getRefreshed();
            }
        });
    }
}
