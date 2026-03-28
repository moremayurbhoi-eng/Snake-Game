import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 75;

    private Timer timer;
    private final Random random;
    
    private ArrayList<Integer> snakeBody;
    private int snakeX;
    private int snakeY;
    private int foodX;
    private int foodY;
    private char direction = 'R';
    private boolean running = false;
    private int score;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        
        startGame();
    }

    public void startGame() {
        snakeBody = new ArrayList<>();
        snakeX = SCREEN_WIDTH / 2 / UNIT_SIZE;
        snakeY = SCREEN_HEIGHT / 2 / UNIT_SIZE;
        direction = 'R';
        score = 0;
        
        // Initial snake length (3 units)
        snakeBody.clear();
        for (int i = 0; i < 3; i++) {
            snakeBody.add((snakeY * (SCREEN_WIDTH / UNIT_SIZE)) + snakeX - i);
        }
        
        newFood();
        running = true;
        
        // Stop existing timer if any, then start new one
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Grid lines
            g.setColor(Color.darkGray);
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Draw food
            g.setColor(Color.red);
            g.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake
            for (int i = 0; i < snakeBody.size(); i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                int x = (snakeBody.get(i) % (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                int y = (snakeBody.get(i) / (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                g.fillRect(x, y, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw score
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + score, 
                        (SCREEN_WIDTH - metrics.stringWidth("Score: " + score)) / 2, 
                        g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newFood() {
        foodX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        foodY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        
        // Make sure food doesn't spawn on snake
        for (int segment : snakeBody) {
            int segX = (segment % (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            int segY = (segment / (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            if (foodX == segX && foodY == segY) {
                newFood(); // Recursive call to get new position
                return;
            }
        }
    }

    public void move() {
        // Calculate new head position
        int newHeadX = snakeX;
        int newHeadY = snakeY;
        
        switch (direction) {
            case 'U':
                newHeadY--;
                break;
            case 'D':
                newHeadY++;
                break;
            case 'L':
                newHeadX--;
                break;
            case 'R':
                newHeadX++;
                break;
        }

        // Convert to array index
        int newHeadIndex = (newHeadY * (SCREEN_WIDTH / UNIT_SIZE)) + newHeadX;
        snakeBody.add(0, newHeadIndex);
        
        snakeX = newHeadX;
        snakeY = newHeadY;

        // Check if snake ate food
        if ((snakeX * UNIT_SIZE == foodX) && (snakeY * UNIT_SIZE == foodY)) {
            score++;
            newFood();
        } else {
            snakeBody.remove(snakeBody.size() - 1);
        }
    }

    public void checkCollisions() {
        // Check if head collides with body (excluding head itself)
        for (int i = 1; i < snakeBody.size(); i++) {
            if (snakeBody.get(0).equals(snakeBody.get(i))) {
                running = false;
            }
        }

        // Check if head touches borders
        if (snakeX < 0 || snakeX >= SCREEN_WIDTH / UNIT_SIZE || 
            snakeY < 0 || snakeY >= SCREEN_HEIGHT / UNIT_SIZE) {
            running = false;
        }

        if (!running) {
            if (timer != null) {
                timer.stop();
            }
        }
    }

    public void gameOver(Graphics g) {
        // Game Over text
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", 
                    (SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2, 
                    SCREEN_HEIGHT / 2);

        // Score text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + score, 
                    (SCREEN_WIDTH - metrics2.stringWidth("Score: " + score)) / 2, 
                    SCREEN_HEIGHT / 2 + 50);

        // Press Space to Play Again
        g.setColor(Color.green);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press SPACE to Play Again", 
                    (SCREEN_WIDTH - metrics3.stringWidth("Press SPACE to Play Again")) / 2, 
                    SCREEN_HEIGHT / 2 + 120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        startGame();
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");  // Fixed: ASCII only title
        SnakeGame snakeGame = new SnakeGame();
        frame.add(snakeGame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}