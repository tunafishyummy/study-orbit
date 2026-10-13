package ui;

import java.awt.*;
import javax.swing.*;
import ui.pages.*;

public class StudyOrbitApp extends JFrame {

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final CardLayout appLayout = new CardLayout();
    private final JPanel app = new JPanel(appLayout);

    public StudyOrbitApp() {
        setTitle("Study Orbit");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        pages.add(new DashboardPage(), "Dashboard");
        pages.add(new CoursesPage(), "Courses");
        pages.add(new NotesPage(), "Notes");
        pages.add(new FlashcardsPage(), "Flashcards");
        pages.add(new QuizzesPage(), "Quizzes");
        pages.add(new SchedulePage(), "Schedule");
        pages.add(new SearchPage(), "Search");
        pages.add(new ProgressPage(), "Progress");
        pages.add(new SettingsPage(), "Settings");

        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.add(new NavBar(name -> pageLayout.show(pages, name)), BorderLayout.WEST);
        dashboard.add(pages, BorderLayout.CENTER);

        app.add(new SplashPage(() -> {
            appLayout.show(app, "Dashboard");
            pageLayout.show(pages, "Dashboard");
        }), "Splash");
        app.add(dashboard, "Dashboard");
        appLayout.show(app, "Splash");
        add(app, BorderLayout.CENTER);
    }
}
