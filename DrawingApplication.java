import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

abstract class Shape {
    protected int x, y;
    protected Color color;

    public Shape(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public abstract void draw(Graphics2D g);
    public abstract void updateSize(int x, int y);
}

class Circle extends Shape {
    private int radius;

    public Circle(int x, int y, Color color) {
        super(x, y, color);
        this.radius = 0;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    @Override
    public void updateSize(int x, int y) {
        this.radius = (int) Math.hypot(x - this.x, y - this.y);
    }
}

class Rectangle extends Shape {
    private int width, height;

    public Rectangle(int x, int y, Color color) {
        super(x, y, color);
        this.width = 0;
        this.height = 0;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    @Override
    public void updateSize(int x, int y) {
        this.width = x - this.x;
        this.height = y - this.y;
    }
}

class Line extends Shape {
    private int x2, y2;

    public Line(int x, int y, Color color) {
        super(x, y, color);
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.drawLine(x, y, x2, y2);
    }

    @Override
    public void updateSize(int x, int y) {
        this.x2 = x;
        this.y2 = y;
    }
}

class DrawingPanel extends JPanel {
    private Color primaryColor = Color.BLACK;
    private Color secondaryColor = Color.WHITE;
    private Shape currentShape;
    private BufferedImage loadedImage;
    private List<Shape> shapes = new ArrayList<>();
    private String selectedShape = "Circle";

    public DrawingPanel() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedShape != null) {
                    currentShape = createShape(e.getX(), e.getY(), primaryColor);
                    shapes.add(currentShape);
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentShape != null) {
                    currentShape.updateSize(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setPrimaryColor(Color color) {
        primaryColor = color;
    }

    public void setSecondaryColor(Color color) {
        secondaryColor = color;
    }

    public void loadImage(File file) {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                throw new IOException("Failed to load image");
            }
            loadedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    Color pixelColor = new Color(originalImage.getRGB(x, y), true);
                    Color newColor;
                    if (isSimilarColor(pixelColor, primaryColor)) {
                        newColor = primaryColor;
                    } else if (isSimilarColor(pixelColor, secondaryColor)) {
                        newColor = secondaryColor;
                    } else {
                        newColor = new Color(0, 0, 0, 0); // Transparent if neither primary nor secondary color
                    }
                    loadedImage.setRGB(x, y, newColor.getRGB());
                }
            }
            repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSimilarColor(Color color1, Color color2) {
        int threshold = 50;
        int rDiff = Math.abs(color1.getRed() - color2.getRed());
        int gDiff = Math.abs(color1.getGreen() - color2.getGreen());
        int bDiff = Math.abs(color1.getBlue() - color2.getBlue());
        return (rDiff + gDiff + bDiff) < threshold;
    }

    private Shape createShape(int x, int y, Color color) {
        switch (selectedShape) {
            case "Circle":
                return new Circle(x, y, color);
            case "Rectangle":
                return new Rectangle(x, y, color);
            case "Line":
                return new Line(x, y, color);
            default:
                return null;
        }
    }

    public void setSelectedShape(String shape) {
        selectedShape = shape;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (loadedImage != null) {
            g2d.drawImage(loadedImage, 0, 0, null);
        }
        for (Shape shape : shapes) {
            shape.draw(g2d);
        }
    }
}

public class DrawingApplication extends JFrame {
    private DrawingPanel drawingPanel;

    public DrawingApplication() {
        setTitle("Drawing Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        JButton primaryColorButton = new JButton("Primary Color");
        primaryColorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Choose Primary Color", drawingPanel.getPrimaryColor());
            if (color != null) {
                drawingPanel.setPrimaryColor(color);
            }
        });
        JButton secondaryColorButton = new JButton("Secondary Color");
        secondaryColorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Choose Secondary Color", drawingPanel.getSecondaryColor());
            if (color != null) {
                drawingPanel.setSecondaryColor(color);
            }
        });
        JButton loadImageButton = new JButton("Load Image");
        loadImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                drawingPanel.loadImage(selectedFile);
            }
        });
        JButton circleButton = new JButton("Circle");
        circleButton.addActionListener(e -> drawingPanel.setSelectedShape("Circle"));
        JButton rectangleButton = new JButton("Rectangle");
        rectangleButton.addActionListener(e -> drawingPanel.setSelectedShape("Rectangle"));
        JButton lineButton = new JButton("Line");
        lineButton.addActionListener(e -> drawingPanel.setSelectedShape("Line"));

        toolBar.add(primaryColorButton);
        toolBar.add(secondaryColorButton);
        toolBar.add(loadImageButton);
        toolBar.add(circleButton);
        toolBar.add(rectangleButton);
        toolBar.add(lineButton);
        add(toolBar, BorderLayout.NORTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawingApplication app = new DrawingApplication();
            app.setVisible(true);
        });
    }
}
