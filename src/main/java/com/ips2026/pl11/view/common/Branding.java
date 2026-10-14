package com.ips2026.pl11.view.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

/**
 * Club look shared by every window of the application: the Nimbus look and
 * feel with the official Ipswich Town FC colors (Royal Blue, Red & White), 
 * the logo (from {@code /images/ipswich.png}) and common window settings.
 */
public final class Branding {

    /** Official Ipswich Town FC Royal Blue (#0044AA). Primary club color. */
    public static final Color NAVY = new Color(0x00, 0x44, 0xAA);

    /** Official Ipswich Town crest crimson red (#DE2C37). */
    public static final Color IPSWICH_RED = new Color(0xDE, 0x2C, 0x37);

    /** 
     * Vibrant highlight blue (#2563EB) replacing gold for active outlines and focus.
     * Retained as GOLD for full backwards compatibility across existing views.
     */
    public static final Color NAVY_DARKER = new Color(0x00, 0x2C, 0x6E);

    /** Pure white used for cards, text contrast, and accents. */
    public static final Color WHITE = Color.WHITE;

    /** Neutral backgrounds and borders. */
    public static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    public static final Color BORDER = new Color(0xCB, 0xD5, 0xE1);

    /** Status indicators: success and error. */
    public static final Color SUCCESS = new Color(0x16, 0x65, 0x34);
    public static final Color ERROR = IPSWICH_RED;

    /** Base font of the application; titles use bigger/bold versions of it. */
    public static final String FONT_FAMILY = "Tahoma";
    public static final Font DEFAULT_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);

    private static final String LOGO_PATH = "/images/ipswich.png";
    private static final int[] WINDOW_ICON_SIZES = { 16, 20, 24, 32, 40, 48, 64, 128, 256 };

    private static BufferedImage logo;

    private Branding() {
        // Utility class: not instantiated.
    }

    /**
     * Installs the Nimbus look and feel styled with Ipswich Town FC colors.
     */
    public static void installLookAndFeel() {
        UIManager.put("nimbusBase", new Color(0x00, 0x44, 0xAA));              
        UIManager.put("nimbusBlueGrey", new Color(0x94, 0xA3, 0xB8));         
        UIManager.put("control", BACKGROUND);                                 
        UIManager.put("nimbusSelectionBackground", new Color(0x00, 0x33, 0x85)); 
        UIManager.put("nimbusFocus", new Color(0x25, 0x63, 0xEB));            
        UIManager.put("defaultFont", DEFAULT_FONT);

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception exception) {
                    // Fallback to default look and feel
                }
                return;
            }
        }
    }

    /**
     * Applies the club logo and window background to any frame or dialog.
     */
    public static void applyTo(Window window) {
        window.setIconImages(getWindowIcons());
        if (window instanceof RootPaneContainer container) {
            container.getContentPane().setBackground(BACKGROUND);
        }
    }

    /**
     * Centers and fits the window safely on the user's screen.
     */
    public static void setInitialSize(Window window, int width, int height, Component parent) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        window.setSize(Math.min(width, screen.width), Math.min(height, screen.height));
        window.setLocationRelativeTo(parent);
    }

    public static ImageIcon getLogo(int size) {
        BufferedImage original = loadLogo();
        return original == null ? null : new ImageIcon(scale(original, size));
    }

    public static List<Image> getWindowIcons() {
        List<Image> icons = new ArrayList<>();
        BufferedImage original = loadLogo();
        if (original != null) {
            for (int size : WINDOW_ICON_SIZES) {
                icons.add(scale(original, size));
            }
        }
        return icons;
    }

    private static synchronized BufferedImage loadLogo() {
        if (logo == null) {
            try (InputStream input = Branding.class.getResourceAsStream(LOGO_PATH)) {
                if (input != null) {
                    logo = ImageIO.read(input);
                }
            } catch (IOException exception) {
                System.err.println("Could not load the logo " + LOGO_PATH + ": " + exception.getMessage());
            }
        }
        return logo;
    }

    private static BufferedImage scale(BufferedImage image, int size) {
        BufferedImage current = image;
        int width = image.getWidth();
        do {
            width = Math.max(size, width / 2);
            BufferedImage next = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = next.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(current, 0, 0, width, width, null);
            graphics.dispose();
            current = next;
        } while (width > size);
        return current;
    }
}