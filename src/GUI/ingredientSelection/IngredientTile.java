package GUI.ingredientSelection;

import javax.swing.*;

public class IngredientTile extends JButton {
    private final int id;
    private final String name;

    public IngredientTile(IngredientTile tile) {
        this(tile.id, tile.name);
    }

    public IngredientTile(int id, String name) {
        this.id = id;
        this.name = name;
        setText(name);
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
