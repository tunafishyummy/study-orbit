package ui.pages;

import data.AppData;
import data.NoteManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Course;
import model.Flashcard;
import model.Note;
import ui.Theme;
import ui.components.RoundedTextField;

// Screen 9: Search across notes and flashcards (each result shows its course)
public class SearchPage extends JPanel {

    public interface Opener {
        void openCourse(Course course);
        void openNote(Note note);
        void openFlashcard(Flashcard card);
    }

    // palette for the badges and highlight
    private static final Color NOTE_BADGE = new Color(0x4DB8FF);
    private static final Color CARD_BADGE = new Color(0xFFB347);
    private static final Color DECK_BADGE = new Color(0x52525B);
    private static final Color HIGHLIGHT = new Color(0x5C4F22);
    private static final Color CHIP_BORDER = new Color(0x4A3FB5);
    private static final Color DIVIDER = new Color(0x52525B);
    private static final Color HOVER = new Color(0x363636);
    private static final int ROW_HEIGHT = 46;

    private final NoteManager manager;
    private final Opener opener;
    private final SearchField searchField = new SearchField();
    private final DefaultListModel<SearchItem> listModel = new DefaultListModel<>();
    private final JList<SearchItem> list = new JList<>(listModel);
    private final JLabel emptyLabel = new JLabel("No matches found.", SwingConstants.CENTER);
    private final CardLayout resultsLayout = new CardLayout();
    private final JPanel resultsHolder = new JPanel(resultsLayout);
    private final Chip allChip = new Chip("All");
    private final Chip notesChip = new Chip("Notes");
    private final Chip cardsChip = new Chip("Flashcards");
    private final Chip courseChip = new Chip("Course: Any");
    private String filter = "All";
    private String courseFilter = null;   // null = any course
    private int hoverRow = -1;
    private final List<SearchItem> noteHits = new ArrayList<>();
    private final List<SearchItem> cardHits = new ArrayList<>();

    public static class SearchItem {
        public final String type;
        public final String title;
        public final String badgeText;    // shown on the right side of the row ("" = none)
        public final Color badgeColor;
        public final Object source;       // the Course, Note or Flashcard behind this row

        public SearchItem(String type, String title, String badgeText, Color badgeColor, Object source) {
            this.type = type;
            this.title = title;
            this.badgeText = badgeText;
            this.badgeColor = badgeColor;
            this.source = source;
        }

        @Override
        public String toString() {
            return "[" + type + "] " + title + (badgeText.isEmpty() ? "" : "  -  " + badgeText);
        }
    }

    // no opener given: results are shown but clicking them does nothing
    public SearchPage() {
        this(null);
    }

    public SearchPage(Opener opener) {
        this.opener = opener;
        this.manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(results(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
                SwingUtilities.invokeLater(searchField::requestFocusInWindow);
            }
        });
        refresh();
    }

    // ---------- layout ----------

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Search");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted("Search your notes and flashcards");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField.setPreferredSize(new Dimension(100, 44));
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        Chip[] typeChips = {allChip, notesChip, cardsChip};
        String[] names = {"All", "Notes", "Flashcards"};
        for (int i = 0; i < typeChips.length; i++) {
            final String name = names[i];
            Chip chip = typeChips[i];
            chip.addActionListener(e -> {
                filter = name;
                showFilter();
            });
            group.add(chip);
            filters.add(chip);
        }
        allChip.setSelected(true);

        courseChip.addActionListener(e -> {
            courseChip.setSelected(false);   // it opens a menu, it is not a toggle
            showCoursePopup();
        });
        filters.add(courseChip);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(14));
        header.add(searchField);
        header.add(Box.createVerticalStrut(12));
        header.add(filters);
        return header;
    }

    private JPanel results() {
        JPanel panel = Theme.card();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        list.setFixedCellHeight(ROW_HEIGHT);
        list.setBackground(Theme.SURFACE);
        list.setOpaque(false);
        list.setCellRenderer(new RowRenderer());
        list.setFocusable(false);
        // click a row: open that result on its own page, then clear the highlight
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAt(e.getPoint());
                if (row >= 0) {
                    openResult(listModel.get(row));
                    list.clearSelection();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                list.repaint();
            }
        });
        // hand cursor + hover shading only while the mouse is over a row
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAt(e.getPoint());
                list.setCursor(Cursor.getPredefinedCursor(row >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (row != hoverRow) {
                    hoverRow = row;
                    list.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        emptyLabel.setFont(Theme.BODY);
        emptyLabel.setForeground(Theme.MUTED);

        resultsHolder.setOpaque(false);
        resultsHolder.add(scroll, "list");
        resultsHolder.add(emptyLabel, "empty");
        panel.add(resultsHolder, BorderLayout.CENTER);
        return panel;
    }

    private int rowAt(Point p) {
        int row = list.locationToIndex(p);
        if (row < 0) return -1;
        Rectangle bounds = list.getCellBounds(row, row);
        return bounds != null && bounds.contains(p) ? row : -1;
    }

    private void openResult(SearchItem item) {
        if (opener == null) return;
        if (item.source instanceof Note) opener.openNote((Note) item.source);
        else if (item.source instanceof Flashcard) opener.openFlashcard((Flashcard) item.source);
        else if (item.source instanceof Course) opener.openCourse((Course) item.source);
    }

    // ---------- course filter ----------

    private void showCoursePopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.SURFACE);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        menu.add(courseMenuItem("Any", null));
        for (Course course : manager.getCourses()) {
            menu.add(courseMenuItem(course.getCode(), course.getCode()));
        }
        menu.show(courseChip, 0, courseChip.getHeight() + 4);
    }

    private JMenuItem courseMenuItem(String label, String code) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(Theme.BODY);
        item.setOpaque(true);
        item.setBackground(Theme.SURFACE);
        item.setForeground(Theme.TEXT);
        item.setBorder(new EmptyBorder(6, 14, 6, 14));
        item.addActionListener(e -> {
            courseFilter = code;
            refresh();
        });
        return item;
    }

    // ---------- searching ----------

    private void refresh() {
        // the chosen course may have been deleted since last time
        if (courseFilter != null && manager.findCourse(courseFilter) == null) {
            courseFilter = null;
        }
        String query = searchField.getText().trim().toLowerCase();

        List<SearchItem> notes = noteHits;
        List<SearchItem> cards = cardHits;
        notes.clear();
        cards.clear();

        for (Note note : manager.getAllNotes()) {
            if (courseFilter != null && !courseFilter.equals(note.getCourseCode())) continue;
            if (matchesNote(note, query)) {
                Course course = manager.findCourse(note.getCourseCode());
                String badge = note.getCourseCode().isEmpty() ? "No course" : note.getCourseCode();
                notes.add(new SearchItem("Note", note.getTitle(), badge,
                        course == null ? DECK_BADGE : Theme.parse(course.getColorHex()), note));
            }
        }

        for (Flashcard f : manager.getFlashcards()) {
            Course course = courseOf(f);
            if (courseFilter != null && (course == null || !courseFilter.equals(course.getCode()))) continue;
            if (matchesFlashcard(f, query)) {
                cards.add(new SearchItem("Card", f.getFront(),
                        course != null ? course.getCode() : "No course",
                        course != null ? Theme.parse(course.getColorHex()) : DECK_BADGE, f));
            }
        }

        allChip.setText("All (" + (notes.size() + cards.size()) + ")");
        notesChip.setText("Notes (" + notes.size() + ")");
        cardsChip.setText("Flashcards (" + cards.size() + ")");
        courseChip.setText("Course: " + (courseFilter == null ? "Any" : courseFilter));
        courseChip.setActive(courseFilter != null);

        showFilter();
    }

    // fills the list for the selected filter and swaps to the "no matches" message when empty
    private void showFilter() {
        listModel.clear();
        if ("All".equals(filter)) {
            noteHits.forEach(listModel::addElement);
            cardHits.forEach(listModel::addElement);
        } else if ("Notes".equals(filter)) {
            noteHits.forEach(listModel::addElement);
        } else {
            cardHits.forEach(listModel::addElement);
        }
        hoverRow = -1;
        resultsLayout.show(resultsHolder, listModel.isEmpty() ? "empty" : "list");
        list.repaint();
    }

    private Course courseOf(Flashcard f) {
        return f.getCourseCode().isEmpty() ? null : manager.findCourse(f.getCourseCode());
    }

    private boolean matchesNote(Note note, String query) {
        if (query.isEmpty()) return true;
        return note.getTitle().toLowerCase().contains(query) ||
               note.getBody().toLowerCase().contains(query) ||
               note.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    private boolean matchesFlashcard(Flashcard f, String query) {
        if (query.isEmpty()) return true;
        return f.getDeckTitle().toLowerCase().contains(query) ||
               f.getFront().toLowerCase().contains(query) ||
               f.getBack().toLowerCase().contains(query) ||
               f.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    // ---------- custom components ----------

    // search box with a magnifier icon and placeholder text
    private static class SearchField extends RoundedTextField {
        SearchField() {
            setFont(Theme.font(Font.PLAIN, 15));
            setBorder(new EmptyBorder(9, 46, 9, 16));
            setCaretColor(Theme.TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int cy = getHeight() / 2;

            // magnifier
            g2.setColor(Theme.TEXT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(17, cy - 9, 12, 12));
            g2.drawLine(27, cy + 1, 32, cy + 6);

            if (getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(Theme.MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Search notes, flashcards...", 46,
                        cy - fm.getHeight() / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // pill shaped filter button
    private static class Chip extends JToggleButton {
        private boolean active;

        Chip(String text) {
            super(text);
            setFont(Theme.font(Font.PLAIN, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 30, fm.getHeight() + 12);
        }

        @Override
        public Dimension getMinimumSize() { return getPreferredSize(); }

        @Override
        public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 12;
            boolean on = isSelected() || active;

            g2.setColor(isSelected() ? Theme.PRIMARY : (active ? new Color(0x2E2A6B) : new Color(0x1F1D3F)));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(on ? Theme.PRIMARY : CHIP_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // one result row: type badge, title with the match highlighted, course badge on the right
    private class RowRenderer extends JComponent implements ListCellRenderer<SearchItem> {
        private SearchItem item;
        private boolean hovered;
        private boolean last;

        @Override
        public Component getListCellRendererComponent(JList<? extends SearchItem> l, SearchItem value,
                                                      int index, boolean selected, boolean focus) {
            item = value;
            hovered = index == hoverRow;
            last = index == l.getModel().getSize() - 1;
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, ROW_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (item == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (hovered) {
                g2.setColor(HOVER);
                g2.fillRoundRect(0, 2, w, h - 4, 10, 10);
            }

            int x = 10;
            int right = w - 10;

            // left badge (Note / Card / Course)
            Font badgeFont = Theme.font(Font.BOLD, 11);
            int typeW = drawBadge(g2, badgeFont, item.type, x, h, badgeColorFor(item.type));
            x += typeW + 14;

            // right badge (course code / deck)
            if (!item.badgeText.isEmpty()) {
                FontMetrics bfm = g2.getFontMetrics(badgeFont);
                int bw = bfm.stringWidth(item.badgeText) + 20;
                drawBadge(g2, badgeFont, item.badgeText, right - bw, h, item.badgeColor);
                right -= bw + 14;
            }

            // title, with every occurrence of the query highlighted
            Font font = Theme.font(Font.BOLD, 14);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String text = ellipsize(item.title, fm, right - x);
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            String q = searchField.getText().trim().toLowerCase();
            if (!q.isEmpty()) {
                String lower = text.toLowerCase();
                int from = 0;
                while ((from = lower.indexOf(q, from)) >= 0) {
                    int hx = x + fm.stringWidth(text.substring(0, from));
                    int hw = fm.stringWidth(text.substring(from, from + q.length()));
                    g2.setColor(HIGHLIGHT);
                    g2.fillRect(hx - 1, baseline - fm.getAscent() - 1, hw + 2, fm.getHeight() + 2);
                    from += q.length();
                }
            }
            g2.setColor(Theme.TEXT);
            g2.drawString(text, x, baseline);

            // divider under every row except the last
            if (!last) {
                g2.setColor(DIVIDER);
                g2.drawLine(0, h - 1, w, h - 1);
            }
            g2.dispose();
        }

        private Color badgeColorFor(String type) {
            if ("Note".equals(type)) return NOTE_BADGE;
            return CARD_BADGE;
        }

        private int drawBadge(Graphics2D g2, Font font, String text, int x, int rowHeight, Color color) {
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(text) + 20;
            int bh = fm.getHeight() + 4;
            int by = (rowHeight - bh) / 2;
            g2.setColor(color);
            g2.fillRoundRect(x, by, bw, bh, bh, bh);
            g2.setColor(readableOn(color));
            g2.drawString(text, x + 10, by + (bh - fm.getHeight()) / 2 + fm.getAscent());
            return bw;
        }

        // dark text on light badges, white text on dark ones
        private Color readableOn(Color c) {
            double lum = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
            return lum > 0.55 ? new Color(0x111111) : Color.WHITE;
        }

        private String ellipsize(String s, FontMetrics fm, int maxWidth) {
            if (maxWidth <= 0) return "";
            if (fm.stringWidth(s) <= maxWidth) return s;
            String dots = "...";
            int end = s.length();
            while (end > 0 && fm.stringWidth(s.substring(0, end) + dots) > maxWidth) end--;
            return s.substring(0, end) + dots;
        }
    }
}