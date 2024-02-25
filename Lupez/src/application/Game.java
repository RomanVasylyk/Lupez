package application;

import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Game extends Application {
    private int[][] levelData;
    private final int TILE_SIZE = 32;
    private ImageView playerImageView; 
    private int playerRow;
    private int playerCol;
    private List<Coin> coins = new ArrayList<>();
    private Pane root;
    private int score = 0;
    private Text scoreText;
    private long startTime;
    private long elapsedTime;
     int availableCoins = 100;
     
    @SuppressWarnings("incomplete-switch")
	@Override
    public void start(Stage primaryStage) {
        try {
             root = new Pane();
            Scene scene = new Scene(root, 482, 286);
            startTime = System.currentTimeMillis();

            scoreText = new Text("Score: 0");
            scoreText.setX(10); 
            scoreText.setY(10);
            root.getChildren().add(scoreText);
            
            

            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case UP:
                        movePlayer(-1, 0);
                        break;
                    case DOWN:
                        movePlayer(1, 0);
                        break;
                }
            });

            levelData = readLevel("resources/cart.txt");
            renderLevel(root);

            Text coinsText = new Text("Available Coins: " + availableCoins);
            
            root.getChildren().add(coinsText);
            
            new AnimationTimer() {
                private long lastUpdate = 0;

                @Override
                public void handle(long now) {
                	elapsedTime = System.currentTimeMillis() - startTime;
                	if (availableCoins <= 0) {
                        this.stop(); 
                        displayFinalScore();
                    }
                	if (now - lastUpdate >= 2_000_000_000 && availableCoins > 0) { 
                	    for (int row = 0; row < levelData.length; row++) {
                	        for (int col = 0; col < levelData[row].length; col++) {
                	            if (levelData[row][col] == 4) {
                	                throwCoin(root, row, col);
                	                availableCoins--;
                	                coinsText.setText("Available Coins: " + availableCoins);
                	            }
                	        }
                	    }
                	    lastUpdate = now;
                	}


                	List<Coin> coinsToRemove = new ArrayList<>();
                	for (Coin coin : coins) {
                	    coin.update();
                	    if (!coin.imageView.isVisible() || (coin.velocityX == 0 && coin.velocityY == 0)) {
                	        coinsToRemove.add(coin);
                	    } else if (isPlayerCollidingWithCoin(coin)) {
                	        score++;
                	        scoreText.setText("Score: " + score);
                	        coinsToRemove.add(coin);
                	    }
                	}

                    coins.removeAll(coinsToRemove);
                    for (Coin coin : coinsToRemove) {
                        removeCoin(coin);
                    }
                }
            }.start();
            
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[][] readLevel(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(filename).toFile()))) {
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            int[][] level = new int[lines.size()][15];

            for (int row = 0; row < lines.size(); row++) {
                line = lines.get(row);
                for (int col = 0; col < line.length(); col++) {
                    level[row][col] = Character.getNumericValue(line.charAt(col));
                }
            }

            return level;
        }
    }


    private void renderLevel(Pane root) {
    	playerRow = -1;
        playerCol = -1;

        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[row].length; col++) {
                ImageView tileView = null;
                switch (levelData[row][col]) {
                    case 1:
                        tileView = createImageView("/sprCol_0.png");
                        break;
                    case 2:
                        tileView = createImageView("/windows.png");
                        break;
                    case 3:
                        tileView = createImageView("/mmo.jpg");
                        break;
                    case 4:
                        tileView = createImageView("/roboRight_0.png");
                        break;
                    case 5:
                        tileView = createImageView("/roboRight_0.png");
                        playerImageView = tileView;
                        playerRow = row;
                        playerCol = col;
                        break;
                }
                if (tileView != null) {
                    tileView.setX(col * TILE_SIZE);
                    tileView.setY(row * TILE_SIZE);
                    root.getChildren().add(tileView);
                }
            }
        }
    }


    private ImageView createImageView(String imagePath) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        imageView.setFitWidth(TILE_SIZE);
        imageView.setFitHeight(TILE_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    private void movePlayer(int dRow, int dCol) {
        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;

        if (newRow >= 0 && newRow < levelData.length && newCol >= 0 && newCol < levelData[playerRow].length
                && levelData[newRow][newCol] != 1) {
            levelData[playerRow][playerCol] = 0; 
            levelData[newRow][newCol] = 5; 

            playerImageView.setY(newRow * TILE_SIZE);
            playerImageView.setX(newCol * TILE_SIZE);

            playerRow = newRow;
            playerCol = newCol;
        }
    }
    private void throwCoin(Pane root, int robberRow, int robberCol) {
        ImageView coinView = createImageView("/coin.png");

        coinView.setX(robberCol * TILE_SIZE);
        coinView.setY(robberRow * TILE_SIZE);
        root.getChildren().add(coinView);

        double velocityX = -2.0;
        double velocityY = Math.random() * 4 - 2;

        Coin coin = new Coin(coinView, velocityX, velocityY, levelData, TILE_SIZE, scoreText, this);
        coins.add(coin);
    }

    private boolean isPlayerCollidingWithCoin(Coin coin) {
        ImageView coinView = coin.imageView;
        double playerX = playerCol * TILE_SIZE;
        double playerY = playerRow * TILE_SIZE;
        double coinX = coinView.getX();
        double coinY = coinView.getY();

        return playerX < coinX + TILE_SIZE && playerX + TILE_SIZE > coinX &&
               playerY < coinY + TILE_SIZE && playerY + TILE_SIZE > coinY;
    }
    private void displayFinalScore() {
        long elapsedSeconds = elapsedTime / 1000;
        String finalMessage = "Final Score: " + score +"/"+ 100 + "\nTime: " + elapsedSeconds + " seconds";
        
        Text finalScoreText = new Text(finalMessage);
        finalScoreText.setX(100); 
        finalScoreText.setY(100);
        finalScoreText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;"); 

        root.getChildren().clear(); 
        root.getChildren().add(finalScoreText);
    }

    private void removeCoin(Coin coin) {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), coin.imageView);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> {
            root.getChildren().remove(coin.imageView);
            coins.remove(coin);
        });
        fade.play();
    }



    public static void main(String[] args) {
        launch(args);
    }
}

