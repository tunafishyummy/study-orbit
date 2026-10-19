package ui;

import java.awt.*;
import javax.swing.*;
import ui.pages.*;

public class StudyOrbitApp extends JFrame {

    private CardLayout pageLayout;
    private JPanel pages;
    private String currentPage = "Dashboard";
    private CreateFlashcardPage createFlashcardPage;
    private FlashcardsPage flashcardsPage;

    public StudyOrbitApp() {
        setTitle("Study Orbit");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(buildContent(true));
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void rebuild() {
        Theme.apply();
        setContentPane(buildContent(false));
        revalidate();
        repaint();
    }

    private JPanel buildContent(boolean withSplash) {
        pageLayout = new CardLayout();
        pages = new JPanel(pageLayout);
        pages.setBackground(Theme.BG);

        createFlashcardPage = new CreateFlashcardPage(() -> {
            flashcardsPage.refreshDecks(() -> {
                createFlashcardPage.refreshNotes();
                pageLayout.show(pages, "CreateFlashcard");
            });
            pageLayout.show(pages, "Flashcards");
        });

        flashcardsPage = new FlashcardsPage(() -> {
            createFlashcardPage.refreshNotes();
            pageLayout.show(pages, "CreateFlashcard");
        });

        pages.add(new DashboardPage(), "Dashboard");
        pages.add(new CoursesPage(), "Courses");
        pages.add(new NotesPage(), "Notes");
        pages.add(flashcardsPage, "Flashcards");
        pages.add(createFlashcardPage, "CreateFlashcard");
        pages.add(new SchedulePage(), "Schedule");
        pages.add(new SearchPage(), "Search");
        pages.add(new ProgressPage(), "Progress");
        pages.add(new SettingsPage(this::rebuild), "Settings");

        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(Theme.BG);
        dashboard.add(new NavBar(name -> {
            currentPage = name;
            if (name.equals("CreateFlashcard")) {
                createFlashcardPage.refreshNotes();
            } else if (name.equals("Flashcards")) {
                flashcardsPage.refreshDecks(() -> {
                    createFlashcardPage.refreshNotes();
                    pageLayout.show(pages, "CreateFlashcard");
                });
            }
            pageLayout.show(pages, name);
        }, currentPage), BorderLayout.WEST);
        dashboard.add(pages, BorderLayout.CENTER);

        CardLayout appLayout = new CardLayout();
        JPanel app = new JPanel(appLayout);
        app.setBackground(Theme.BG);
        if (withSplash) {
            app.add(new SplashPage(() -> {
                currentPage = "Dashboard";
                appLayout.show(app, "Dashboard");
                pageLayout.show(pages, "Dashboard");
            }), "Splash");
        }
        app.add(dashboard, "Dashboard");
        if (withSplash) {
            appLayout.show(app, "Splash");
        } else {
            appLayout.show(app, "Dashboard");
            pageLayout.show(pages, currentPage);
        }
        return app;
    }
}
