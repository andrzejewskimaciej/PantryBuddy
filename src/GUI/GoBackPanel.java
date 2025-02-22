package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class GoBackPanel extends JPanel implements Refreshable<GoBackPanel> {
    private Container previous;
    private JComponent component;

    public GoBackPanel(Container previous, JComponent component, Supplier<Boolean> shouldGoBack) {
        this.previous = previous;
        setLayout(new BorderLayout());
        JPanel topBar = new JPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton goBackButton = new JButton("◀");
        goBackButton.addActionListener(_ -> {
            if (shouldGoBack != null && !shouldGoBack.get()) {
                return;
            }
            goBackAndRefresh();
        });
        var c =  goBackButton.getAccessibleContext().getAccessibleComponent();
        //System.out.println(c.getFont());
        c.setFont(c.getFont().deriveFont(Font.PLAIN, (float) (c.getFont().getSize() * 1.7)));
        topBar.add(goBackButton);
        add(topBar, BorderLayout.LINE_START);
        setComponent(component);
        var parent = previous.getParent();
        parent.remove(previous);
        parent.add(this);
        parent.revalidate();
        parent.repaint();
    }

    public GoBackPanel(Container previous, JComponent component) {
        this(previous, component, null);
    }

    public void goBack() {
        var parent = getParent();
        parent.remove(this);
        parent.add(previous);
        parent.revalidate();
        parent.repaint();
    }

    public void goBackAndRefresh() {
        if (previous instanceof Refreshable<?>) {
            previous = ((Refreshable<?>) previous).getRefreshed();
        }
        goBack();
    }

    @Override
    public GoBackPanel getRefreshed() {
        if (component instanceof Refreshable<?>) {
            //System.out.println("Previous: "+ component);
            setComponent(((Refreshable<?>) component).getRefreshed());
            //System.out.println("Refreshed: " + component);
            revalidate();
            repaint();
        }
        return this;
    }

    private void setComponent(JComponent newComponent) {
        if (component != null) {
            remove(component);
        }
        component = newComponent;
        add(component, BorderLayout.CENTER);
    }
}
