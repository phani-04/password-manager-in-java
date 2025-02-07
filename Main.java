import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.*;
import java.security.SecureRandom;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class Main extends Application {
    private Stage primaryStage;
    private TextField websiteEntry;
    private TextField emailEntry;
    private PasswordField passwordEntry;
    private Stage passwordDisplayStage; // New stage to display password and email

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Password Manager");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(50, 50, 50, 50));

        ImageView logoImage = new ImageView(new Image("file:logo.png"));
        grid.add(logoImage, 1, 0);

        Label websiteLabel = new Label("Website:");
        grid.add(websiteLabel, 0, 1);

        websiteEntry = new TextField();
        grid.add(websiteEntry, 1, 1);

        Label emailLabel = new Label("Email/Username:");
        grid.add(emailLabel, 0, 2);

        emailEntry = new TextField("phanindhra@gmail.com");
        grid.add(emailEntry, 1, 2);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 3);

        passwordEntry = new PasswordField();
        grid.add(passwordEntry, 1, 3);

        // Add a checkbox for showing/hiding the password
        CheckBox showPasswordCheckBox = new CheckBox("Show Password");
        showPasswordCheckBox.setOnAction(e -> togglePasswordVisibility(showPasswordCheckBox.isSelected()));
        grid.add(showPasswordCheckBox, 2, 3);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> findPassword());
        grid.add(searchButton, 2, 1);

        Button generatePasswordButton = new Button("Generate Password");
        generatePasswordButton.setOnAction(e -> generatePassword());
        grid.add(generatePasswordButton, 2, 3);

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> save());
        grid.add(addButton, 1, 4);

        Button dataButton = new Button("Data");
        dataButton.setOnAction(e -> showData());
        grid.add(dataButton, 2, 4);

        Scene scene = new Scene(grid, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Create the stage for displaying password and email
        passwordDisplayStage = new Stage();
        passwordDisplayStage.initOwner(primaryStage);
        passwordDisplayStage.initModality(Modality.WINDOW_MODAL);
    }

    private void togglePasswordVisibility(boolean visible) {
        passwordEntry.setManaged(visible);
        passwordEntry.setVisible(visible);
    }

    private void generatePassword() {
        String password = generateRandomPassword();
        passwordEntry.setText(password);
    }

    private void save() {
        String website = websiteEntry.getText();
        String email = emailEntry.getText();
        String password = passwordEntry.getText();

        if (website.isEmpty() || password.isEmpty()) {
            displayErrorDialog("Empty Fields", "Website and Password fields cannot be empty.");
        } else {
            // Perform save operation on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    JSONArray jsonArray = readDataFromFile();
                    if (jsonArray == null) {
                        jsonArray = new JSONArray();
                    }

                    JSONObject entry = new JSONObject();
                    entry.put("website", website);
                    entry.put("email", email);
                    entry.put("password", password);
                    jsonArray.add(entry);

                    writeDataToFile(jsonArray);

                    websiteEntry.clear();

                    passwordEntry.clear();
                } catch (IOException | ParseException e) {
                    displayErrorDialog("Error", "Failed to save data. Please try again.");
                }
            });
        }
    }

    private void findPassword() {
        String website = websiteEntry.getText();

        if (!website.isEmpty()) {
            Platform.runLater(() -> {
                try {
                    JSONArray jsonArray = readDataFromFile();
                    if (jsonArray != null) {
                        for (Object obj : jsonArray) {
                            JSONObject entry = (JSONObject) obj;
                            String entryWebsite = (String) entry.get("website");
                            String email = (String) entry.get("email");
                            String password = (String) entry.get("password");

                            if (entryWebsite.equals(website)) {
                                emailEntry.setText(email);
                                passwordEntry.setText(password);

                                // Show a flash message with password and email
                                displayPasswordAndEmailFlashMessage(email, password);
                                return;
                            }
                        }
                        displayErrorDialog("Website Not Found", "No details for the website exist.");
                    } else {
                        displayErrorDialog("Data File Not Found", "No data file found.");
                    }
                } catch (IOException | ParseException e) {
                    displayErrorDialog("Error", "Failed to retrieve data. Please try again.");
                }
            });
        }
    }

    private void displayPasswordAndEmailFlashMessage(String email, String password) {
        Label emailLabel = new Label("Email/Username: " + email);
        Label passwordLabel = new Label("Password: " + password);

        VBox vbox = new VBox(emailLabel, passwordLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        Scene scene = new Scene(vbox, 250, 100);
        passwordDisplayStage.setScene(scene);
        passwordDisplayStage.setTitle("Password and Email");
        passwordDisplayStage.show();
    }

    private void showData() {
        // Perform show data operation on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                JSONArray jsonArray = readDataFromFile();
                if (jsonArray != null && jsonArray.size() > 0) {
                    StringBuilder dataText = new StringBuilder();
                    for (Object obj : jsonArray) {
                        JSONObject entry = (JSONObject) obj;
                        String website = (String) entry.get("website");
                        String email = (String) entry.get("email");
                        String password = (String) entry.get("password");
                        dataText.append("Website: ").append(website).append("\n");
                        dataText.append("Email: ").append(email).append("\n");
                        dataText.append("Password: ").append(password).append("\n\n");
                    }
                    displayInformationDialog("Saved Website Data", "Saved Website Data:", dataText.toString());
                } else {
                    displayErrorDialog("No Data", "No data is saved yet.");
                }
            } catch (IOException | ParseException e) {
                displayErrorDialog("Error", "Failed to read data. Please try again.");
            }
        });
    }

    private JSONArray readDataFromFile() throws IOException, ParseException {
        File dataFile = new File("data.json");
        if (dataFile.exists()) {
            JSONParser parser = new JSONParser();
            try (FileReader fileReader = new FileReader(dataFile)) {
                return (JSONArray) parser.parse(fileReader);
            }
        }
        return null;
    }

    private void writeDataToFile(JSONArray jsonArray) throws IOException {
        File dataFile = new File("data.json");
        try (FileWriter fileWriter = new FileWriter(dataFile)) {
            fileWriter.write(jsonArray.toJSONString());
        }
    }

    private String generateRandomPassword() {
        String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!@#$%^&*()_-+=<>?";

        String allCharacters = uppercaseLetters + lowercaseLetters + numbers + symbols;
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        int passwordLength = 12;

        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = random.nextInt(allCharacters.length());
            char randomChar = allCharacters.charAt(randomIndex);
            password.append(randomChar);
        }
        return password.toString();
    }

    private void displayErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void displayInformationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
}
