package com.example.paint;

import java.awt.image.RenderedImage;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;


public class PaintMenuControls {
    public void open(File file, FileChooser fileChooser, Canvas canvas, Stage stage){
        if (file == null){
            file = fileChooser.showOpenDialog(stage);
        }
        drawImage(file, canvas);
    }

    public void drawImage(File file, Canvas canvas) {
        if (file == null){
            return;
        }
        Image image = new Image(file.toURI().toString());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        gc.drawImage(image, 0, 0);
    }
}
