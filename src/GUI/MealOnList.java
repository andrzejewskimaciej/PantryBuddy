package GUI;

import Style.Theme;
import basicDataTypes.Amount;
import basicDataTypes.Ingredient;
import database.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MealOnList extends JPanel implements Refreshable<MealOnList> {

    private String name, category, area;
    private final int id;
    private boolean isFavourite;
    private BufferedImage image;
    private boolean isImageLoaded;
    private final static int width = Theme.screenDimensions.width / 6;
    private final static int height = Theme.screenDimensions.height / 6;

    public MealOnList(int id, String name, String category, String area,
                      boolean isFavourite, byte[] imageBytes, Container encompasingContainer) {
        super(new BorderLayout(8, 8));
        this.id = id;
        this.name = name;

        this.category = Objects.isNull(category) ? "unavailable" : category;
        this.area = Objects.isNull(area) ? "unavailable" : area;
        this.isFavourite = isFavourite;

//        int compWidth;
//        if (encompasingContainer instanceof SearchPane){
//            compWidth = ((SearchPane) encompasingContainer).getScrollMealsListPanel().getWidth();
//        } else {
//            compWidth = Theme.screenDimensions.width / 2;
//        }

        setMaximumSize(new Dimension(
                //compWidth,
//                encompasingContainer.getWidth(),
                Integer.MAX_VALUE,
                Theme.screenDimensions.height / 8));

//        setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(3, 2, 3, 2), null));
        setBorder(BorderFactory.createLineBorder(
                Theme.inactiveColor, 1, true));

        if (Objects.isNull(imageBytes) || imageBytes.length == 0){
            this.isImageLoaded = false;
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics gr = image.getGraphics();
            gr.setColor(new Color(0, 0, 0, 0));
            gr.fillRect(0, 0, width, height);
        } else{
            try {
                image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                this.isImageLoaded = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        displayYourself();


        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() instanceof MealOnList mealOnList) {
//                                            MealInfoPane.displayMealInfo(mealOnList);
                    mouseExited(e);
                    new GoBackPanel(encompasingContainer,
                            new MealInfoPane(mealOnList));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (e.getSource() instanceof MealOnList) {
                    ((MealOnList) e.getSource()).setBorder(BorderFactory.createLineBorder(
                            Theme.activeColor, 2, true));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getSource() instanceof MealOnList) {
                    ((MealOnList) e.getSource()).setBorder(BorderFactory.createLineBorder(
                            Theme.inactiveColor, 1, true));
                }
            }
        });
    }

    private void displayYourself() {
        JPanel info = createInfoPanel(name, category, area, isFavourite);
        add(info, BorderLayout.CENTER);
        JLabel thumbnail = new JLabel(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_DEFAULT)));
        thumbnail.setPreferredSize(new Dimension(width, height));
        add(thumbnail, BorderLayout.WEST);
        if (isFavourite) {
            JLabel isFavouriteLabel = new JLabel("★ ");
            isFavouriteLabel.setFont(
                    isFavouriteLabel.getFont().deriveFont(Font.BOLD,
                            isFavouriteLabel.getFont().getSize()*2)
            );
            isFavouriteLabel.setForeground(Theme.favouriteYellow);
            add(isFavouriteLabel, BorderLayout.EAST);
        }
    }

    private static JPanel createInfoPanel(String name, String category, String area, boolean isFavourite) {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        if (name.length() > 35) {
            ArrayList<String> arr = splitString(name);
            String name1 = arr.getFirst();
            String name2 = arr.get(1);
            name = "<html>" + name1 + "<br>" + name2 + "</html>";
        }

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD,
                nameLabel.getFont().getSize()*3/2));
        nameLabel.setToolTipText(name);
        info.add(nameLabel);

        JLabel categoryAreaLabel = new JLabel(category + "          " + area);
        info.add(categoryAreaLabel);

        String isFavouriteString = isFavourite? "Favourite meal: True" : "Favourite meal: False";
        JLabel isFavouriteLabel = new JLabel(isFavouriteString);
        info.add(isFavouriteLabel);
        return info;
    }

    private static ArrayList<String> splitString(String msg) {
        ArrayList<String> arr = new ArrayList<>();

        Pattern p = Pattern.compile("\\b.{1," + (35 -1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
            //System.out.println(m.group().trim());
            arr.add(m.group());
        }
        return arr;
    }

    public static JPanel displaySearchedMeals(JPanel tabPane,
                                              ResultSet rs,
                                              JScrollPane scrollPane) throws SQLException {
        return displaySearchedMeals(tabPane, rs, scrollPane, scrollPane.getViewport(), null);
    }

    public static JPanel displaySearchedMeals(JPanel tabPane,
                                            ResultSet rs,
                                            JScrollPane scrollPane,
                                              Container containerInScrollPane,
                                              JPanel targetListingPanel) throws SQLException {

        JPanel newListingPanel = new JPanel();
        newListingPanel.setLayout(new BoxLayout(newListingPanel, BoxLayout.Y_AXIS));

        if (targetListingPanel != null) {
            var components = Arrays.stream(containerInScrollPane.getComponents()).toList();
            if (!components.contains(targetListingPanel)) {
                throw new IllegalArgumentException("The scroll pane does not contain the target listing panel");
            }
            containerInScrollPane.remove(targetListingPanel);
            if (containerInScrollPane.getLayout() instanceof GridBagLayout layout) {
                var constraints = layout.getConstraints(targetListingPanel);
                layout.setConstraints(newListingPanel, constraints);
                constraints.gridx = 0;
                constraints.gridy = components.indexOf(targetListingPanel);
                constraints.weighty = 1;
                constraints.weightx = 1;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.anchor = GridBagConstraints.PAGE_START;
                containerInScrollPane.add(newListingPanel, constraints, components.indexOf(targetListingPanel));
            } else {
                containerInScrollPane.add(newListingPanel, components.indexOf(targetListingPanel));
            }
        } else {
            containerInScrollPane.add(newListingPanel);
        }

        if (rs.next()) {
            SwingWorker<Void, List<MealOnList>> task = new SwingWorker<>(){
                boolean notLastRow;
                @Override
                protected Void doInBackground() throws Exception {
                    do {
                        List<MealOnList> meals = new ArrayList<>();
                        for (int i = 0; i < 9; i++) {
                            var newMeal = new MealOnList(
                                    rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getString("category"),
                                    rs.getString("area"),
                                    rs.getBoolean("is_favourite"),
                                    rs.getBytes("image"),
                                    scrollPane.getParent()
                            );
                            meals.add(newMeal);

                            notLastRow = rs.next();
                            if (!notLastRow) break;
                        }

                        publish(meals);
                    } while (notLastRow);
                    return null;
                }

                @Override
                protected void process(List<List<MealOnList>> chunks) {
                    chunks.stream()
                            .flatMap(List::stream)
                            .forEach(newListingPanel::add);
                    newListingPanel.revalidate();
                    scrollPane.repaint();
                    tabPane.revalidate();
                    tabPane.repaint();
                }

                @Override
                protected void done() {
                    try {
                        if (!notLastRow){
                            tabPane.revalidate();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            EventQueue.invokeLater(task::execute);
        } else {
            newListingPanel.add(
                    createMealLabel(new String[]{"There is no meal meeting the criteria"}, true)
            );

            scrollPane.repaint();
            tabPane.revalidate();
        }
        return newListingPanel;
    }


    public static JPanel createMealLabel(String[] textToDisplay, boolean changeStyle){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String text : textToDisplay) {
            JLabel infoLabel = new JLabel();
            infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN + Font.ITALIC,
                    infoLabel.getFont().getSize() * 2));
            infoLabel.setText(text + "!!!");
            if (changeStyle) {
                infoLabel.setForeground(Color.red);
                infoLabel.setText(text + "!!!");
            }
            else infoLabel.setText(text);
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
        }
//        JLabel label = new JLabel(textToDisplay + "!!!");
//        label.setForeground(Color.red);
//        label.setFont(new Font("Arial", Font.BOLD, label.getFont().getSize()));
        return panel;
    }



    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getArea() {
        return area;
    }

    public boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

//    public void setImageLoaded(boolean imageLoaded) {
//        isImageLoaded = imageLoaded;
//    }

    public ArrayList<Amount> getAmountsList(){
        try {
            String query = "SELECT ingredient_id, amount, priority FROM amounts WHERE meal_id = ?";
            PreparedStatement stmt = DatabaseConnection.conn.prepareStatement(query);
            stmt.setInt(1, getId());
            ResultSet res = stmt.executeQuery();
            ArrayList<Amount> amounts = new ArrayList<>();
            while (res.next()) {
                int ingredientId = res.getInt("ingredient_id");
                String amountValue = res.getString("amount");
                int priority = res.getInt("priority");
                amounts.add(new Amount(Ingredient.findIngredientById(ingredientId), amountValue, priority));
            }
            return amounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage getImageBytes() {

        return image;
    }

    @Override
    public MealOnList getRefreshed() {
        var sql = "SELECT meals.id, name, category, area, is_favourite, image " +
                "FROM meals LEFT JOIN images ON meals.id = images.meal_id " +
                "WHERE meals.id = " + id +" ;";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)){
            rs.next();
            name = rs.getString("name");
            category = rs.getString("category");
            area = rs.getString("area");
            isFavourite = rs.getBoolean("is_favourite");
            var imageBytes= rs.getBytes("image");
            if (Objects.isNull(imageBytes) || imageBytes.length == 0){
                isImageLoaded = false;
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics gr = image.getGraphics();
                gr.setColor(new Color(0, 0, 0, 0));
                gr.fillRect(0, 0, width, height);
            } else{
                try {
                    image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    isImageLoaded = true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        removeAll();
        displayYourself();
        revalidate();
        repaint();
        return this;
    }
}