package Tests;

import GUI.AddMealPane.AddIngredientPopup;
import GUI.AddMealPane.AddNewMealRecipePane;
import GUI.AddMealPane.MakeAmountListPanel;
import GUI.GoBackPanel;
import GUI.SearchPane;
import GUI.Tabs;
import GUI.ingredientSelection.AutocompleteIngredientSearchBar;
import GUI.ingredientSelection.IngredientSelector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

public class TestGUI {
    @BeforeAll
    static void initAll() {
        BasicTests.initAll();
    }

    @AfterAll
    static void cleanupAll() {
        BasicTests.cleanupAll();
    }

    /*
// Przykładowe wykorzystanie TestPopup; Nie jest to prawdziwy test
    @Test
    void testIngredientOnList () {
        TestPopup.testComponent(new IngredientOnList("name"));
    }

    @Test
    void testIngredientOnList2 () {
        JComponent component = new IngredientOnList("name");
        JFrame _ = TestPopup.showComponent(component);
//        Test behaviour
//        component.totallyARealMethod(someArgs);
        TimeUnit.SECONDS.sleep(3);
    }
     */

    @Test
    void testAutocompleteSearchBar () {
        AutocompleteIngredientSearchBar searchBar = new AutocompleteIngredientSearchBar(
                (tile)-> System.out.println(tile.getName()));
        searchBar.setMaximumSize(new Dimension(700, 1000));
        TestPopup.testComponent(searchBar);
    }

    @Test
    void testIngredientSelector () {
        IngredientSelector selector = new IngredientSelector(IngredientSelector.GridLayoutType.horizontal);
        selector.setMinimumSize(new Dimension(400, 200));
        selector.setMaximumSize(new Dimension(1500, 1000));
        TestPopup.testComponent(selector);
    }

    @Test
    void testTabs () {
        TestPopup.testComponent(new Tabs());
    }

    @Test
    void testSearchPane () {
        TestPopup.testComponent(new SearchPane());
    }

    @Test
    void testMakeAmountListPanel () {
        TestPopup.testComponent(new MakeAmountListPanel());
    }

    @Test
    void testAddNewMealRecipePane () {
        TestPopup.testComponent(new AddNewMealRecipePane());
    }

    @Test
    void testAddIngredientPopup () {
        new AddIngredientPopup(null, null, null);
    }

    @Test
    void testGoBackPanel () {
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        panel.add(panel2);
        new GoBackPanel(panel2, new JPanel());
        TestPopup.testComponent(panel);
    }

}
