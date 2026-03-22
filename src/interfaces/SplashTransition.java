package interfaces;

import interfaces.theme.PharmTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import entite.Employe;

/**
 * SplashTransition — animated full-screen transition from LoginFrame to MainFrame.
 *
 * Sequence (total ~1.8s):
 *  0–200ms  : Login frame fades out
 *  200–600ms: Green wave expands from center
 *  600–1000ms: Pharmacy cross + welcome text animate in
 *  1000–1400ms: Particles burst outward
 *  1400–1800ms: MainFrame fades in through the overlay
 */
public class SplashTransition extends JWindow {

    private final Employe employe;
    private final JFrame  loginFrame;

    // Animation state
    private float phase         = 0f;   // 0..1 overall progress
    private float circleRadius  = 0f;
    private float crossAlpha    = 0f;
    private float textAlpha     = 0f;
    private float particleAlpha = 0f;
    private float mainAlpha     = 0f;

    private static final int TOTAL_MS = 3500;
    private static final int FPS      = 60;
    private static final int TICK_MS  = 1000 / FPS;

    // Particles
    private final float[] px, py, vx, vy, pa;
    private static final int N_PARTICLES = 80;

    private Timer timer;
    private long  startTime;
    private MainFrame mainFrame;

    public SplashTransition(JFrame loginFrame, Employe employe) {
        this.loginFrame = loginFrame;
        this.employe    = employe;

        // Init particles
        px = new float[N_PARTICLES]; py = new float[N_PARTICLES];
        vx = new float[N_PARTICLES]; vy = new float[N_PARTICLES];
        pa = new float[N_PARTICLES];
        for (int i = 0; i < N_PARTICLES; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2.5 + Math.random() * 4.5;
            vx[i] = (float)(Math.cos(angle) * speed);
            vy[i] = (float)(Math.sin(angle) * speed);
            pa[i] = 1f;
        }
    }

    public void start() {
        // Cover entire screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screen = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(screen);
        setBackground(new Color(0, 0, 0, 0));

        // Build main frame in background
        SwingWorker<MainFrame, Void> builder = new SwingWorker<>() {
            @Override protected MainFrame doInBackground() {
                return new MainFrame(employe);
            }
            @Override protected void done() {
                try { mainFrame = get(); } catch (Exception ignored) {}
            }
        };
        builder.execute();

        // Canvas
        JPanel canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                paintTransition((Graphics2D) g);
            }
        };
        canvas.setOpaque(false);
        setContentPane(canvas);
        setVisible(true);

        startTime = System.currentTimeMillis();
        timer = new Timer(TICK_MS, e -> tick());
        timer.start();
    }

    private void tick() {
        long elapsed = System.currentTimeMillis() - startTime;
        phase = Math.min(1f, (float) elapsed / TOTAL_MS);

        // Phase breakdown
        // 0.00–0.15 → login fade out
        // 0.10–0.45 → circle expands
        // 0.35–0.60 → cross fades in
        // 0.50–0.70 → text fades in
        // 0.55–0.80 → particles burst
        // 0.75–1.00 → main frame fades in

        float loginAlpha = 1f - easeIn(clamp(phase / 0.10f));
        circleRadius     = easeOut(clamp((phase - 0.08f) / 0.27f));
        crossAlpha       = easeOut(clamp((phase - 0.30f) / 0.22f));
        textAlpha        = easeOut(clamp((phase - 0.45f) / 0.18f));
        particleAlpha    = easeOut(clamp((phase - 0.50f) / 0.12f))
                         * (1f - easeIn(clamp((phase - 0.80f) / 0.12f)));
        mainAlpha        = easeOut(clamp((phase - 0.78f) / 0.22f));

        // Move particles
        if (particleAlpha > 0) {
            for (int i = 0; i < N_PARTICLES; i++) {
                px[i] += vx[i] * 1.4f;
                py[i] += vy[i] * 1.4f;
                pa[i] = Math.max(0, pa[i] - 0.008f);
            }
        }

        // Fade login frame
        try {
            loginFrame.setOpacity(Math.max(0f, loginAlpha));
        } catch (Exception ignored) {}

        repaint();

        if (phase >= 1f) {
            timer.stop();
            finish();
        }
    }

    private void finish() {
        loginFrame.dispose();
        dispose();
        if (mainFrame != null) {
            mainFrame.setOpacity(1f);
            mainFrame.setVisible(true);
        }
    }

    private void paintTransition(Graphics2D g2) {
        int W = getWidth(), H = getHeight();
        int cx = W / 2, cy = H / 2;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Clear
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, W, H);

        if (circleRadius <= 0 && mainAlpha <= 0) return;

        // Max radius to cover screen
        float maxR = (float) Math.sqrt(W * W + H * H) / 2f + 20;
        float r    = circleRadius * maxR;

        // ── Expanding circle ──────────────────────────────────────────
        if (r > 0) {
            // Gradient: dark green center → mid green edge
            RadialGradientPaint rgp = new RadialGradientPaint(
                cx, cy, Math.max(1, r),
                new float[]{0f, 1f},
                new Color[]{PharmTheme.PM_800, new Color(0x12543F)}
            );
            g2.setPaint(rgp);
            g2.fillOval((int)(cx - r), (int)(cy - r), (int)(r * 2), (int)(r * 2));
        }

        // ── Main frame reveal (white wash fading in) ──────────────────
        if (mainAlpha > 0) {
            g2.setColor(new Color(1f, 1f, 1f, mainAlpha * 0.92f));
            g2.fillRect(0, 0, W, H);
        }

        // ── Particles (lime dots) ─────────────────────────────────────
        if (particleAlpha > 0) {
            for (int i = 0; i < N_PARTICLES; i++) {
                float alpha = pa[i] * particleAlpha;
                if (alpha <= 0.01f) continue;
                float size = 4f + (i % 5) * 2f;
                Color col = (i % 3 == 0) ? PharmTheme.ACC
                          : (i % 3 == 1) ? new Color(0xFFFFFF)
                          : PharmTheme.PM_400;
                g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(),
                    (int)(alpha * 220)));
                g2.fillOval((int)(cx + px[i] * 8 - size/2),
                            (int)(cy + py[i] * 8 - size/2),
                            (int)size, (int)size);
            }
        }

        // ── Cross / logo ──────────────────────────────────────────────
        if (crossAlpha > 0 && mainAlpha < 0.85f) {
            float alpha = crossAlpha * (1f - mainAlpha);
            drawCross(g2, cx, cy, alpha);
        }

        // ── Welcome text ──────────────────────────────────────────────
        if (textAlpha > 0 && mainAlpha < 0.65f) {
            float alpha = textAlpha * (1f - mainAlpha * 1.4f);
            if (alpha > 0) drawText(g2, cx, cy, alpha);
        }
    }

    private void drawCross(Graphics2D g2, int cx, int cy, float alpha) {
        // Glowing background circle
        float glowR = 70 + (1f - crossAlpha) * 20;
        g2.setPaint(new RadialGradientPaint(cx, cy, glowR,
            new float[]{0f, 1f},
            new Color[]{
                new Color(200, 245, 106, (int)(alpha * 60)),
                new Color(200, 245, 106, 0)
            }));
        g2.fillOval((int)(cx-glowR), (int)(cy-glowR), (int)(glowR*2), (int)(glowR*2));

        // Cross arms
        int thick = 12, arm = 36;
        // Scale from 0→full
        float scale = crossAlpha;
        int a = (int)(arm * scale), t = (int)(thick * Math.min(1f, scale * 2));
        if (a < 2 || t < 2) return;

        // Shadow
        g2.setColor(new Color(0, 0, 0, (int)(alpha * 40)));
        g2.fillRoundRect(cx - t/2 + 2, cy - a + 2, t, a*2, 4, 4);
        g2.fillRoundRect(cx - a + 2, cy - t/2 + 2, a*2, t, 4, 4);

        // Lime cross
        Color crossColor = new Color(
            PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(),
            (int)(alpha * 255));
        g2.setColor(crossColor);
        g2.fillRoundRect(cx - t/2, cy - a, t, a*2, 4, 4);
        g2.fillRoundRect(cx - a, cy - t/2, a*2, t, 4, 4);

        // Inner white highlight
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 80)));
        g2.fillRoundRect(cx - t/4, cy - a + 4, t/2, (int)(a * 0.6f), 2, 2);
    }

    private void drawText(Graphics2D g2, int cx, int cy, float alpha) {
        int textY = cy + 75;

        // ── Decorative line above ──
        int lineW = 160;
        g2.setColor(new Color(PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(),
            (int)(alpha * 160)));
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawLine(cx - lineW/2, textY - 16, cx + lineW/2, textY - 16);

        // ── "Pharmacie" — smaller label ──
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        FontMetrics fm = g2.getFontMetrics();
        String label = "PHARMACIE";
        int tw = fm.stringWidth(label);
        // Letter spacing effect — draw char by char
        g2.setColor(new Color(200, 245, 106, (int)(alpha * 180)));
        int lx = cx - tw/2;
        for (char c : label.toCharArray()) {
            g2.drawString(String.valueOf(c), lx, textY);
            lx += fm.charWidth(c) + 2;
        }

        // ── "Ben Abdallah" — large bold ──
        g2.setFont(new Font("SansSerif", Font.BOLD, 46));
        fm = g2.getFontMetrics();
        String mainName = "Ben Abdallah";
        tw = fm.stringWidth(mainName);
        // Shadow
        g2.setColor(new Color(0, 0, 0, (int)(alpha * 80)));
        g2.drawString(mainName, cx - tw/2 + 3, textY + fm.getAscent() + 2);
        // Main
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 240)));
        g2.drawString(mainName, cx - tw/2, textY + fm.getAscent());

        // ── Decorative line below ──
        g2.setColor(new Color(PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(),
            (int)(alpha * 160)));
        g2.drawLine(cx - lineW/2, textY + fm.getAscent() + 18, cx + lineW/2, textY + fm.getAscent() + 18);

        int belowY = textY + fm.getAscent() + 36;

        // ── Welcome line ──
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        String sub = "Bienvenue, " + employe.getPrenom() + " " + employe.getNom();
        tw = fm.stringWidth(sub);
        g2.setColor(new Color(200, 245, 106, (int)(alpha * 200)));
        g2.drawString(sub, cx - tw/2, belowY);

        // ── System tagline ──
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        String tag = "Système de gestion · PharmaSys";
        tw = fm.stringWidth(tag);
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 100)));
        g2.drawString(tag, cx - tw/2, belowY + 24);

        // ── Loading dots ──
        float dotPhase = (phase - 0.45f) / 0.45f;
        if (dotPhase > 0) {
            int dotY = belowY + 52;
            for (int i = 0; i < 3; i++) {
                float dotA = clamp(dotPhase * 3 - i * 0.3f);
                float pulse = (float)(0.6f + 0.4f * Math.sin(System.currentTimeMillis() / 180.0 + i * 1.3));
                int dotSize = (int)(9 * pulse);
                g2.setColor(new Color(
                    PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(),
                    (int)(dotA * alpha * 220)));
                g2.fillOval(cx - 22 + i*22 - dotSize/2, dotY - dotSize/2, dotSize, dotSize);
            }
        }
    }

    // ── Easing ────────────────────────────────────────────────────────────
    private float easeOut(float t) { return 1f - (1f-t)*(1f-t); }
    private float easeIn(float t)  { return t * t; }
    private float clamp(float t)   { return Math.max(0f, Math.min(1f, t)); }
}