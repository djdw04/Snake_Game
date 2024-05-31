import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;

public class MainClass extends JFrame {

    // Game window dimensions
    private final int WIDTH = 300, HEIGHT = 300;
    // Size of the snake and ball
    private final int DOT_SIZE = 10;
    // Arrays to store the x and y coordinates of the snake's joints
    private int[] x = new int[900];
    private int[] y = new int[900];
    // Initial size of the snake
    private int bodyParts = 4;
    // Coordinates of the ball
    private int ballX;
    private int ballY;
    // Player's score
    private int points = 0;
    // High score
    private int highScore = 0;
    // Initial direction of the snake
    private char direction = 'R';
    // Game state
    private boolean running = false;
    // Timer for game updates
    private Timer timer;
    // Game panel
    private JPanel gamePanel;
    // Snake color (default is green)
    private Color snakeColor = Color.GREEN;

    public MainClass() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a custom JPanel for drawing the game
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                doDrawing(g);
                showScore(g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        // Load the high score from a file
        loadHighScore();
        // Select the snake color
        selectSnakeColor();
        // Initialize the game
        initGame();
        // Set up key bindings for controlling the snake
        initKeyBindings();
    }

    // Initialize the game state
    private void initGame() {
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 50 - i * 10;
            y[i] = 50;
        }
        placeBall();
        timer = new Timer(60, e -> gameUpdate());
        timer.start();
        running = true;
    }

    // Set up key bindings for controlling the snake
    private void initKeyBindings() {
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        gamePanel.getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'R') direction = 'L';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        gamePanel.getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'L') direction = 'R';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        gamePanel.getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'D') direction = 'U';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        gamePanel.getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'U') direction = 'D';
            }
        });

        // Add key bindings for W, A, S, and D
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "moveUp");
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "moveLeft");
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "moveDown");
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "moveRight");

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        gamePanel.getActionMap().put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!running) restartGame();
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "playAgain");
        gamePanel.getActionMap().put("playAgain", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!running) playAgain();
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "quitGame");
        gamePanel.getActionMap().put("quitGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!running) {
                    JOptionPane.showMessageDialog(gamePanel, "Thank you for playing, come back soon!", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        });
    }

    // Place the ball at a random position on the game panel
    private void placeBall() {
        int r = (int) (Math.random() * ((WIDTH - DOT_SIZE) / DOT_SIZE));
        ballX = r * DOT_SIZE;
        int y = (int) (Math.random() * ((HEIGHT - DOT_SIZE) / DOT_SIZE));
        ballY = y * DOT_SIZE;
    }

    // Game update loop
    private void gameUpdate() {
        if (running) {
            move();
            checkBall();
            checkCollisions();
        }
        gamePanel.repaint();
    }

    // Move the snake based on the current direction
    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= DOT_SIZE; break;
            case 'D': y[0] += DOT_SIZE; break;
            case 'L': x[0] -= DOT_SIZE; break;
            case 'R': x[0] += DOT_SIZE; break;
        }
    }

    // Check if the snake's head collides with the ball
    private void checkBall() {
        if ((x[0] == ballX) && (y[0] == ballY)) {
            bodyParts++;
            points++;
            placeBall();
        }
    }

    // Check for collisions (snake hitting itself or the walls)
    private void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }

        if (y[0] < 0 || y[0] >= HEIGHT || x[0] < 0 || x[0] >= WIDTH) {
            running = false;
        }

        if (!running) {
            timer.stop();
            updateHighScore(); // Update the high score if the current score is higher
        }
    }

    // Update the high score if the current score is higher
    private void updateHighScore() {
        if (points > highScore) {
            highScore = points;
            saveHighScore(); // Save the new high score to a file
        }
    }

    // Load the high score from a file
    private void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save the high score to a file
    private void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"));
            writer.write(Integer.toString(highScore));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Restart the game
    private void restartGame() {
        bodyParts = 6;
        points = 0;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 50 - i * 10;
            y[i] = 50;
        }
        placeBall();
        timer.start();
        running = true;
    }

    // Play again after a game over
    private void playAgain() {
        bodyParts = 4;
        points = 0;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 50 - i * 10;
            y[i] = 50;
        }
        placeBall();
        timer.start();
        running = true;
    }

    // Draw the game elements on the panel
    private void doDrawing(Graphics g) {
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        if (running) {
            g.setColor(Color.red);
            g.fillRect(ballX, ballY, DOT_SIZE, DOT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(snakeColor);
                    g.fillRect(x[i], y[i], DOT_SIZE, DOT_SIZE);
                } else {
                    g.setColor(snakeColor.darker());
                    g.fillRect(x[i], y[i], DOT_SIZE, DOT_SIZE);
                }
            }
        } else {
            gameOver(g);
        }
    }

    // Display the player's score
    private void showScore(Graphics g) {
        g.setColor(Color.black);
        g.drawString("Points: " + points, 10, 20);
    }

    // Display the game over screen
    private void gameOver(Graphics g) {
        g.setColor(Color.red);
        if (points > 9) {
            g.drawString("Congrats you had a high score!", WIDTH / 2 - 80, HEIGHT / 2 - 60);
        } else {
            g.drawString("Better luck next time!", WIDTH / 2 - 80, HEIGHT / 2 - 60);
        }
        g.drawString("Game Over", WIDTH / 2 - 50, HEIGHT / 2 - 20);
        g.drawString("Points: " + points, WIDTH / 2 - 50, HEIGHT / 2);
        g.drawString("High Score: " + highScore, WIDTH / 2 - 50, HEIGHT / 2 + 20); // Display the high score
        g.drawString("Press 'P' to Play Again or 'Q' to Quit", WIDTH / 2 - 120, HEIGHT / 2 + 40);
    }

    // Allow the user to select the snake color
    private void selectSnakeColor() {
        Object[] options = {"Blue", "Yellow", "Green", "Purple", "Pink", "Orange"};
        int choice = JOptionPane.showOptionDialog(this, "Select the color of your snake:", "Snake Color", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);

        switch (choice) {
            case 0:
                snakeColor = Color.BLUE;
                break;
            case 1:
                snakeColor = Color.YELLOW;
                break;
            case 2:
                snakeColor = Color.GREEN;
                break;
            case 3:
                snakeColor = new Color(128, 0, 128); // Purple
                break;
            case 4:
                snakeColor = Color.PINK;
                break;
            case 5:
                snakeColor = Color.ORANGE;
                break;
            default:
                snakeColor = Color.GREEN;
                break;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new MainClass();
            frame.setVisible(true);
        });
    }
}