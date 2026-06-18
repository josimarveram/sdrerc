package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class AppV2SideActionPanel extends JPanel {

    private final JPanel sections = new JPanel();
    private final JPanel footer = new JPanel(new BorderLayout());
    private final JPanel leadingSlot = new JPanel(new BorderLayout());
    private final JLabel lblTitle = new JLabel();

    public AppV2SideActionPanel(String title) {
        this(title, null);
    }

    public AppV2SideActionPanel(String title, final Runnable onClose) {
        super(new BorderLayout(0, 14));
        setPreferredSize(new Dimension(420, 0));
        setMinimumSize(new Dimension(380, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        setBackground(AppV2Theme.SURFACE);
        applyAccentBorder(null);

        lblTitle.setText(title);
        lblTitle.setFont(AppV2Theme.fontBold(18));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        leadingSlot.setOpaque(false);
        leadingSlot.setVisible(false);
        JPanel titleContent = new JPanel(new BorderLayout(10, 0));
        titleContent.setOpaque(false);
        titleContent.add(leadingSlot, BorderLayout.WEST);
        titleContent.add(lblTitle, BorderLayout.CENTER);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.add(titleContent, BorderLayout.CENTER);
        if (onClose != null) {
            JButton btnClose = new JButton("X");
            btnClose.setFocusable(false);
            btnClose.setToolTipText("Ocultar panel");
            btnClose.setHorizontalAlignment(SwingConstants.CENTER);
            btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnClose.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            btnClose.setForeground(AppV2Theme.TEXT_SECONDARY);
            btnClose.setBackground(AppV2Theme.SURFACE);
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppV2Theme.BORDER),
                    BorderFactory.createEmptyBorder(4, 9, 4, 9)));
            btnClose.addActionListener(e -> onClose.run());
            titleRow.add(btnClose, BorderLayout.EAST);
        }

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(titleRow, BorderLayout.NORTH);
        header.add(new JSeparator(), BorderLayout.SOUTH);

        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(sections);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        footer.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void addSection(Component section) {
        sections.add(section);
        sections.add(Box.createVerticalStrut(12));
    }

    public void setFooter(Component component) {
        footer.removeAll();
        footer.add(component, BorderLayout.CENTER);
    }

    public void setHeaderLeadingComponent(Component component) {
        leadingSlot.removeAll();
        if (component != null) {
            leadingSlot.add(component, BorderLayout.CENTER);
            leadingSlot.setVisible(true);
        } else {
            leadingSlot.setVisible(false);
        }
        revalidate();
        repaint();
    }

    public void setTitle(String title) {
        lblTitle.setText(title == null || title.trim().isEmpty() ? "Panel" : title.trim());
        revalidate();
        repaint();
    }

    public void setAccentColor(Color accent) {
        applyAccentBorder(accent);
        repaint();
    }

    private void applyAccentBorder(Color accent) {
        Border outline = BorderFactory.createLineBorder(AppV2Theme.BORDER);
        if (accent != null) {
            outline = BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 0, 0, accent),
                    outline);
        }
        setBorder(BorderFactory.createCompoundBorder(
                outline,
                BorderFactory.createEmptyBorder(
                        AppV2Theme.SPACE_LARGE,
                        AppV2Theme.SPACE_LARGE,
                        AppV2Theme.SPACE_LARGE,
                        AppV2Theme.SPACE_LARGE)));
    }
}
