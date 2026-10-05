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
 * feel with the club colors, the logo (from {@code /images/logo.png}) and the
 * common window settings.
 *
 * <p>How to use it in a new window (see the style guide in the README):</p>
 * <ol>
 *   <li>{@link #installLookAndFeel()} is already called once in {@code App}.</li>
 *   <li>Call {@link #applyTo(Window)} in the constructor of the window.</li>
 *   <li>Put a {@link HeaderPanel} at the top ({@code BorderLayout.NORTH}).</li>
 * </ol>
 */
public final class Branding {

    public static final Color NAVY = new Color(0x0B2545);
    public static final Color GOLD = new Color(0xE3A92B);
    public static final Color BACKGROUND = new Color(0xF3F5F9);
    public static final Color TEXT_MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD6DBE4);
    /** Status messages: something is correct / something is wrong. */
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color ERROR = new Color(0xB91C1C);

    /** Base font of the application; titles use bigger/bold versions of it. */
    public static final String FONT_FAMILY = "Tahoma";
    public static final Font DEFAULT_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);

    private static final String LOGO_PATH = "/images/logo.png";
    private static final int[] WINDOW_ICON_SIZES = { 16, 20, 24, 32, 40, 48, 64, 128, 256 };

    private static BufferedImage logo;

    private Branding() {
        // Utility class: not instantiated.
    }

    /**
     * Installs the Nimbus look and feel tinted with the club colors. It must
     * be called once, before creating the first window. If Nimbus is not
     * available, the default look and feel is kept.
     */
    public static void installLookAndFeel() {
        // Nimbus derives the colors of every component from these keys,
        // so they have to be set before installing it.
        // A lighter blue than NAVY: with NAVY the combo boxes looked too dark.
        UIManager.put("nimbusBase", new Color(0x4A72A8));
        UIManager.put("nimbusBlueGrey", new Color(0xB8C2D3));
        UIManager.put("control", BACKGROUND);
        UIManager.put("nimbusSelectionBackground", new Color(0x1F4E8C));
        UIManager.put("nimbusFocus", GOLD);
        UIManager.put("defaultFont", DEFAULT_FONT);

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception exception) {
                    // If it fails, the default look and feel is kept.
                }
                return;
            }
        }
    }

    /**
     * Common settings of every window: the club logo as icon and the club
     * background color.
     */
    public static void applyTo(Window window) {
        window.setIconImages(getWindowIcons());
        if (window instanceof RootPaneContainer container) {
            container.getContentPane().setBackground(BACKGROUND);
        }
    }

    /**
     * Gives the window its default size, reduced if the screen is smaller
     * (for example on small laptops), and centers it on {@code parent}.
     */
    public static void setInitialSize(Window window, int width, int height, Component parent) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        window.setSize(Math.min(width, screen.width), Math.min(height, screen.height));
        window.setLocationRelativeTo(parent);
    }

    /** The logo scaled to {@code size} x {@code size} pixels, or null if it could not be loaded. */
    public static ImageIcon getLogo(int size) {
        BufferedImage original = loadLogo();
        return original == null ? null : new ImageIcon(scale(original, size));
    }

    /** The logo in several sizes, so the system picks the best one for the title bar and taskbar. */
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

    /** Scales down in halving steps, which looks much smoother than a single resize. */
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
