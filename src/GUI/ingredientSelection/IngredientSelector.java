package GUI.ingredientSelection;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientSelector extends JPanel {

    AutocompleteIngredientSearchBar searchBar = new AutocompleteIngredientSearchBar(this::addSelected);
    JPanel selectedPanel = new JPanel();
    JLabel selectedLabel = new JLabel("Selected:");

    public enum GridLayoutType{vertical, horizontal}

    public IngredientSelector(GridLayoutType gridLayoutType, String selectedLabelText) {
        this(gridLayoutType);
        this.selectedLabel.setText(selectedLabelText);
    }

    public IngredientSelector(GridLayoutType gridLayoutType) {
        if (gridLayoutType == GridLayoutType.horizontal) {
            setLayout(new GridLayout(0, 2, 10, 0));
        } else {
            setLayout(new GridLayout(2, 0, 0, 10));
        }
        selectedPanel.setLayout(new BoxLayout(selectedPanel, BoxLayout.Y_AXIS));
        add(searchBar);
        JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerBar.add(selectedLabel);
        JPanel rightPanel = new JPanel(new BorderLayout());
        Color brighter = headerBar.getBackground().brighter();
        headerBar.setBackground(brighter);
        selectedPanel.setBorder(BorderFactory.createLineBorder(brighter, 4, true));
        rightPanel.add(headerBar, BorderLayout.NORTH);
        var selectedScrollPane = new JScrollPane(selectedPanel);
        selectedScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        rightPanel.add(selectedScrollPane);
        add(rightPanel);
    }

    public void addSelected(IngredientTile tile) {
        if(getSelected().stream()
                .anyMatch(t -> t.getId()==tile.getId())) return;
        IngredientTile selectedTile = new IngredientTile(tile);
        selectedTile.addActionListener(_ -> {
            selectedPanel.remove(selectedTile);
            selectedPanel.revalidate();
            selectedPanel.repaint();
        });
        selectedTile.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedPanel.add(selectedTile);
        selectedPanel.revalidate();
        selectedPanel.repaint();
    }

    public List<IngredientTile> getSelected() {
        return Arrays.stream(selectedPanel.getComponents())
                .filter(component -> component.getClass().equals(IngredientTile.class))
                .map(component -> (IngredientTile) component)
                .collect(Collectors.toList());
    }

}
