import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.ArrayList.*;
import java.util.Random.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // logic game
    Bird bird;
    int velocityX = -4; // move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; // move bird up/down
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;       // score berawal dari 0, jika burung melewati pipa, score bertambah 1

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        // load image
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        placePipesTimer.start();  

        // game timer
        gameLoop = new Timer(1000/60, this); // 1000/60 = 16.6
        gameLoop.start();
    }

    public void placePipes() {
        // (0-1) * pipeHeight/2 -> (0-256)
        // 128
        // 0 - 128 - (0-256) --> 1/4 pipeHeight -> 3/4 pipeHeight

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;


        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // System.out.println("draw");
        // background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // pipes
        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        };

        // score 
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // validasi jika burung tidak melewati pipa dan posisi x burung melewati/lebih besar dari pipa kanan
            // keadaan pipa pada sumbu x dimulai dari sisi kiri ditambah luas pipa, maka kita akan mendapat nilai pipa dari sisi kanan
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;       // score bertambah 0.5 karna setiap kali burung melewati pipa, pipa yang dilewati ada 2 pipa (pipa atas dan pipa bawah), 0.5 + 0.5 = 1
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // validasi skenario ketika bird menabrak pipa
    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&       // kiri atas a tidak mencapai kanan bawah b
                a.x + a.width > b.x &&      // kanan atas a melewati kiri atas b
                a.y < b.y + b.height &&     // kiri atas a tidak mencapai kiri bawah b
                a.y + a.height > b.y;       // kiri bawah a melewati kiri atas b
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();   
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                // restart game dengan me reset kondisi dan menjalankan fungsi dari awal lagi
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }
}
