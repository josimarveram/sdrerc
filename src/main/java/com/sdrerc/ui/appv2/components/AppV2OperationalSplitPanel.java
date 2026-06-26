package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class AppV2OperationalSplitPanel extends JPanel {

    private final Component mainComponent;
    private final Component sideComponent;
    private final JSplitPane splitPane;
    private final int mainMinWidth;
    private final int sideMinWidth;
    private final int sidePreferredWidth;
    private int currentSideWidth;
    private int normalSideWidth;
    private boolean sideVisible;
    private boolean adjustingDivider;
    private boolean sideExpanded;

    public AppV2OperationalSplitPanel(
            Component mainComponent,
            Component sideComponent,
            int mainMinWidth,
            int sideMinWidth,
            int sidePreferredWidth) {
        super(new BorderLayout());
        this.mainComponent = mainComponent;
        this.sideComponent = sideComponent;
        this.mainMinWidth = mainMinWidth;
        this.sideMinWidth = sideMinWidth;
        this.sidePreferredWidth = sidePreferredWidth;
        this.currentSideWidth = sidePreferredWidth;
        this.normalSideWidth = sidePreferredWidth;

        setOpaque(false);
        configureMinimums();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(false);
        splitPane.setResizeWeight(1.0d);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setUI(new SubtleSplitPaneUI());
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                rememberDividerWidth();
            }
        });
        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyDividerLocationLater();
            }
        });

        add(mainComponent, BorderLayout.CENTER);
    }

    public void setSideVisible(boolean visible) {
        if (sideVisible == visible) {
            return;
        }
        if (visible) {
            showSide();
        } else {
            hideSide();
        }
    }

    public boolean isSideVisible() {
        return sideVisible;
    }

    public boolean toggleSideExpanded() {
        setSideExpanded(!sideExpanded);
        return sideExpanded;
    }

    public void setSideExpanded(boolean expanded) {
        if (!sideVisible) {
            sideExpanded = false;
            return;
        }
        if (expanded) {
            if (!sideExpanded) {
                normalSideWidth = clampSideWidth(currentSideWidth);
            }
            currentSideWidth = maxSideWidth();
            sideExpanded = true;
        } else {
            currentSideWidth = normalSideWidth <= 0 ? sidePreferredWidth : normalSideWidth;
            sideExpanded = false;
        }
        applyDividerLocationLater();
    }

    public boolean isSideExpanded() {
        return sideExpanded;
    }

    private void showSide() {
        removeAll();
        splitPane.setLeftComponent(mainComponent);
        splitPane.setRightComponent(sideComponent);
        add(splitPane, BorderLayout.CENTER);
        sideVisible = true;
        revalidate();
        repaint();
        applyDividerLocationLater();
    }

    private void hideSide() {
        rememberDividerWidth();
        if (sideExpanded) {
            currentSideWidth = normalSideWidth <= 0 ? sidePreferredWidth : normalSideWidth;
            sideExpanded = false;
        }
        removeAll();
        splitPane.setLeftComponent(null);
        splitPane.setRightComponent(null);
        add(mainComponent, BorderLayout.CENTER);
        sideVisible = false;
        revalidate();
        repaint();
    }

    private void configureMinimums() {
        if (mainComponent != null) {
            mainComponent.setMinimumSize(new Dimension(mainMinWidth, 0));
        }
        if (sideComponent != null) {
            sideComponent.setMinimumSize(new Dimension(sideMinWidth, 0));
            sideComponent.setPreferredSize(new Dimension(sidePreferredWidth, 0));
        }
    }

    private void applyDividerLocationLater() {
        if (!sideVisible) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                applyDividerLocation();
            }
        });
    }

    private void applyDividerLocation() {
        if (!sideVisible || splitPane.getWidth() <= 0) {
            return;
        }
        if (sideExpanded) {
            currentSideWidth = maxSideWidth();
        }
        int sideWidth = clampSideWidth(currentSideWidth);
        int dividerLocation = splitPane.getWidth() - splitPane.getDividerSize() - sideWidth;
        if (dividerLocation < 0) {
            dividerLocation = 0;
        }
        adjustingDivider = true;
        splitPane.setDividerLocation(dividerLocation);
        adjustingDivider = false;
    }

    private void rememberDividerWidth() {
        if (!sideVisible || adjustingDivider || splitPane.getWidth() <= 0) {
            return;
        }
        int sideWidth = splitPane.getWidth() - splitPane.getDividerLocation() - splitPane.getDividerSize();
        int clamped = clampSideWidth(sideWidth);
        boolean expanded = clamped >= maxSideWidth() - 4;
        currentSideWidth = clamped;
        if (!expanded) {
            normalSideWidth = clamped;
        }
        sideExpanded = expanded;
        if (clamped != sideWidth) {
            applyDividerLocationLater();
        }
    }

    private int clampSideWidth(int desiredWidth) {
        int totalWidth = splitPane.getWidth() > 0 ? splitPane.getWidth() : getWidth();
        if (totalWidth <= 0) {
            return Math.max(sideMinWidth, desiredWidth);
        }
        int available = Math.max(0, totalWidth - splitPane.getDividerSize());
        int availableAfterSideMin = Math.max(0, available - Math.min(sideMinWidth, available));
        int effectiveMainMin = Math.min(Math.max(0, mainMinWidth), availableAfterSideMin);
        int maxSide = Math.max(0, available - effectiveMainMin);
        int minSide = Math.min(sideMinWidth, maxSide);
        return Math.max(minSide, Math.min(desiredWidth, maxSide));
    }

    private int maxSideWidth() {
        int totalWidth = splitPane.getWidth() > 0 ? splitPane.getWidth() : getWidth();
        if (totalWidth <= 0) {
            return Math.max(sideMinWidth, currentSideWidth);
        }
        int available = Math.max(0, totalWidth - splitPane.getDividerSize());
        int availableAfterSideMin = Math.max(0, available - Math.min(sideMinWidth, available));
        int effectiveMainMin = Math.min(Math.max(0, mainMinWidth), availableAfterSideMin);
        return Math.max(0, available - effectiveMainMin);
    }

    private static class SubtleSplitPaneUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            BasicSplitPaneDivider subtleDivider = new BasicSplitPaneDivider(this) {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    g.setColor(AppV2Theme.BORDER_STRONG);
                    for (int y = centerY - 12; y <= centerY + 12; y += 8) {
                        g.fillRoundRect(centerX - 1, y, 3, 3, 3, 3);
                    }
                }
            };
            subtleDivider.setBackground(AppV2Theme.SURFACE_ALT);
            subtleDivider.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, AppV2Theme.BORDER));
            return subtleDivider;
        }
    }
}
