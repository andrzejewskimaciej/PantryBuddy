package Tests;

import Style.Theme;
import org.opentest4j.AssertionFailedError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

public class TestPopup extends JFrame {
    private boolean isCorrect = false;

    private TestPopup(String testName, JComponent jComponent) {
        setSize(Theme.screenDimensions);
        setExtendedState(MAXIMIZED_BOTH);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        JPanel labelPanel = new JPanel(new GridLayout(0, 1));
        labelPanel.add(new JLabel("Test name: " + testName));
        labelPanel.add(new JLabel("Component class: " + jComponent.getClass()));
        add(labelPanel);
        add(jComponent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void addQuestionForm() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JPanel questionPanel = new JPanel();
        questionPanel.add(new JLabel("Does this look correct?"));
        JButton yesButton = new JButton("YES");
        JButton noButton = new JButton("NO");
        questionPanel.add(yesButton);
        questionPanel.add(noButton);
        this.add(questionPanel);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                latch.countDown();
                super.windowClosing(e);
            }
        });
        yesButton.addActionListener(_ -> {
            isCorrect = true;
            latch.countDown();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        noButton.addActionListener(_ -> {
            latch.countDown();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        latch.await();
    }

    public static void testComponent (JComponent jComponent) {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        String testName = st[2].getMethodName();
        TestPopup testPopup = new TestPopup(testName, jComponent);
        try {
            testPopup.addQuestionForm();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!testPopup.isCorrect) {
            throw new AssertionFailedError("Component does not look correct");
        }
    }

    public static JFrame showComponent (JComponent jComponent) {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        String testName = st[2].getMethodName();
        return new TestPopup(testName, jComponent);
    }

}
