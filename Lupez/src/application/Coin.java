package application;

import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Coin {
    private static int TILE_SIZE; 
    ImageView imageView;
    double velocityX;
    double velocityY;
    private int[][] levelData;
    private boolean hasBounced = false;

    private Text coinsText;
    private Game game; 

    public Coin(ImageView imageView, double velocityX, double velocityY, int[][] levelData, int tileSize, Text coinsText, Game game) {
        this.imageView = imageView;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.levelData = levelData;
        TILE_SIZE = tileSize;
        this.coinsText = coinsText;
        this.game = game; 
    }

    void update() {
        if (!imageView.isVisible()) {
            return; 
        }

        double nextX = imageView.getX() + velocityX;
        double nextY = imageView.getY() + velocityY;

        if (nextX <= 0 || nextY <= 0 || nextY >= 480) {
            this.velocityX = 0;
            this.velocityY = 0;
            return;
        }

        int gridCol = (int) (nextX / TILE_SIZE);
        int gridRow = (int) (nextY / TILE_SIZE);
        if (this.velocityX == 0 && this.velocityY == 0) {
            imageView.setVisible(false);
        }
        if (gridRow >= 0 && gridRow < levelData.length && gridCol >= 0 && gridCol < levelData[gridRow].length) {
            if (levelData[gridRow][gridCol] == 1 || levelData[gridRow][gridCol] == 4) { 
                if (!hasBounced) {
                    this.velocityX = -velocityX;
                    this.velocityY = -velocityY;
                    hasBounced = true;
                } else {
                    imageView.setVisible(false);
                    game.availableCoins++; 
                    coinsText.setText("Available Coins: " + game.availableCoins);
                    this.velocityX = 0;
                    this.velocityY = 0;
                }
            } else if (levelData[gridRow][gridCol] == 0 || levelData[gridRow][gridCol] == 2 || levelData[gridRow][gridCol] == 5) {
                imageView.setX(nextX);
                imageView.setY(nextY);
            }
        } else {
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }
    
    
}
