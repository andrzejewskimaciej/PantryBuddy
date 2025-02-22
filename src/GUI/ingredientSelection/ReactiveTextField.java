package GUI.ingredientSelection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.*;

public class ReactiveTextField extends JTextField {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private final Runnable runnable;
    private final int delay;

    public ReactiveTextField(Runnable runnable, int delayMilliseconds, int columns) {
        this.runnable = runnable;
        delay = delayMilliseconds;
        setColumns(columns);
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleCall();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleCall();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleCall();
            }
        });
        addActionListener(_ -> runnable.run());
    }

    private void scheduleCall() {
        if (future != null) future.cancel(false);
        future = executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!getText().isEmpty()) return;
        String placeholderText = "search ingredients";
        g.setColor(g.getColor().darker());
        g.drawString(placeholderText,
                g.getFontMetrics().stringWidth("  "),
                getBaseline(getWidth(),getHeight()));
    }

}
