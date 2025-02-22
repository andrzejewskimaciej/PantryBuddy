package GUI;

import GUI.ingredientSelection.IngredientSelector;
import GUI.ingredientSelection.IngredientTile;
import Style.Theme;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class LetsCookPane extends JPanel implements Refreshable<LetsCookPane> {
    private final IngredientSelector selector;
    private final JScrollPane scrollPane;
    private final JPanel panel = new JPanel();
    private JPanel exactMatchesPanel = new JPanel();
    private JPanel oneLessPanel = new JPanel();
    private JPanel essentialsPanel = new JPanel();
    public static ReentrantLock lock = new ReentrantLock();
    public static Condition condition;

    private void addComponent (JComponent component, GridBagLayout layout, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weighty = 0;
        gbc.weightx = 1.0;
        layout.setConstraints(component, gbc);
        panel.add(component);
    }

    public LetsCookPane() {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selector = new IngredientSelector(IngredientSelector.GridLayoutType.horizontal,
                "In your pantry you have:");
        getPantry().forEach(selector::addSelected);
        selector.setPreferredSize(new Dimension(
                Theme.screenDimensions.width/2,
                Theme.screenDimensions.height/5
                ));
        JPanel searchOptionsPanel = new JPanel();
        searchOptionsPanel.setLayout(new BoxLayout(searchOptionsPanel, BoxLayout.X_AXIS));
        searchOptionsPanel.add(selector);
        JPanel buttonPanel = getButtonPanel();
        searchOptionsPanel.add(buttonPanel);
        topPanel.add(searchOptionsPanel);
        add(topPanel, BorderLayout.NORTH);
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        addComponent(exactMatchesPanel, layout, 0);
        addComponent(oneLessPanel, layout, 1);
        addComponent(essentialsPanel, layout, 2);
        scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        add(scrollPane, BorderLayout.CENTER);
        ReentrantLock antilock = new ReentrantLock();
        Condition anticondition = antilock.newCondition();
        antilock.lock();
        SwingUtilities.invokeLater(() -> {
            try {
                condition = lock.newCondition();
                lock.lock();
                antilock.lock();
                anticondition.signal();
                antilock.unlock();
                condition.await();
                lock.unlock();
                LetsCookPane.this.revalidate();
                LetsCookPane.this.repaint();
                LetsCookPane.this.search();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            anticondition.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        antilock.unlock();
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton cookButton = new JButton("Cook");
        cookButton.addActionListener(_ -> search());
        cookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(cookButton);
        JButton savePantryButton = new JButton("Save Pantry");
        savePantryButton.addActionListener(_ -> updatePantry());
        savePantryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(savePantryButton);
        return buttonPanel;
    }

    private void search() {
        var selected = selector.getSelected();
        if (selected.isEmpty()) {return;}
        var ids = selected
                .stream()
                .map(IngredientTile::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        var sqlExact = "SELECT meals.id AS id, name, category, area," +
                " is_favourite, image " +
                "FROM (SELECT *, SUM(ingredient_id IN (" +
                    ids +
                    ")) AS sm, COUNT(*) AS cnt " +
                    "FROM amounts " +
                    "GROUP BY meal_id " +
                    "HAVING cnt = sm) t " +
                "JOIN meals ON t.meal_id = meals.id " +
                "LEFT JOIN images ON meals.id = images.meal_id" +
                " ORDER BY is_favourite DESC";

        var sqlOneLess = "SELECT meals.id AS id, name, category, area," +
                " is_favourite, image " +
                "FROM (SELECT *, SUM(ingredient_id IN (" +
                ids +
                ")) AS sm, COUNT(*) AS cnt " +
                "FROM amounts " +
                "GROUP BY meal_id " +
                "HAVING cnt - 1 = sm) t " +
                "JOIN meals ON t.meal_id = meals.id " +
                "LEFT JOIN images ON meals.id = images.meal_id" +
                " ORDER BY is_favourite DESC";

        var sqlEssentials = "SELECT meals.id AS id, name, category, area," +
                " is_favourite, image " +
                "FROM (SELECT *, SUM(ingredient_id IN (" +
                    ids +
                    ")) AS sm, COUNT(*) AS cnt " +
                    "FROM (" +
                        "SELECT *" +
                        " FROM amounts" +
                        " WHERE priority > 0" +
                    ")" +
                    "GROUP BY meal_id " +
                    "HAVING cnt = sm) t " +
                "JOIN meals ON t.meal_id = meals.id " +
                "LEFT JOIN images ON meals.id = images.meal_id" +
                " EXCEPT " +
                "SELECT * FROM ( " +
                "SELECT meals.id AS id, name, category, area," +
                " is_favourite, image " +
                "FROM (SELECT *, SUM(ingredient_id IN (" +
                ids +
                ")) AS sm, COUNT(*) AS cnt " +
                "FROM amounts " +
                "GROUP BY meal_id " +
                "HAVING cnt = sm) t1 " +
                "JOIN meals ON t1.meal_id = meals.id " +
                "LEFT JOIN images ON meals.id = images.meal_id" +
                " UNION " +
                "SELECT meals.id AS id, name, category, area," +
                " is_favourite, image " +
                "FROM (SELECT *, SUM(ingredient_id IN (" +
                ids +
                ")) AS sm, COUNT(*) AS cnt " +
                "FROM amounts " +
                "GROUP BY meal_id " +
                "HAVING cnt - 1 = sm) t2 " +
                "JOIN meals ON t2.meal_id = meals.id " +
                "LEFT JOIN images ON meals.id = images.meal_id" +
                " )" +
                " ORDER BY is_favourite DESC ";

        try {
            var stmt = DatabaseConnection.conn.createStatement();
            var rs = stmt.executeQuery(sqlExact);
            exactMatchesPanel = MealOnList.displaySearchedMeals(this, rs, scrollPane, panel, exactMatchesPanel);
            var exactMatchesBorder =  BorderFactory.createTitledBorder("You have all the ingredients:");
            Font borderFont = exactMatchesPanel.getFont().deriveFont(Font.BOLD,
                    (float) (exactMatchesPanel.getFont().getSize() * 1.3));
            exactMatchesBorder.setTitleFont(borderFont);
            exactMatchesPanel.setBorder(exactMatchesBorder);
            var rs2 = DatabaseConnection.conn.createStatement().executeQuery(sqlOneLess);
            oneLessPanel = MealOnList.displaySearchedMeals(this, rs2, scrollPane, panel, oneLessPanel);
            var onesLessBorder = BorderFactory.createTitledBorder("One ingredient is missing:");
            onesLessBorder.setTitleFont(borderFont);
            oneLessPanel.setBorder(onesLessBorder);
            var rs3 = DatabaseConnection.conn.createStatement().executeQuery(sqlEssentials);
            essentialsPanel = MealOnList.displaySearchedMeals(this, rs3, scrollPane, panel, essentialsPanel);
            var essentialsBorder = BorderFactory.createTitledBorder("You have the essentials:");
            essentialsBorder.setTitleFont(borderFont);
            essentialsPanel.setBorder(essentialsBorder);
//            MealOnList.displaySearchedMeals(this, rs, scrollPane);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePantry() {
        var selected = selector.getSelected();
        if (selected.isEmpty()) {return;}
        var ids = selected
                .stream()
                .map(IngredientTile::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        var clearPantrySQL = "UPDATE ingredients " +
                "SET is_in_pantry = FALSE " +
                "WHERE is_in_pantry = TRUE";
        var setPantrySQL = "UPDATE ingredients " +
                "SET is_in_pantry = TRUE " +
                "WHERE id IN (" + ids + ")";
        try (var stmt = DatabaseConnection.conn.createStatement()){
            stmt.executeUpdate(clearPantrySQL);
            stmt.executeUpdate(setPantrySQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<IngredientTile> getPantry() {
        List<IngredientTile> ingredients = new ArrayList<>();
        var sql = "SELECT id, name " +
                "FROM ingredients " +
                "WHERE is_in_pantry = TRUE";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)){
            while (rs.next()) {
                ingredients.add(
                        new IngredientTile(
                                rs.getInt("id"),
                                rs.getString("name")
                        ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    @Override
    public LetsCookPane getRefreshed() {
        search();
        return this;
    }
}

/*

SELECT t.*, ingredients.name
FROM (SELECT meal_id, name,ingredient_id
         FROM amounts JOIN meals
      ON meals.id = amounts.meal_id) AS t
         JOIN ingredients
    ON t.ingredient_id = ingredients.id
;
*/

/*
SELECT *
FROM ingredients
WHERE is_in_pantry = TRUE;

*/

/*
SELECT name, COUNT(*) AS cnt
FROM amounts LEFT JOIN meals ON amounts.meal_id = meals.id
GROUP BY meal_id
ORDER BY cnt
 */


/*
SELECT category, COUNT(*)
    FROM meals
    GROUP BY category
 */

/*
SELECT m.name, SUM(i.name IN ()) AS s
FROM meals m JOIN amounts a ON m.id = a.meal_id
    JOIN main.ingredients i ON a.ingredient_id = i.id
WHERE category = ''
GROUP BY m.id
HAVING s = 0
 */

/*
    SELECT name
    FROM ingredients
        WHERE name LIKE '%bean%'
 */

/*
SELECT m.name, i.name
FROM meals m JOIN amounts a ON m.id = a.meal_id
    JOIN main.ingredients i ON a.ingredient_id = i.id
WHERE category = 'Vegan'
 */

/*
SELECT COUNT(*)
FROM meals m JOIN amounts a ON m.id = a.meal_id
    JOIN main.ingredients i ON a.ingredient_id = i.id
WHERE category = 'Chicken'
GROUP BY i.id
 */

/*
SELECT COUNT(*)
FROM meals
WHERE category = 'Goat'
 */

/*
SELECT COUNT(*), SUM(priority)
FROM amounts
 */