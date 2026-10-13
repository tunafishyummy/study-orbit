import java.awt.*;
import javax.swing.*;

public class FlashcardsPage extends JPanel {

    public FlashcardsPage() {
        setLayout(new BorderLayout());

        add(new JLabel("Flashcards", SwingConstants.CENTER), BorderLayout.CENTER);
    }

}