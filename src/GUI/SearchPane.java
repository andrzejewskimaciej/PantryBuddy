package GUI;

import GUI.CustomCheckBox.CheckItem;
import GUI.CustomCheckBox.CheckedComboBox;
import GUI.CustomCheckBox.CustomCheckedBox;
import GUI.ingredientSelection.IngredientSelector;
import GUI.ingredientSelection.IngredientTile;
import Style.Theme;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static Features.SearchByName.*;
import static GUI.CustomCheckBox.CustomCheckedBox.createCCModelBox;

public class SearchPane extends JPanel implements ActionListener {

    private static final JButton searchByNameButton = new JButton("Search");
    private static final JTextField searchByNameTextF = new JTextField(20);
    private static final JButton searchByNameButtonRandom = new JButton("I'm Feeling Lucky");
    private final JScrollPane scrollMealsListPanel;
    private final IngredientSelector selector = new IngredientSelector(IngredientSelector.GridLayoutType.vertical);
    private final ComboBoxModel<CheckItem> categoryModel;
    private final ComboBoxModel<CheckItem> areaModel;
    private final JButton resetDefaultMealsButton;


    public SearchPane() {

        this.setLayout(new BorderLayout(0, 0));

        JPanel searchPanel = new JPanel(new BorderLayout(20, 20));
        this.add(searchPanel, BorderLayout.NORTH);
        scrollMealsListPanel = new JScrollPane(
                MealOnList.createMealLabel(new String[]{"Type meal name and search", "or surprise yourself"},
                        false));
        this.add(scrollMealsListPanel, BorderLayout.CENTER);
        scrollMealsListPanel.getVerticalScrollBar().setUnitIncrement(20);

        JPanel ingredientsListPanel = new JPanel();
        ingredientsListPanel.setLayout(new BorderLayout());
        this.add(ingredientsListPanel, BorderLayout.WEST);

        selector.setMaximumSize(new Dimension(
                Theme.screenDimensions.width / 2,
                Theme.screenDimensions.height * 2 / 3
        ));
        ingredientsListPanel.add(selector, BorderLayout.WEST);
        resetDefaultMealsButton = new JButton("Reset default meals");
        resetDefaultMealsButton.addActionListener(this);
        JPanel leftDownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftDownPanel.add(resetDefaultMealsButton);
        ingredientsListPanel.add(Box.createVerticalGlue());
        ingredientsListPanel.add(leftDownPanel, BorderLayout.SOUTH);


        JPanel searchByNamePanel = new JPanel();
        searchPanel.add(searchByNamePanel, BorderLayout.CENTER);

        searchByNamePanel.add(searchByNameTextF, BorderLayout.CENTER);
        JPanel searchButtonsPanel = new JPanel(new FlowLayout());
        searchButtonsPanel.add(searchByNameButton);
        searchButtonsPanel.add(searchByNameButtonRandom);
        searchByNamePanel.add(searchButtonsPanel);
        searchByNameTextF.addActionListener(this);
        searchByNameButton.addActionListener(this);
        searchByNameButtonRandom.addActionListener(this);

        JPanel categoryAreaPanel = new JPanel();
        categoryAreaPanel.setLayout(new BoxLayout(categoryAreaPanel, BoxLayout.Y_AXIS));
        this.add(categoryAreaPanel, BorderLayout.EAST);

        categoryAreaPanel.add(new JLabel("Select categories"));
        Object[] categoryArr = createCCModelBox(CustomCheckedBox.Type.category);
        categoryModel = (ComboBoxModel<CheckItem>) categoryArr[0];
        CheckedComboBox<CheckItem> ccBoxCategory = (CheckedComboBox<CheckItem>) categoryArr[1];
        categoryAreaPanel.add(ccBoxCategory);

        categoryAreaPanel.add(Box.createVerticalStrut(Theme.screenDimensions.height / 3));

        categoryAreaPanel.add(new JLabel("Select areas"));
        Object[] areaArr = createCCModelBox(CustomCheckedBox.Type.area);
        areaModel = (ComboBoxModel<CheckItem>) areaArr[0];
        CheckedComboBox<CheckItem> ccBoxArea = (CheckedComboBox<CheckItem>) areaArr[1];
        categoryAreaPanel.add(ccBoxArea);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (searchByNameButton.equals(e.getSource()) || searchByNameTextF.equals(e.getSource())) {
            searchMeals(false);
        } else if (searchByNameButtonRandom.equals(e.getSource())) {
            searchMeals(true);
        } else if (resetDefaultMealsButton.equals(e.getSource())){
            int response = JOptionPane.showConfirmDialog(
                this,
                "Do you really want to reset all built in meals?",
                "Reset Confirmation",
                JOptionPane.YES_NO_OPTION
            );
            if (response == JOptionPane.YES_OPTION) {
                DatabaseConnection.resetBuiltinsFromBackup();
                searchMeals(false);
                JOptionPane.showMessageDialog(
                    this,
                    "Reset completed.",
                    "Reset Completed",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void searchMeals(boolean isShuffled) {
        clearList();
        List<IngredientTile> TilesList = selector.getSelected();
        TilesList.forEach(tile -> addIngredientToList(tile.getId()));


        ArrayList<String> catList = CustomCheckedBox.addCategoryAreaToList(categoryModel);
        ArrayList<String> areaList = CustomCheckedBox.addCategoryAreaToList(areaModel);
        ResultSet rs = SearchingByNameIngredients(searchByNameTextF, isShuffled, catList, areaList);
        try {
            MealOnList.displaySearchedMeals(this, rs, scrollMealsListPanel);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

//    public JScrollPane getScrollMealsListPanel() {
//        return scrollMealsListPanel;
//    }
}


