package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.util.DateRangePickerSupport;
import com.toedter.calendar.JDateChooser;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PremiumDateFieldV2 extends JPanel {

    private static final Locale LOCALE_ES_PE = new Locale("es", "PE");
    private static final int ARC = 18;
    private static final Dimension PREFERRED = new Dimension(240, 40);

    private final JDateChooser chooser = new JDateChooser();
    private boolean hover;
    private boolean focus;

    public PremiumDateFieldV2() {
        super(new BorderLayout());
        setOpaque(false);
        setBackground(AppV2Theme.INPUT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setPreferredSize(PREFERRED);
        setMinimumSize(new Dimension(180, PREFERRED.height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, PREFERRED.height));

        configurarChooser();
        add(chooser, BorderLayout.CENTER);
        conectarEventos();
    }

    private void configurarChooser() {
        chooser.setOpaque(false);
        chooser.setBackground(AppV2Theme.INPUT_BACKGROUND);
        chooser.setLocale(LOCALE_ES_PE);
        chooser.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        chooser.setPreferredSize(PREFERRED);
        chooser.setDateFormatString("dd/MM/yyyy");
        DateRangePickerSupport.configurePicker(chooser);

        if (chooser.getCalendarButton() != null) {
            JButton button = chooser.getCalendarButton();
            button.setIcon(calendarIcon());
            button.setToolTipText("Abrir calendario");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setPreferredSize(new Dimension(38, PREFERRED.height - 4));
            button.setMinimumSize(new Dimension(38, PREFERRED.height - 4));
            button.setMaximumSize(new Dimension(38, PREFERRED.height - 4));
            button.setMargin(new java.awt.Insets(6, 6, 6, 6));
        }

        if (chooser.getDateEditor() != null && chooser.getDateEditor().getUiComponent() instanceof JComponent) {
            JComponent editor = (JComponent) chooser.getDateEditor().getUiComponent();
            editor.setOpaque(false);
            editor.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
            editor.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            editor.setBackground(AppV2Theme.INPUT_BACKGROUND);
            editor.setForeground(AppV2Theme.TEXT_PRIMARY);
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }

        if (chooser.getJCalendar() != null) {
            chooser.getJCalendar().setLocale(LOCALE_ES_PE);
            chooser.getJCalendar().setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            chooser.getJCalendar().setWeekOfYearVisible(false);
            chooser.getJCalendar().setTodayButtonVisible(true);
            chooser.getJCalendar().setNullDateButtonVisible(false);
            chooser.getJCalendar().setDecorationBackgroundColor(AppV2Theme.SURFACE_ALT);
            chooser.getJCalendar().setDecorationBackgroundVisible(true);
            chooser.getJCalendar().setDecorationBordersVisible(false);
            chooser.getJCalendar().setSundayForeground(AppV2Theme.ERROR);
            chooser.getJCalendar().setPreferredSize(new Dimension(320, 290));
            if (chooser.getJCalendar().getDayChooser() != null) {
                chooser.getJCalendar().getDayChooser().setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
                chooser.getJCalendar().getDayChooser().setBackground(AppV2Theme.SURFACE);
                chooser.getJCalendar().getDayChooser().setForeground(AppV2Theme.TEXT_PRIMARY);
                chooser.getJCalendar().getDayChooser().setDecorationBackgroundColor(AppV2Theme.SURFACE_ALT);
                chooser.getJCalendar().getDayChooser().setDecorationBackgroundVisible(true);
                chooser.getJCalendar().getDayChooser().setDecorationBordersVisible(false);
                chooser.getJCalendar().getDayChooser().setDayBordersVisible(false);
                chooser.getJCalendar().getDayChooser().setSundayForeground(AppV2Theme.ERROR);
            }
            if (chooser.getJCalendar().getMonthChooser() != null) {
                chooser.getJCalendar().getMonthChooser().setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
                chooser.getJCalendar().getMonthChooser().setBackground(AppV2Theme.SURFACE_ALT);
                chooser.getJCalendar().getMonthChooser().setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            if (chooser.getJCalendar().getYearChooser() != null) {
                chooser.getJCalendar().getYearChooser().setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
                chooser.getJCalendar().getYearChooser().setBackground(AppV2Theme.SURFACE_ALT);
                chooser.getJCalendar().getYearChooser().setForeground(AppV2Theme.TEXT_PRIMARY);
            }
        }
    }

    private void conectarEventos() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirCalendario();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(false);
            }
        };

        addMouseListener(mouseAdapter);

        if (chooser.getDateEditor() != null && chooser.getDateEditor().getUiComponent() != null) {
            chooser.getDateEditor().getUiComponent().addMouseListener(mouseAdapter);
            chooser.getDateEditor().getUiComponent().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setFocus(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setFocus(false);
                }
            });
        }

        if (chooser.getCalendarButton() != null) {
            chooser.getCalendarButton().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setHover(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setHover(false);
                }
            });
        }
    }

    public Date getDate() {
        return chooser.getDate();
    }

    public void setDate(Date date) {
        chooser.setDate(date);
    }

    public void setDateFormatString(String pattern) {
        chooser.setDateFormatString(pattern);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        chooser.setEnabled(enabled);
        if (!enabled) {
            hover = false;
            focus = false;
        }
        repaint();
    }

    public boolean isFieldEmpty() {
        return chooser.getDate() == null;
    }

    public void addDateChangeListener(java.beans.PropertyChangeListener listener) {
        chooser.addPropertyChangeListener("date", listener);
    }

    public JDateChooser getDateChooser() {
        return chooser;
    }

    public void setSelectableDateRange(Date min, Date max) {
        chooser.setSelectableDateRange(min, max);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color background = isEnabled() ? AppV2Theme.INPUT_BACKGROUND : AppV2Theme.SURFACE_ALT;
        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC));

        Color border = !isEnabled()
                ? AppV2Theme.BORDER
                : focus
                        ? AppV2Theme.INPUT_BORDER_FOCUS
                        : hover
                                ? AppV2Theme.INPUT_BORDER_HOVER
                                : AppV2Theme.INPUT_BORDER;
        g2.setColor(border);
        g2.setStroke(new BasicStroke(focus ? 1.8f : 1.1f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, ARC, ARC));

        g2.dispose();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (chooser != null) {
            chooser.setBackground(bg);
        }
    }

    private void abrirCalendario() {
        if (!isEnabled() || chooser.getCalendarButton() == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> chooser.getCalendarButton().doClick());
    }

    private void setHover(boolean hover) {
        if (this.hover == hover) {
            return;
        }
        this.hover = hover;
        repaint();
    }

    private void setFocus(boolean focus) {
        if (this.focus == focus) {
            return;
        }
        this.focus = focus;
        repaint();
    }

    private static ImageIcon calendarIcon() {
        URL url = PremiumDateFieldV2.class.getResource("/com/sdrerc/ui/iconos/icono_calendar-plus.png");
        if (url != null) {
            try {
                BufferedImage image = ImageIO.read(url);
                if (image != null) {
                    return new ImageIcon(image.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            } catch (Exception ignored) {
            }
            return new ImageIcon(url);
        }
        return null;
    }
}
