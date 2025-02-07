import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PasswordManagementApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Password Management");

        // Load the logo image
        Image logoImage = new Image(getClass().getResourceAsStream("/logo.png"));
        ImageView logoImageView = new ImageView(logoImage);

        // Create a layout to hold the image
        StackPane root = new StackPane();
        root.getChildren().add(logoImageView);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
