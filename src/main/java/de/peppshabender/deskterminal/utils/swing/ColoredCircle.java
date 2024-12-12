package de.peppshabender.deskterminal.utils.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Simple colored circle */
public class ColoredCircle extends JPanel {
    private Color color;

    public ColoredCircle(final Color baseColor) {
        this.color = baseColor;

        setPreferredSize(new Dimension(30, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(this.color);
        g2d.fillOval(0, 0, getWidth(), getHeight());
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
    }

    public void repaint(final Color color) {
        this.color = color;
        repaint();
    }
}
