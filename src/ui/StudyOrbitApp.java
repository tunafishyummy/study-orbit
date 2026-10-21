package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import model.Course;
import model.Flashcard;
import model.Note;
import ui.pages.*;

public class StudyOrbitApp extends JFrame {

    private static final int TITLE_BAR_ICON_SIZE = 16;

    private CardLayout pageLayout;
    private JPanel pages;
    private CardLayout appLayout;
    private JPanel app;
    private String currentPage = "Dashboard";
    private CreateFlashcardPage createFlashcardPage;
    private FlashcardsPage flashcardsPage;
    // kept as fields so a search result can reach these later
    private NavBar navBar;
    private NotesPage notesPage;
    private CoursesPage coursesPage;
    private final java.util.List<WindowControlButton> maximizeButtons = new ArrayList<>();
    private Rectangle restoreBounds;
    private boolean maximized;

    public StudyOrbitApp() {
        setTitle("Study Orbit");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setIconImage(Assets.image("images/Logo.png"));
        setContentPane(buildContent(true));
        setSize(1000, 620); // default shrunk size
        setLocationRelativeTo(null);
        restoreBounds = getBounds();
        maximizeWindow();
    }

    public void rebuild() {
        Theme.apply();
        setContentPane(buildContent(false));
        revalidate();
        repaint();
    }

    private SearchPage.Opener searchOpener() {
        return new SearchPage.Opener() {
            @Override
            public void openCourse(Course course) {
                goTo("Courses");
                coursesPage.showCourse(course);
            }

            @Override
            public void openNote(Note note) {
                goTo("Notes");
                notesPage.showNote(note);
            }

            @Override
            public void openFlashcard(Flashcard card) {
                goTo("Flashcards");
                flashcardsPage.showCard(card);
            }
        };
    }

    // switches page and moves the navbar highlight with it
    private void goTo(String name) {
        currentPage = name;
        navBar.select(name);
        pageLayout.show(pages, name);
    }

    private JPanel buildContent(boolean showSplashInitially) {
        maximizeButtons.clear();
        pageLayout = new CardLayout();
        pages = new JPanel(pageLayout);
        pages.setBackground(Theme.BG);

        createFlashcardPage = new CreateFlashcardPage(() -> {
            flashcardsPage.refresh();
            pageLayout.show(pages, "Flashcards");
        });

        flashcardsPage = new FlashcardsPage(() -> {
            createFlashcardPage.refreshNotes();
            pageLayout.show(pages, "CreateFlashcard");
        });

        pages.add(new DashboardPage(), "Dashboard");
        // create these two first and save them, so we can call showCourse / showNote on them
        coursesPage = new CoursesPage();
        notesPage = new NotesPage();
        pages.add(coursesPage, "Courses");
        pages.add(notesPage, "Notes");
        pages.add(flashcardsPage, "Flashcards");
        pages.add(createFlashcardPage, "CreateFlashcard");
        pages.add(new QuizzesPage(), "Quizzes");
        pages.add(new SchedulePage(), "Schedule");
        pages.add(new SearchPage(searchOpener()), "Search");
        pages.add(new ProgressPage(), "Progress");
        pages.add(new SettingsPage(), "Settings");

        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(Theme.BG);
        // saved in navBar so goTo() can move the highlight
        navBar = new NavBar(name -> {
            currentPage = name;
            if (name.equals("CreateFlashcard")) {
                createFlashcardPage.refreshNotes();
            } else if (name.equals("Flashcards")) {
                flashcardsPage.refresh();
            }
            pageLayout.show(pages, name);
        }, currentPage, () -> appLayout.show(app, "Splash"));
        dashboard.add(navBar, BorderLayout.WEST);

        JPanel dashboardContent = new JPanel(new BorderLayout());
        dashboardContent.setBackground(Theme.BG);
        dashboardContent.add(buildWindowBar(), BorderLayout.NORTH);
        dashboardContent.add(pages, BorderLayout.CENTER);
        dashboard.add(dashboardContent, BorderLayout.CENTER);

        appLayout = new CardLayout();
        app = new JPanel(appLayout);
        app.setBackground(Theme.BG);
        JPanel splash = new JPanel(new BorderLayout());
        splash.setBackground(Theme.BG);
        splash.add(buildWindowBar(), BorderLayout.NORTH);
        splash.add(new SplashPage(() -> {
            currentPage = "Dashboard";
            appLayout.show(app, "Dashboard");
            pageLayout.show(pages, "Dashboard");
        }), BorderLayout.CENTER);
        app.add(splash, "Splash");
        app.add(dashboard, "Dashboard");
        if (showSplashInitially) {
            appLayout.show(app, "Splash");
        } else {
            appLayout.show(app, "Dashboard");
            pageLayout.show(pages, currentPage);
        }
        return app;
    }

    private JPanel buildWindowBar() {
        // grab
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG);
        bar.setPreferredSize(new Dimension(1, 36));

        JPanel dragZone = new JPanel();
        dragZone.setOpaque(false);
        installDragging(dragZone);
        bar.add(dragZone, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);
        WindowControlButton minimizeButton = new WindowControlButton(WindowControl.MINIMIZE);
        minimizeButton.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        WindowControlButton maximizeButton = new WindowControlButton(WindowControl.MAXIMIZE);
        maximizeButton.setMaximized(maximized);
        maximizeButton.addActionListener(e -> toggleMaximize());
        maximizeButtons.add(maximizeButton);

        WindowControlButton closeButton = new WindowControlButton(WindowControl.CLOSE);
        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        controls.add(minimizeButton);
        controls.add(maximizeButton);
        controls.add(closeButton);
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    private void installDragging(JComponent dragZone) {
        // remember where the grab started
        dragZone.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point mouse = e.getLocationOnScreen();
                    dragZone.putClientProperty("dragOffset", new Point(mouse.x - getX(), mouse.y - getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isMaximized()) {
                    restoreBounds = getBounds();
                }
                dragZone.putClientProperty("dragOffset", null);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    toggleMaximize();
                }
            }
        });

        dragZone.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point offset = (Point) dragZone.getClientProperty("dragOffset");
                if (offset == null) {
                    return;
                }

                Point mouse = e.getLocationOnScreen();
                if (isMaximized()) {
                    // restore first, then keep the cursor same spot
                    Rectangle bounds = restoreBounds == null
                            ? new Rectangle(100, 100, 1000, 620)
                            : new Rectangle(restoreBounds);
                    double horizontalRatio = getWidth() == 0 ? 0.5 : (double) offset.x / getWidth();
                    maximized = false;
                    setBounds(bounds);
                    offset = new Point((int) Math.round(horizontalRatio * getWidth()), offset.y);
                    dragZone.putClientProperty("dragOffset", offset);
                    updateMaximizeButtons();
                }
                setLocation(mouse.x - offset.x, mouse.y - offset.y);
            }
        });
    }

    private boolean isMaximized() {
        return maximized;
    }

    private void toggleMaximize() {
        if (isMaximized()) {
            restoreWindow();
        } else {
            restoreBounds = getBounds();
            maximizeWindow();
        }
    }

    private void maximizeWindow() {
        // use the usable screen area so the taskbar stays in view
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        Rectangle screen = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        maximized = true;
        setBounds(screen.x + insets.left, screen.y + insets.top,
                screen.width - insets.left - insets.right,
                screen.height - insets.top - insets.bottom);
        updateMaximizeButtons();
    }

    private void restoreWindow() {
        maximized = false;
        if (restoreBounds != null) {
            setBounds(restoreBounds);
        }
        updateMaximizeButtons();
    }

    private void updateMaximizeButtons() {
        for (WindowControlButton button : maximizeButtons) {
            button.setMaximized(maximized);
        }
    }

    private enum WindowControl {
        MINIMIZE("TitleMinimize.png"),
        MAXIMIZE("TitleMaximize.png"),
        CLOSE("TitleClose.png");

        private final String iconName;

        WindowControl(String iconName) {
            this.iconName = iconName;
        }
    }

    private static class WindowControlButton extends JButton {
        private final WindowControl control;

        WindowControlButton(WindowControl control) {
            this.control = control;
            setIcon(loadIcon(control.iconName));
            setPreferredSize(new Dimension(46, 36));
            setFocusable(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(control.name().toLowerCase() + " window");
        }

        void setMaximized(boolean maximized) {
            if (control == WindowControl.MAXIMIZE) {
                String iconName = maximized ? "TitleRestore.png" : control.iconName;
                setIcon(loadIcon(iconName));
            }
        }

        private static ImageIcon loadIcon(String iconName) {
            Image image = Assets.image("icons/" + iconName);
            return new ImageIcon(image.getScaledInstance(
                    TITLE_BAR_ICON_SIZE, TITLE_BAR_ICON_SIZE, Image.SCALE_SMOOTH));
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean highlighted = getModel().isRollover() || getModel().isPressed();
            if (highlighted) {
                g.setColor(control == WindowControl.CLOSE ? new Color(0xC42B1C) : new Color(0x3D3D3D));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }
}