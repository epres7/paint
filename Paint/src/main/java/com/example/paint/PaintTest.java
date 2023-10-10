package com.example.paint;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;


public class PaintTest extends Application {

    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage buffer;
    private File openedFile;
    private File savedFile;
    private double prevX, prevY;
    private double lineWidth = 1.0;
    private Color lineColor = Color.BLACK;
    private ScrollPane scrollPane;
    private final ComboBox<String> shapesDropdown = new ComboBox<>();
    private final ComboBox<String> linesDropdown = new ComboBox<>();
    private double startX, startY;
    private boolean drawLineMode = true;
    private boolean drawShapeMode = false;
    private boolean eyeDropMode = false;
    private ColorPicker colorPicker = new ColorPicker();
    private Slider lineThicknessSlider = new Slider();
    private boolean isSaved;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Paint");
        isSaved = true;

        // Create parts for the main interface
        canvas = new Canvas(800, 800);
        gc = canvas.getGraphicsContext2D();
        scrollPane = new ScrollPane(canvas);
        scrollPane.setPrefSize(800, 800);
        MenuBar menuBar = new MenuBar();

        //Create and add menu items for the file tab
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> openImage(primaryStage));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveImage(primaryStage));
        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setOnAction(e -> saveAsImage(primaryStage));
        fileMenu.getItems().add(openItem);
        fileMenu.getItems().add(saveItem);
        fileMenu.getItems().add(saveAsItem);
        menuBar.getMenus().add(fileMenu);

        //Create and add menu items for the help tab
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutPopup(primaryStage));
        MenuItem supportItem = new MenuItem("Contact Support");
        supportItem.setOnAction(e -> showSupportPopup(primaryStage));
        MenuItem jokeItem = new MenuItem("Jokes");
        jokeItem.setOnAction(e -> showJokePopup(primaryStage));
        helpMenu.getItems().add(aboutItem);
        helpMenu.getItems().add(supportItem);
        helpMenu.getItems().add(jokeItem);
        menuBar.getMenus().add(helpMenu);


        // Initialize the line thickness slider
        lineThicknessSlider = new Slider(1, 10, 1); // Min, max, and default values
        lineThicknessSlider.setShowTickLabels(true);
        lineThicknessSlider.setShowTickMarks(true);
        lineThicknessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            lineWidth = newValue.doubleValue();
        });

        // Initialize the color picker
        colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(e -> lineColor = colorPicker.getValue());

        // Create width and height input fields for manual user resizing of the canvas
        TextField widthField = new TextField("800");
        widthField.setPromptText("Width");
        TextField heightField = new TextField("800");
        heightField.setPromptText("Height");

        // Create a Resize button which takes the width and height values to resize the canvas
        Button resizeButton = new Button("Resize");
        resizeButton.setOnAction(e -> resizeCanvas(Integer.parseInt(widthField.getText()), Integer.parseInt(heightField.getText())));

        shapesDropdown.getItems().addAll("Square", "Circle", "Rectangle", "Ellipse", "Triangle", "Custom Polygon");
        shapesDropdown.setValue("Square");

        linesDropdown.getItems().addAll("Line", "Free Draw");
        linesDropdown.setValue("Line");

        Button lineButton = new Button("Line");
        lineButton.setOnAction(e -> {
            drawLineMode = true;
            drawShapeMode = false;
            eyeDropMode = false;
        });

        Button shapeButton = new Button("Shapes");
        shapeButton.setOnAction(e -> {
            drawLineMode = false;
            drawShapeMode = true;
            eyeDropMode = false;
        });

        Button eyeDropperButton = new Button("Eye Dropper");
        eyeDropperButton.setOnAction(e -> {
            drawLineMode = false;
            drawShapeMode = false;
            eyeDropMode = true;
        });

        Button straightButton = new Button("Straight Outline");
        straightButton.setOnAction(e -> {
            gc.setLineDashes(0);
        });

        Button dashButton = new Button("Dashed Outline");
        dashButton.setOnAction(e -> {
            gc.setLineDashes(5);
        });

        HBox canvasWidth = new HBox(new Label("Width: "), widthField);
        HBox canvasHeight = new HBox(new Label("Height"), heightField);
        VBox resizeMenu = new VBox(canvasWidth, canvasHeight);
        VBox outlineMenu = new VBox(straightButton, dashButton);

        HBox controlsBox = new HBox(
                20, lineButton, linesDropdown,
                new Label("Line Thickness"), lineThicknessSlider,
                new Label("Line Color"), colorPicker, eyeDropperButton,
                shapeButton, shapesDropdown,
                outlineMenu,
                resizeMenu, resizeButton
        );

        // Create the layout
        VBox vbox = new VBox(menuBar, controlsBox, scrollPane);
        Scene scene = new Scene(vbox, 400, 400);

        //"Smart" save method
        primaryStage.setOnCloseRequest(event -> {
            if (!isSaved) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Close Without Saving");
                alert.setHeaderText("You are about to close without saving. Would you like to save before closing?");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        saveAsImage(primaryStage);
                        primaryStage.close();
                    } else if (response == ButtonType.NO) {
                        primaryStage.close();
                    } else {
                        event.consume();
                    }
                });
            }
        });

        //Create keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            if (e.isShortcutDown()) { // Check if Ctrl (or Command) is pressed
                if (e.getCode() == KeyCode.S) { // Save shortcut (S)
                    saveImage(primaryStage);
                }
                if (e.getCode() == KeyCode.W) { // Close shortcut (W)
                    primaryStage.close();
                }
            }
        });


        primaryStage.setScene(scene);
        primaryStage.show();


        // Add mouse event listeners for drawing
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (drawLineMode) {
                    if (Objects.equals(linesDropdown.getValue(), "Line")){
                        startX = e.getX();
                        startY = e.getY();
                    } else if (Objects.equals(linesDropdown.getValue(), "Free Draw")){
                        startFreehandLine(e.getX(), e.getY());
                    }
                } else if (drawShapeMode) {
                    startX = e.getX();
                    startY = e.getY();
                } else if (eyeDropMode) {
                    handleEyedropperTool(e);
                }
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (drawLineMode) {
                    if (Objects.equals(linesDropdown.getValue(), "Line")){
                        drawStraightLine(startX, startY, e.getX(), e.getY());
                    } else if (Objects.equals(linesDropdown.getValue(), "Free Draw")){
                        continueFreehandLine(e.getX(), e.getY());
                    }
                } else if (drawShapeMode) {
                    double currentX = e.getX();
                    double currentY = e.getY();
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.drawImage(buffer, 0, 0);
                    drawShape(startX, startY, currentX, currentY, e);
                }
            }
        });


        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (drawLineMode) {
                    if (Objects.equals(linesDropdown.getValue(), "Line")) {
                        gc.setLineWidth(lineWidth);
                        gc.setStroke(lineColor);
                        gc.beginPath();
                        gc.moveTo(prevX, prevY);
                        gc.stroke();
                        buffer = canvas.snapshot(null, null);
                        isSaved = false;
                    } else if (Objects.equals(linesDropdown.getValue(), "Free Draw")) {
                        endFreehandLine();
                        buffer = canvas.snapshot(null, null);
                        isSaved = false;
                    }
                }
            } else if (drawShapeMode) {
                double endX = e.getX();
                double endY = e.getY();
                drawShape(startX, startY, endX, endY, e);
                gc.setLineWidth(lineWidth);
                gc.setStroke(lineColor);
                gc.beginPath();
                gc.moveTo(startX, startY);
                gc.stroke();
                buffer = canvas.snapshot(null, null);
                isSaved = false;
            }
        });
    }

    private void openImage (Stage primaryStage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        openedFile = fileChooser.showOpenDialog(primaryStage);

        if (openedFile != null) {
            // Clear the canvas
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Load and draw the image onto the canvas
            javafx.scene.image.Image image = new javafx.scene.image.Image(openedFile.toURI().toString());
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());

            // Set canvas size to match image size
            canvas.setWidth(image.getWidth());
            canvas.setHeight(image.getHeight());

            // Update the scroll pane to match the canvas size
            scrollPane.setHvalue(0);
            scrollPane.setVvalue(0);
        }
    }

    private void saveAsImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File saveFile = fileChooser.showSaveDialog(primaryStage);

        if (saveFile != null) {
            // Save the canvas content as an image
            javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", saveFile);
                savedFile = saveFile; // Update the saved file reference
                isSaved = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(Stage primaryStage) {
        if (savedFile != null) {
            // Save the updated drawing to the already saved file
            javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", savedFile);
                isSaved = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveAsImage(primaryStage); // If no saved file, do "Save As" instead
        }

    }

    private void showAboutPopup(Stage primaryStage) {
        Popup popup = new Popup();
        Label aboutText = new Label("This is Ethan's version of the CS250 Paint program.\n" +
                "The software is currently running version 1.0.3, last updated 10/8/23.");
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popup.hide());

        VBox popupContent = new VBox(aboutText, closeButton);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10px;");
        popup.getContent().add(popupContent);
        popup.show(primaryStage);
    }

    private void showSupportPopup(Stage primaryStage) {
        Popup popup = new Popup();
        Label supportText = new Label("If you need help with the software or would like to report a bug,\n please contact ethan.preston@valpo.edu.");
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popup.hide());

        VBox popupContent = new VBox(supportText, closeButton);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10px;");
        popup.getContent().add(popupContent);
        popup.show(primaryStage);
    }

    private void showJokePopup(Stage primaryStage) {
        Popup popup = new Popup();
        Label jokeText = new Label("Why did the Java Developer quit his job?\n" +
                "\n" +
                "Because he didn't get arrays.");
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popup.hide());

        VBox popupContent = new VBox(jokeText, closeButton);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10px;");
        popup.getContent().add(popupContent);
        popup.show(primaryStage);
    }


    private void resizeCanvas(double newWidth, double newHeight) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = canvas.snapshot(params, null);

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

        // Create a new BufferedImage with the specified width and height
        BufferedImage resizedImage = new BufferedImage((int) newWidth, (int) newHeight, bufferedImage.getType());
        java.awt.Graphics2D g = resizedImage.createGraphics();

        // Use nearest-neighbor interpolation for sharp resizing
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw the original image onto the resized image using nearest-neighbor interpolation
        g.drawImage(bufferedImage, 0, 0, (int) newWidth, (int) newHeight, null);
        g.dispose();

        // Convert the resized BufferedImage back to a WritableImage
        WritableImage resultImage = SwingFXUtils.toFXImage(resizedImage, null);

        // Update the canvas size and draw the resized image
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(newWidth);
        canvas.setHeight(newHeight);
        gc.drawImage(resultImage, 0, 0);
    }


    private void drawShape(double startX, double startY, double endX, double endY, MouseEvent event) {

        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        switch (shapesDropdown.getValue()) {
            case "Square":
                drawSquare(startX, startY, width);
                break;
            case "Circle":
                double radius = Math.min(width, height) / 2;
                drawCircle(startX + width / 2, startY + height / 2, radius);
                break;
            case "Rectangle":
                drawRectangle(startX, startY, width, height);
                break;
            case "Ellipse":
                drawEllipse(startX, startY, width, height);
                break;
            case "Triangle":
                drawTriangle(startX, startY, endX, endY);
                break;
        }
        event.consume();
    }

    private void drawSquare(double x, double y, double size) {
        Rectangle square = new Rectangle((int) x, (int) y, (int) size, (int) size);
        gc.strokeRect(square.getX(), square.getY(), square.getWidth(), square.getHeight());
    }

    private void drawRectangle(double x, double y, double width, double height) {
        Rectangle rectangle = new Rectangle((int) x, (int) y, (int) width, (int) height);
        gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private void drawEllipse(double x, double y, double width, double height) {
        Ellipse ellipse = new Ellipse(x, y, width / 2, height / 2);
        gc.strokeOval(ellipse.getCenterX() - ellipse.getRadiusX(), ellipse.getCenterY() - ellipse.getRadiusY(), ellipse.getRadiusX() * 2, ellipse.getRadiusY() * 2);
    }
    private void drawCircle(double centerX, double centerY, double radius) {
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private void drawTriangle(double startX, double startY, double endX, double endY) {
        gc.strokePolygon(new double[]{startX, (startX + endX) / 2, endX},
                new double[]{startY, endY, endY},
                3);
    }

    private void drawStraightLine(double startX, double startY, double endX, double endY) {
        // Clear canvas and draw the buffer image
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(buffer, 0, 0);

        // Draw a straight line from the starting point to the ending point
        gc.setLineWidth(lineWidth);
        gc.setStroke(lineColor);
        gc.strokeLine(startX, startY, endX, endY);
    }
    private void startFreehandLine(double x, double y) {
        gc.beginPath();
        gc.moveTo(x, y);
        gc.setLineWidth(lineWidth);
        gc.setStroke(lineColor);
    }

    private void continueFreehandLine(double x, double y) {
        gc.lineTo(x, y);
        gc.stroke();
    }

    private void endFreehandLine() {
        gc.closePath();
    }
    private void handleEyedropperTool(MouseEvent event) {
        if (eyeDropMode) {
            double mouseX = event.getX();
            double mouseY = event.getY();

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = canvas.snapshot(params, null);

            Color pickedColor = snapshot.getPixelReader().getColor((int) mouseX, (int) mouseY);

            colorPicker.setValue(pickedColor);
        }
    }

}