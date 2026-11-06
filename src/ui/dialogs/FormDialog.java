package ui.dialogs;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import ui.Assets;
import ui.Theme;

public final class FormDialog {
    private FormDialog() {}

    public static boolean show(Component parent, String title, JPanel form) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        root.add(heading, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        boolean[] accepted = { false };
        JButton cancel = Theme.button("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = Theme.primaryButton("OK");
        ok.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(ok);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setResizable(false);
        dialog.getRootPane().setDefaultButton(ok);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        Image icon = Assets.image("images/Logo.png");
        if (icon != null) {
            dialog.setIconImage(icon);
        }
        dialog.pack();
        dialog.setLocationRelativeTo(owner != null ? owner : parent);
        dialog.setVisible(true);
        return accepted[0];
    }
}
