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

    private final StretchableSectionsPanel sections = new StretchableSectionsPanel();
    private final JPanel bodySpacer = new JPanel();
    private final JPanel footer = new JPanel(new BorderLayout());
    private final JPanel footerSpacer = new JPanel();
    private final JPanel leadingSlot = new JPanel(new BorderLayout());
    private final JLabel lblTitle = new JLabel();
    private final JLabel lblSubtitle = new JLabel();
    private final JScrollPane scroll;
    private boolean bodyVisible = true;

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

        lblTitle.setText(normalizeTitleText(title));
        lblTitle.setFont(AppV2Theme.fontBold(18));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setFont(AppV2Theme.fontBold(15));
        lblSubtitle.setForeground(AppV2Theme.PRIMARY);
        lblSubtitle.setVisible(false);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leadingSlot.setOpaque(false);
        leadingSlot.setVisible(false);
        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(lblTitle);
        titleText.add(Box.createVerticalStrut(1));
        titleText.add(lblSubtitle);

        JPanel titleContent = new JPanel(new BorderLayout(10, 0));
        titleContent.setOpaque(false);
        titleContent.add(leadingSlot, BorderLayout.WEST);
        titleContent.add(titleText, BorderLayout.CENTER);

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
        bodySpacer.setOpaque(false);
        bodySpacer.setPreferredSize(new Dimension(0, 0));
        sections.add(bodySpacer);

        scroll = new JScrollPane(sections);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        footer.setOpaque(false);
        footerSpacer.setOpaque(false);
        footerSpacer.setPreferredSize(new Dimension(0, 0));
        footer.add(footerSpacer, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void addSection(Component section) {
        sections.remove(bodySpacer);
        sections.add(section);
        sections.add(Box.createVerticalStrut(12));
        sections.add(bodySpacer);
        sections.revalidate();
        sections.repaint();
    }

    public void setFooter(Component component) {
        footer.removeAll();
        footer.add(footerSpacer, BorderLayout.SOUTH);
        footer.add(component, BorderLayout.CENTER);
    }

    public void setFooterSpacerHeight(int height) {
        int safeHeight = Math.max(0, height);
        footerSpacer.setPreferredSize(new Dimension(0, safeHeight));
        footerSpacer.setMinimumSize(new Dimension(0, safeHeight));
        footerSpacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, safeHeight));
        footer.revalidate();
        footer.repaint();
    }

    public void setBodySpacerHeight(int height) {
        int safeHeight = Math.max(0, height);
        bodySpacer.setPreferredSize(new Dimension(0, safeHeight));
        bodySpacer.setMinimumSize(new Dimension(0, safeHeight));
        bodySpacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, safeHeight));
        sections.revalidate();
        sections.repaint();
    }

    public void setBodyVisible(boolean visible) {
        bodyVisible = visible;
        scroll.setVisible(visible);
        footer.setVisible(visible);
        revalidate();
        repaint();
    }

    public boolean isBodyVisible() {
        return bodyVisible;
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
        lblTitle.setText(normalizeTitleText(title));
        revalidate();
        repaint();
    }

    public void setSubtitle(String subtitle) {
        String value = subtitle == null ? "" : subtitle.trim();
        lblSubtitle.setText(value);
        lblSubtitle.setVisible(!value.isEmpty());
        revalidate();
        repaint();
    }

    public void refreshLayoutNow() {
        sections.revalidate();
        sections.doLayout();
        sections.repaint();
        scroll.revalidate();
        scroll.doLayout();
        scroll.repaint();
        footer.revalidate();
        footer.repaint();
        revalidate();
        doLayout();
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

    private static String normalizeTitleText(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            text = "Panel";
        }
        if (text.regionMatches(true, 0, "<html>", 0, 6)) {
            return text;
        }
        return "<html><div style='font-size:18px;font-weight:700;color:#1c242e;'>"
                + escapeHtml(text)
                + "</div></html>";
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class StretchableSectionsPanel extends JPanel implements javax.swing.Scrollable {

        private StretchableSectionsPanel() {
            super();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
