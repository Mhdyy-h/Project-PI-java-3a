package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur pour la personnalisation d'avatar avec édition des traits faciaux.
 */
public class AvatarCustomizationController implements Initializable {

    @FXML private ImageView avatarPreview;
    @FXML private ComboBox<String> skinColorCombo;
    @FXML private ComboBox<String> hairStyleCombo;
    @FXML private ComboBox<String> hairColorCombo;
    @FXML private ComboBox<String> eyesCombo;
    @FXML private ComboBox<String> eyebrowsCombo;
    @FXML private ComboBox<String> mouthCombo;
    @FXML private ComboBox<String> accessoriesCombo;
    @FXML private ComboBox<String> clothesCombo;
    @FXML private ComboBox<String> clothesColorCombo;

    private static final String API_BASE_URL = "https://api.dicebear.com/9.x/avataaars/png";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String seed = "user";
    private Image currentAvatar;
    private final PauseTransition debounceTimer = new PauseTransition(Duration.millis(400));

    // Options pour chaque trait (API DiceBear Avataaars v9)
    private final String[] skinColors = {"f8d25c", "f6c5a0", "e8a87c", "d08b5b", "ae5d29", "5c3a21"};
    private final String[] topStyles = {"noHair", "longHairBigHair", "longHairBob", "shortHairShortFlat", "shortHairShaggyMullet", "shortHairShortCurly", "shortHairDreads01", "longHairFro", "shortHairShortRound", "shortHairTheCaesar", "longHairCurly", "longHairFrida", "longHairMiaWallace", "longHairNotTooLong", "shortHairDreads02", "shortHairFrizzle", "shortHairSides", "shortHairTheCaesarSidePart", "longHairCurlyFro", "longHairFroBand", "longHairShavedSides", "turban", "hijab", "hat", "winterHat1", "winterHat2", "winterHat3", "winterHat4", "eyepatch"};
    private final String[] hairColors = {"", "a55728", "2c1b18", "b58143", "4a3121", "f6ece6", "e6e6e6", "c93305", "d6b4a8"};
    private final String[] eyesOptions = {"default", "close", "cry", "dots", "happy", "hearts", "side", "squint", "surprised", "wink", "winkWacky"};
    private final String[] eyebrowsOptions = {"default", "angry", "angryNatural", "defaultNatural", "flatNatural", "frownNatural", "raised", "raisedExcited", "raisedExcitedNatural", "sadConcerned", "sadConcernedNatural", "unibrowNatural", "upDown", "upDownNatural"};
    private final String[] mouthOptions = {"default", "concerned", "disbelief", "eating", "grimace", "sad", "screamOpen", "serious", "smile", "tongue", "twinkle", "vomit"};
    private final String[] accessoriesOptions = {"blank", "kurt", "prescription01", "prescription02", "round", "sunglasses", "wayfarers"};
    private final String[] clothesOptions = {"blank", "blazerAndShirt", "blazerAndSweater", "collarAndSweater", "graphicShirt", "hoodie", "overall", "shirtCrewNeck", "shirtScoopNeck", "shirtVNeck"};
    private final String[] clothesColors = {"", "262626", "3c3c3c", "545454", "808080", "a6a6a6", "d9d9d9", "ffffff", "ff5c5c", "ffadad", "ffd6a5", "fdffb6", "caffbf", "9bf6ff", "a0c4ff", "bdb2ff", "ffc6ff", "fffffc"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        setupListeners();
        updatePreview();
    }

    private void initializeComboBoxes() {
        // Peau
        skinColorCombo.getItems().addAll("Jaune clair", "Pêche", "Beige", "Bronze", "Marron", "Noir");
        skinColorCombo.setValue("Beige");

        // Coiffure
        hairStyleCombo.getItems().addAll(
            "Chauve", "Longs volumineux", "Longs carré", "Court plat", "Shaggy",
            "Court bouclé", "Dreads", "Afro", "Court rond", "César",
            "Longs bouclés", "Frida", "Mia Wallace", "Longs moyens",
            "Dreads 02", "Frizzle", "Sides", "César côté", "Curly Fro",
            "Fro Band", "Shaved Sides", "Turban", "Hijab", "Chapeau",
            "Bonnet 1", "Bonnet 2", "Bonnet 3", "Bonnet 4", "Patch"
        );
        hairStyleCombo.setValue("Chauve");

        // Couleur cheveux
        hairColorCombo.getItems().addAll(
            "Naturelle", "Auburn", "Noir", "Blond", "Châtain",
            "Pastel", "Platine", "Roux", "Argent"
        );
        hairColorCombo.setValue("Châtain");

        // Yeux
        eyesCombo.getItems().addAll(
            "Normaux", "Fermés", "Pleurer", "Points", "Heureux", "Cœurs",
            "Sur le côté", "Plissés", "Surpris", "Clin d'œil", "Clin comique"
        );
        eyesCombo.setValue("Normaux");

        // Sourcils
        eyebrowsCombo.getItems().addAll(
            "Normaux", "Fâchés", "Fâchés naturels", "Naturels", "Plats",
            "Froncés", "Surpris", "Excités", "Excités naturels", "Soucieux",
            "Soucieux naturels", "Unibrow", "Haut-bas", "Haut-bas naturels"
        );
        eyebrowsCombo.setValue("Normaux");

        // Bouche
        mouthCombo.getItems().addAll(
            "Normale", "Soucieuse", "Incrédule", "Mange", "Grimace",
            "Triste", "Crie", "Sérieuse", "Sourire", "Langue", "Pétillante", "Dégout"
        );
        mouthCombo.setValue("Sourire");

        // Accessoires
        accessoriesCombo.getItems().addAll(
            "Aucun", "Kurt", "Ordonnance 01", "Ordonnance 02", "Rondes", "Soleil", "Wayfarers"
        );
        accessoriesCombo.setValue("Aucun");

        // Vêtements
        clothesCombo.getItems().addAll(
            "Aucun", "Blazer + Chemise", "Blazer + Pull", "Col + Pull",
            "T-shirt graphique", "Sweat à capuche", "Salopette",
            "Col rond", "Col scoop", "Col en V"
        );
        clothesCombo.setValue("Blazer + Chemise");

        // Couleur vêtements
        clothesColorCombo.getItems().addAll(
            "Aléatoire", "Noir", "Gris foncé", "Gris", "Gris clair",
            "Argent", "Blanc", "Rouge", "Rose", "Orange", "Jaune",
            "Vert", "Bleu clair", "Bleu", "Violet", "Magenta", "Crème"
        );
        clothesColorCombo.setValue("Bleu");
    }

    private void setupListeners() {
        // Debounce: wait 400ms before calling API to avoid rapid-fire 400 errors
        debounceTimer.setOnFinished(e -> doUpdatePreview());

        skinColorCombo.setOnAction(e -> scheduleUpdate());
        hairStyleCombo.setOnAction(e -> scheduleUpdate());
        hairColorCombo.setOnAction(e -> scheduleUpdate());
        eyesCombo.setOnAction(e -> scheduleUpdate());
        eyebrowsCombo.setOnAction(e -> scheduleUpdate());
        mouthCombo.setOnAction(e -> scheduleUpdate());
        accessoriesCombo.setOnAction(e -> scheduleUpdate());
        clothesCombo.setOnAction(e -> scheduleUpdate());
        clothesColorCombo.setOnAction(e -> scheduleUpdate());
    }

    private void scheduleUpdate() {
        debounceTimer.playFromStart();
    }

    private void doUpdatePreview() {
        String url = buildAvatarUrl();
        System.out.println("[Avatar] URL: " + url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(url).toURI())
                .header("Accept", "image/png")
                .GET()
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        javafx.application.Platform.runLater(() -> {
                            try {
                                currentAvatar = new Image(new java.io.ByteArrayInputStream(response.body()));
                                avatarPreview.setImage(currentAvatar);
                                System.out.println("[Avatar] Image chargée: " + currentAvatar.getWidth() + "x" + currentAvatar.getHeight());
                            } catch (Exception ex) {
                                System.err.println("[Avatar] Erreur création image: " + ex.getMessage());
                            }
                        });
                    } else {
                        System.err.println("[Avatar] Erreur HTTP: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[Avatar] Erreur réseau: " + ex.getMessage());
                    return null;
                });
        } catch (Exception ex) {
            System.err.println("[Avatar] Erreur URL: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRandomize() {
        // Randomize all options
        skinColorCombo.setValue(skinColorCombo.getItems().get((int)(Math.random() * skinColorCombo.getItems().size())));
        hairStyleCombo.setValue(hairStyleCombo.getItems().get((int)(Math.random() * hairStyleCombo.getItems().size())));
        hairColorCombo.setValue(hairColorCombo.getItems().get((int)(Math.random() * hairColorCombo.getItems().size())));
        eyesCombo.setValue(eyesCombo.getItems().get((int)(Math.random() * eyesCombo.getItems().size())));
        eyebrowsCombo.setValue(eyebrowsCombo.getItems().get((int)(Math.random() * eyebrowsCombo.getItems().size())));
        mouthCombo.setValue(mouthCombo.getItems().get((int)(Math.random() * mouthCombo.getItems().size())));
        accessoriesCombo.setValue(accessoriesCombo.getItems().get((int)(Math.random() * accessoriesCombo.getItems().size())));
        clothesCombo.setValue(clothesCombo.getItems().get((int)(Math.random() * clothesCombo.getItems().size())));
        clothesColorCombo.setValue(clothesColorCombo.getItems().get((int)(Math.random() * clothesColorCombo.getItems().size())));

        updatePreview();
    }

    private void updatePreview() {
        scheduleUpdate();
    }

    private String buildAvatarUrl() {
        Map<String, String> params = new HashMap<>();

        // Seed avec timestamp aléatoire pour forcer la regénération
        String randomSeed = seed + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
        params.put("seed", randomSeed);
        params.put("size", "200");

        // Peau
        int skinIndex = skinColorCombo.getSelectionModel().getSelectedIndex();
        if (skinIndex >= 0) {
            params.put("skinColor", skinColors[skinIndex]);
        }

        // Cheveux (top)
        int hairIndex = hairStyleCombo.getSelectionModel().getSelectedIndex();
        if (hairIndex >= 0) {
            params.put("top", topStyles[hairIndex]);
        }

        int hairColorIndex = hairColorCombo.getSelectionModel().getSelectedIndex();
        if (hairColorIndex > 0) {
            params.put("hairColor", hairColors[hairColorIndex]);
        }

        // Yeux
        int eyesIndex = eyesCombo.getSelectionModel().getSelectedIndex();
        if (eyesIndex >= 0) {
            params.put("eyes", eyesOptions[eyesIndex]);
        }

        // Sourcils
        int browsIndex = eyebrowsCombo.getSelectionModel().getSelectedIndex();
        if (browsIndex >= 0) {
            params.put("eyebrow", eyebrowsOptions[browsIndex]);
        }

        // Bouche
        int mouthIndex = mouthCombo.getSelectionModel().getSelectedIndex();
        if (mouthIndex >= 0) {
            params.put("mouth", mouthOptions[mouthIndex]);
        }

        // Accessoires
        int accIndex = accessoriesCombo.getSelectionModel().getSelectedIndex();
        if (accIndex >= 0) {
            params.put("accessories", accessoriesOptions[accIndex]);
        }

        // Vêtements
        int clothesIndex = clothesCombo.getSelectionModel().getSelectedIndex();
        if (clothesIndex >= 0) {
            params.put("clothes", clothesOptions[clothesIndex]);
        }

        int clothesColorIndex = clothesColorCombo.getSelectionModel().getSelectedIndex();
        if (clothesColorIndex > 0) {
            params.put("clothesColor", clothesColors[clothesColorIndex]);
        }

        // Construire l'URL
        StringBuilder url = new StringBuilder(API_BASE_URL + "?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) url.append("&");
            url.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        return url.toString();
    }

    public void setSeed(String seed) {
        this.seed = seed != null ? seed : "user";
        updatePreview();
    }

    public Image getGeneratedAvatar() {
        return currentAvatar;
    }

    public String getAvatarUrl() {
        return buildAvatarUrl();
    }
}
