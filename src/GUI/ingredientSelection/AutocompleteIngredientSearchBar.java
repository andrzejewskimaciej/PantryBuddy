package GUI.ingredientSelection;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.function.Consumer;

public class AutocompleteIngredientSearchBar extends JPanel {
    private final ReactiveTextField textField = new ReactiveTextField(this::search, 200, 30);
    private final JPanel suggestionsPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
    private final Consumer<IngredientTile> searchAction;

    public AutocompleteIngredientSearchBar(Consumer<IngredientTile> selectionAction) {
        this.searchAction = selectionAction;
        setLayout(new BorderLayout());
        suggestionsPanel.setBorder(BorderFactory.createLineBorder(
                textField.getBackground(),
                5,
                true
        ));
        suggestionsPanel.setBackground(suggestionsPanel.getBackground().darker());
        add(textField, BorderLayout.NORTH);
        add(suggestionsPanel, BorderLayout.CENTER);
    }


    private void search() {
        String searchTerm = textField.getText();
        int suggestionsNumberLimit = 10;
        suggestionsPanel.removeAll();
        if (!searchTerm.isEmpty()) {
            var sql =
                    "SELECT id, name " +
                            "FROM ingredients " +
                            "WHERE name LIKE '%" + searchTerm + "%'" +
                            "LIMIT " + suggestionsNumberLimit + ";";
            try (var stmt = DatabaseConnection.conn.createStatement();
                 var rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    IngredientTile suggestionTile = new IngredientTile(
                            rs.getInt("id"),
                            rs.getString("name"));
                    suggestionTile.addActionListener(_ -> searchAction.accept(suggestionTile));
                    suggestionsPanel.add(suggestionTile);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }
}
