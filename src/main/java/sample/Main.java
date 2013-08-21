package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Random;

/**
 * JavaFX rocks
 */

public class Main extends Application {
    private final String characters =
            // Hiragana
            "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもや"
                    + "ゆよらりるれろわゐゑをんがぎぐげござじずぜぞだぢどばびぶべぼぱぴぷぺぽ"
                    //Katakana
                    + "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタチツテトダヂヅデドナ"
                    + "ニヌネノハヒフヘホバビブベボパピプペポマミムメモヤユヨラリルレロワヰヱヲン"
                    //80 most common japanese kanji
                    + "一右雨円王音下火花学気九休金空月犬見五口校左三山子四糸字耳七車手十出女小上森人水"
                    + "正生青夕石赤千川先早足村大男中虫町天田土二日入年白八百文木本名目立力林六引雲遠何";
    private final Random random = new Random();
    private final Font font = Font.font("MS PGothic", 15);
    private char[] data = new char[2000 * 2000];
    private int[] path = new int[2000 * 2000];
    private Stage primaryStage;
    private long lastTime = 0;

    public static void main(String[] args) {
        launch(args);
    }

    private int getNbGlyphsPerRow() {
        return (int) primaryStage.getWidth() / 12;
    }

    private int getNbGlyphsPerColumn() {
        return (int) primaryStage.getHeight() / 12;
    }

    private char getRandChar() {
        return characters.charAt(Math.abs(random.nextInt() % characters.length()));
    }

    private void setupSimulation() {
        for (int i = 0; i < getNbGlyphsPerRow() * getNbGlyphsPerColumn(); ++i) {
            data[i] = getRandChar();
            path[i] = 0;
        }
    }

    private void update(long now) {
        if (lastTime == 0) {
            lastTime = now;
        }

        final int decay = 6;
        final float flipRate = 0.001f;
        final int fillStart = 100;
        final float fillRate = 0.05f;

        int nbGlyphsPerRow = getNbGlyphsPerRow();
        int nbGlyphsPerColumn = getNbGlyphsPerColumn();
        int nbGlyphs = nbGlyphsPerRow * nbGlyphsPerColumn;

        for (int i = nbGlyphs - 1; i >= 0; --i) {
            if (i + nbGlyphsPerRow < nbGlyphs) {
                if (path[i] == 255) {
                    // This means the glyph was just set
                    // Initialize the next row's glyph at this glyph's X position
                    path[i + nbGlyphsPerRow] = 255;
                    data[i + nbGlyphsPerRow] = getRandChar();
                }
            }

            // path[i] > 64 means if this glyph's Green component > 25%
            if (path[i] > 64 && random.nextFloat() < flipRate) {
                data[i] = getRandChar();
            }

            // Decrement the glyph's Green component
            if (path[i] > decay) {
                path[i] -= decay;
            } else {
                path[i] = 0;
            }

            // First row
            // Start doing stuff only if the Green component > 40%
            if (i < nbGlyphsPerRow && path[i] <= fillStart) {
                if (random.nextFloat() < fillRate) {
                    path[i] = 255;
                    data[i] = getRandChar();
                }
            }
        }

        lastTime = now;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        primaryStage.setTitle("Matrix Digital Rain");

        Group root = new Group();
        Scene scene = new Scene(root, 1024, 768);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.F) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
                if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.Q) {
                    primaryStage.close();
                }
            }
        });

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());

        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(font);

        setupSimulation();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);

                gc.clearRect(0, 0, primaryStage.getWidth(), primaryStage.getHeight());
                gc.setFill(Color.rgb(0, 0, 1));
                gc.fillRect(0, 0, primaryStage.getWidth(), primaryStage.getHeight());

                int y = 0;
                int nbGlyphsPerRow = getNbGlyphsPerRow();
                int nbGlyphsPerColumn = getNbGlyphsPerColumn();

                for (int i = 0; i < nbGlyphsPerRow * nbGlyphsPerColumn; ++i) {
                    gc.setFill(Color.rgb(0, path[i], 0));
                    String text = String.valueOf(data[i]);
                    gc.fillText(text, (i % nbGlyphsPerRow) * 12 + 1, y + 13);

                    if (i % nbGlyphsPerRow == nbGlyphsPerRow - 1) {
                        y += 12;
                    }
                }
            }
        }.start();

        root.getChildren().add(canvas);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
