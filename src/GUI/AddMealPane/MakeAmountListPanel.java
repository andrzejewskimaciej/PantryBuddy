package GUI.AddMealPane;

import Style.Theme;
import basicDataTypes.Ingredient;
import basicDataTypes.Amount;
import database.DatabaseConnection;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class MakeAmountListPanel extends JPanel {
    private final ArrayList<Amount> amountList = new ArrayList<>();
    private final JTextField amountField;
    private final JButton addButton;
    private final JComboBox<String> ingredientComboBox;
    private final JSpinner prioritySpinner;
    private final DefaultTableModel tableModel;
    private final JButton addIngredientButton;
    private final JButton removeButton;
    private final JTable amountTable;
    private final ArrayList<Amount> removedAmounts = new ArrayList<>();


    public MakeAmountListPanel() {
        setLayout(new BorderLayout(8, 8));
        Listener listener = new Listener();

        ingredientComboBox = new JComboBox<>(Ingredient.getAllIngredientsNames());
        AutoCompleteDecorator.decorate(ingredientComboBox);
        ingredientComboBox.setSelectedItem("");
        ingredientComboBox.addKeyListener(listener);
        ingredientComboBox.setBorder(BorderFactory.createTitledBorder("Ingredient name"));
        ingredientComboBox.setEditable(true);

        amountField = new JTextField(10);
        amountField.addKeyListener(listener);
        amountField.setBorder(BorderFactory.createTitledBorder("Amount"));

        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 5, 1);
        prioritySpinner = new JSpinner(spinnerModel);
        prioritySpinner.setEditor(new JSpinner.DefaultEditor(prioritySpinner));
        prioritySpinner.addKeyListener(listener);
        prioritySpinner.setBorder(BorderFactory.createTitledBorder("Priority"));

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(ingredientComboBox);
        inputPanel.add(amountField);
        inputPanel.add(prioritySpinner);

        addButton = new JButton("Add");
        addButton.addActionListener(listener);
        inputPanel.add(addButton);

        JPanel addIngredientPanel = new JPanel(new FlowLayout());

        JLabel label = new JLabel("Ingredient not in list? Add a new one");
        addIngredientPanel.add(label);

        addIngredientButton = new JButton("Add new ingredient");
        addIngredientButton.addActionListener(listener);
        addIngredientPanel.add(addIngredientButton);

        JLabel removeInstructionLabel = new JLabel("Want to remove an ingredient? Select it and then use this button:");
        removeButton = new JButton("Remove");
        removeButton.addActionListener(listener);

        JPanel ingredientDialogPanel = new JPanel();
        ingredientDialogPanel.setLayout(new BoxLayout(ingredientDialogPanel, BoxLayout.Y_AXIS));
        ingredientDialogPanel.add(inputPanel);
        ingredientDialogPanel.add(addIngredientPanel);

        JPanel removeIngredientPanel = new JPanel();
        removeIngredientPanel.setLayout(new BoxLayout(removeIngredientPanel, BoxLayout.Y_AXIS));
        removeInstructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeIngredientPanel.add(removeInstructionLabel);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeIngredientPanel.add(removeButton);

        JPanel topPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        topPanel.setLayout(gridBagLayout);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2;
        gbc.weighty = 1;
        gridBagLayout.setConstraints(ingredientDialogPanel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.gridwidth = 1;
        gridBagLayout.setConstraints(removeIngredientPanel, gbc);

        topPanel.add(ingredientDialogPanel);
        topPanel.add(removeIngredientPanel);
        add(topPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Ingredient", "Amount", "Priority"}, 0);
        amountTable = new JTable(tableModel);
        amountTable.setDefaultEditor(Object.class, null);

        tablePanel.add(new JScrollPane(amountTable), BorderLayout.CENTER);

        add(tablePanel, BorderLayout.SOUTH);

    }

    public void updateSize(){
       amountTable.setPreferredScrollableViewportSize(new Dimension(amountTable.getWidth() * 2/3, Theme.screenDimensions.height / 6));
       repaint();
    }

    public void addAmount(ArrayList<Amount> amounts) {
        amountList.addAll(amounts);
        amounts.forEach(newAmount -> tableModel.addRow(new Object[]{newAmount.ingredient().name(),
                newAmount.amount(),
                newAmount.priority()}));
        repaint();
    }

    public ArrayList<Amount> getAmountList() {
        return amountList;
    }
    public ArrayList<Amount> getRemovedAmounts() {
        return removedAmounts;
    }

    private class Listener implements ActionListener, KeyListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addButton) {
                addAmount();
            } else if (e.getSource() == addIngredientButton) {
                new AddIngredientPopup(null, DatabaseConnection.conn, ingredientComboBox);
            } else if (e.getSource() == removeButton) {
                int selectedRow = amountTable.getSelectedRow();
                if (selectedRow >= 0 && amountTable.getRowCount() > 1) {
                    Amount amountToRemove = amountList.get(selectedRow);
                    amountList.remove(amountToRemove);
                    tableModel.removeRow(selectedRow);
                    removedAmounts.add(amountToRemove);
                } else if (selectedRow == 0 && amountTable.getRowCount() == 1) {
                    JOptionPane.showMessageDialog(MakeAmountListPanel.this, "Could not remove sole ingredient from recipe",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(MakeAmountListPanel.this, "Choose an ingredient before removing it",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        }


        //dodanie nowego Amount do list
        private void addAmount() {
            String name = (String) ingredientComboBox.getSelectedItem();
            String amount = amountField.getText();
            if (Objects.equals(amount, "")) {
                JOptionPane.showMessageDialog(MakeAmountListPanel.this, "Please fill amount field.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int priority = (int) prioritySpinner.getValue();

            Amount newAmount;
            try {
                newAmount = new Amount(Ingredient.findIngredientByName(name), amount, priority);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            Amount finalNewAmount = newAmount;
            if (amountList.parallelStream().anyMatch(a -> a.ingredient().name().equals(finalNewAmount.ingredient().name()))) {
                JOptionPane.showMessageDialog(MakeAmountListPanel.this, "Ingredient already in list.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            amountList.add(newAmount);
            ingredientComboBox.setSelectedItem("");
            amountField.setText("");
            prioritySpinner.setValue(0);
            tableModel.addRow(new Object[]{newAmount.ingredient().name(), newAmount.amount(), newAmount.priority()});
        }


        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            // enter robi to samo co guzik "Add"
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                addAmount();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}