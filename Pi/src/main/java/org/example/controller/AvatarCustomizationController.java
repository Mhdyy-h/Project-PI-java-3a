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
import java.util.LinkedHashMap;
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

    // Options pour chaque trait (API DiceBear Avataaars v9 - OFFICIAL VALUES)
    private final String[] skinColors = {"ffdbb4", "f8d25c", "edb98a", "d08b5b", "ae5d29", "614335"};
    private final String[] topStyles = {"", "bigHair", "bob", "bun", "curly", "curvy", "dreads", "dreads01", "dreads02", "frida", "frizzle", "fro", "froBand", "hat", "hijab", "longButNotTooLong", "miaWallace", "shaggy", "shaggyMullet", "shavedSides", "shortCurly", "shortFlat", "shortRound", "shortWaved", "sides", "straight01", "straight02", "straightAndStrand", "theCaesar", "theCaesarAndSidePart", "turban", "winterHat1", "winterHat02", "winterHat03", "winterHat04"};
    private final String[] hairColors = {"", "2c1b18", "4a312c", "724133", "a55728", "b58143", "c93305", "d6b370", "e8e1e1", "ecdcbf", "f59797"};
    private final String[] eyesOptions = {"default", "closed", "cry", "eyeRoll", "happy", "hearts", "side", "squint", "surprised", "wink", "winkWacky", "xDizzy"};
    private final String[] eyebrowsOptions = {"default", "angry", "angryNatural", "defaultNatural", "flatNatural", "frownNatural", "raisedExcited", "raisedExcitedNatural", "sadConcerned", "sadConcernedNatural", "unibrowNatural", "upDown", "upDownNatural"};
    private final String[] mouthOptions = {"default", "concerned", "disbelief", "eating", "grimace", "sad", "screamOpen", "serious", "smile", "tongue", "twinkle", "vomit"};
    private final String[] accessoriesOptions = {"", "eyepatch", "kurt", "prescription01", "prescription02", "round", "sunglasses", "wayfarers"};
    private final String[] clothesOptions = {"", "blazerAndShirt", "blazerAndSweater", "collarAndSweater", "graphicShirt", "hoodie", "overall", "shirtCrewNeck", "shirtScoopNeck", "shirtVNeck"};
    private final String[] clothesColors = {"", "3c4f5c", "65c9ff", "262e33", "5199e4", "25557c", "929598", "a7ffc4", "b1e2ff", "e6e6e6", "ff5c5c", "ff488e", "ffafb9", "ffffb1", "ffffff"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        setupListeners();
        updatePreview();
    }

    private void initializeComboBoxes() {
        // Peau
        skinColorCombo.getItems().addAll("Clair", "Jaune", "Pêche", "Bronze", "Marron", "Sombre");
        skinColorCombo.setValue("Pêche");

        // Coiffure
        hairStyleCombo.getItems().addAll(
            "Chauve", "Volumineux", "Carré", "Chignon", "Bouclés", "Ondulés",
            "Dreads", "Dreads 01", "Dreads 02", "Frida", "Frizzle",
            "Afro", "Afro bandeau", "Chapeau", "Hijab", "Longs moyens",
            "Mia Wallace", "Shaggy", "Shaggy mullet", "Rasés sur les côtés",
            "Court bouclé", "Court plat", "Court rond", "Court ondulé",
            "Sur les côtés", "Raide 01", "Raide 02", "Raide + mèche",
            "César", "César côté", "Turban",
            "Bonnet 1", "Bonnet 2", "Bonnet 3", "Bonnet 4"
        );
        hairStyleCombo.setValue("Chauve");

        // Couleur cheveux
        hairColorCombo.getItems().addAll(
            "Naturelle", "Noir", "Brun foncé", "Brun moyen", "Auburn",
            "Châtain", "Roux", "Blond foncé", "Platine", "Beige", "Rose"
        );
        hairColorCombo.setValue("Châtain");

        // Yeux
        eyesCombo.getItems().addAll(
            "Normaux", "Fermés", "Pleurer", "Roulés", "Heureux", "Cœurs",
            "Sur le côté", "Plissés", "Surpris", "Clin d'œil", "Clin comique", "Étourdis"
        );
        eyesCombo.setValue("Normaux");

        // Sourcils
        eyebrowsCombo.getItems().addAll(
            "Normaux", "Fâchés", "Fâchés naturels", "Naturels", "Plats",
            "Froncés", "Excités", "Excités naturels", "Soucieux",
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
            "Aucun", "Patch", "Kurt", "Ordonnance 01", "Ordonnance 02",
            "Rondes", "Soleil", "Wayfarers"
        );
        accessoriesCombo.setValue("Aucun");

        // Vêtements
        clothesCombo.getItems().addAll(
            "Aléatoire", "Blazer + Chemise", "Blazer + Pull", "Col + Pull",
            "T-shirt graphique", "Sweat à capuche", "Salopette",
            "Col rond", "Col scoop", "Col en V"
        );
        clothesCombo.setValue("Blazer + Chemise");

        // Couleur vêtements
        clothesColorCombo.getItems().addAll(
            "Aléatoire", "Bleu foncé", "Bleu ciel", "Noir", "Bleu", "Bleu marine",
            "Gris", "Vert clair", "Bleu clair", "Gris clair",
            "Rouge", "Rose", "Rose pâle", "Jaune", "Blanc"
        );
        clothesColorCombo.setValue("Blanc");
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
        // Randomize seed for truly different avatar each time
        seed = "random_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 100000);

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
        Map<String, String> params = new LinkedHashMap<>();

        // Seed fixe (pas de random) pour que l'avatar soit déterministe par les options
        params.put("seed", seed);
        params.put("size", "200");

        // Peau
        int skinIndex = skinColorCombo.getSelectionModel().getSelectedIndex();
        if (skinIndex >= 0) {
            params.put("skinColor", skinColors[skinIndex]);
        }

        // Cheveux (top) - index 0 = Chauve = pas de cheveux
        int hairIndex = hairStyleCombo.getSelectionModel().getSelectedIndex();
        if (hairIndex == 0) {
            // Chauve: no top, set topProbability=0 to hide hair
            params.put("topProbability", "0");
        } else if (hairIndex > 0 && hairIndex < topStyles.length) {
            params.put("top", topStyles[hairIndex]);
        }

        int hairColorIndex = hairColorCombo.getSelectionModel().getSelectedIndex();
        if (hairColorIndex > 0 && hairColorIndex < hairColors.length) {
            params.put("hairColor", hairColors[hairColorIndex]);
        }

        // Yeux
        int eyesIndex = eyesCombo.getSelectionModel().getSelectedIndex();
        if (eyesIndex >= 0 && eyesIndex < eyesOptions.length) {
            params.put("eyes", eyesOptions[eyesIndex]);
        }

        // Sourcils (API parameter name is "eyebrows" not "eyebrow")
        int browsIndex = eyebrowsCombo.getSelectionModel().getSelectedIndex();
        if (browsIndex >= 0 && browsIndex < eyebrowsOptions.length) {
            params.put("eyebrows", eyebrowsOptions[browsIndex]);
        }

        // Bouche
        int mouthIndex = mouthCombo.getSelectionModel().getSelectedIndex();
        if (mouthIndex >= 0 && mouthIndex < mouthOptions.length) {
            params.put("mouth", mouthOptions[mouthIndex]);
        }

        // Accessoires - index 0 = Aucun
        int accIndex = accessoriesCombo.getSelectionModel().getSelectedIndex();
        if (accIndex == 0) {
            // Aucun: hide accessories
            params.put("accessoriesProbability", "0");
        } else if (accIndex > 0 && accIndex < accessoriesOptions.length) {
            params.put("accessories", accessoriesOptions[accIndex]);
        }

        // Vêtements (API parameter name is "clothing" not "clothes") - index 0 = Aléatoire
        int clothesIndex = clothesCombo.getSelectionModel().getSelectedIndex();
        if (clothesIndex > 0 && clothesIndex < clothesOptions.length) {
            params.put("clothing", clothesOptions[clothesIndex]);
        }

        int clothesColorIndex = clothesColorCombo.getSelectionModel().getSelectedIndex();
        if (clothesColorIndex > 0 && clothesColorIndex < clothesColors.length) {
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
        // If avatar already loaded, return it
        if (currentAvatar != null) {
            return currentAvatar;
        }

        // Fallback: download synchronously from current URL
        try {
            String url = buildAvatarUrl();
            System.out.println("[Avatar] Fallback download: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(url).toURI())
                .header("Accept", "image/png")
                .GET()
                .timeout(java.time.Duration.ofSeconds(15))
                .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                currentAvatar = new Image(new java.io.ByteArrayInputStream(response.body()));
                System.out.println("[Avatar] Fallback image loaded: " + currentAvatar.getWidth() + "x" + currentAvatar.getHeight());
                return currentAvatar;
            } else {
                System.err.println("[Avatar] Fallback HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[Avatar] Fallback download error: " + e.getMessage());
        }
        return null;
    }

    public String getAvatarUrl() {
        return buildAvatarUrl();
    }
}
