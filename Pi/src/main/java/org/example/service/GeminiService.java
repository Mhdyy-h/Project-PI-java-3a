package org.example.service;

import org.example.model.Aliment;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeminiService {

    private static final String API_KEY =
            "AIzaSyCkSFRhPvOiIC6VGBx3u5CCEbkj3p3aXyY";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    // ══════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE
    // ══════════════════════════════════════════════════

    public static Aliment obtenirInfoNutritionnelle(String query) {
        if (query == null || query.trim().isEmpty()) return null;

        if (!estUnAliment(query.trim())) {
            return null;
        }

        try {
            return appellerGemini(query.trim());
        } catch (Exception e) {
            System.err.println("Gemini API error: " + e.getMessage());
            return fallbackBaseLocale(query.trim().toLowerCase());
        }
    }

    // ══════════════════════════════════════════════════
    //  VALIDATION — estUnAliment (SIMPLIFIÉE)
    //
    //  Principe : Java ne fait QUE bloquer les cas
    //  évidents (salutations, questions, phrases trop longues).
    //  C'est Gemini qui décide si c'est un aliment ou non,
    //  via son prompt strict. On supprime la Règle 4
    //  qui acceptait "paris", "einstein", etc.
    // ══════════════════════════════════════════════════

    public static boolean estUnAliment(String query) {
        if (query == null || query.trim().isEmpty()) return false;

        String q = query.trim().toLowerCase()
                .replace("é", "e").replace("è", "e").replace("ê", "e")
                .replace("à", "a").replace("â", "a")
                .replace("ô", "o").replace("û", "u").replace("ù", "u")
                .replace("î", "i").replace("ï", "i")
                .replace("ç", "c");

        String[] mots = q.split("\\s+");

        // Règle 1 : phrase trop longue = question, pas un aliment
        if (mots.length > 5) return false;

        // Règle 2 : bloquer les mots de refus évidents (comparaison MOT ENTIER)
        String[] motsRefus = {
                "quelle", "quel", "quels", "quelles",
                "comment", "pourquoi", "quand", "combien", "que", "quoi",
                "capitale", "pays", "ville", "region", "continent",
                "president", "ministre", "politique", "gouvernement",
                "histoire", "science", "mathematique", "physique", "chimie",
                "sport", "football", "basketball", "tennis", "rugby",
                "cinema", "film", "serie", "musique", "chanteur", "acteur",
                "meteo", "climat", "temperature",
                "bourse", "argent", "economie", "monnaie", "bitcoin",
                "religion", "philosophie", "dieu",
                "bonjour", "salut", "merci", "bonsoir", "coucou", "hello",
                "hi", "hey", "bye", "aurevoir",
                "tunisie", "france", "maroc", "algerie", "tunis",
                "paris", "london", "madrid", "rome", "berlin",
                "expliquer", "definir", "raconte", "parle", "donne", "dis",
                "aide", "help", "test", "essai",
                "who", "what", "when", "where", "why", "how"
        };

        // Comparaison mot à mot (equals), jamais contains()
        for (String mot : mots) {
            for (String refus : motsRefus) {
                if (mot.equals(refus)) return false;
            }
        }

        // Règle 3 : aliments connus → accepter immédiatement sans appel API
        String[] alimentsConnus = {
                "pomme", "banane", "orange", "citron", "mangue", "ananas",
                "raisin", "fraise", "framboise", "myrtille", "peche", "poire",
                "kiwi", "melon", "pasteque", "abricot", "cerise", "prune",
                "figue", "datte", "avocat", "grenade", "coco", "litchi",
                "tomate", "carotte", "courgette", "aubergine", "poivron",
                "concombre", "laitue", "salade", "epinard", "brocoli",
                "chou", "poireau", "oignon", "ail", "celeri", "radis",
                "betterave", "champignon", "patate", "courge", "navet",
                "pomme de terre", "pois chiche", "haricot", "feve",
                "poulet", "boeuf", "porc", "agneau", "dinde", "veau", "lapin",
                "saumon", "thon", "sardine", "cabillaud", "crevette", "moule",
                "truite", "maquereau", "hareng", "seiche", "calmar",
                "oeuf", "lait", "fromage", "yaourt", "beurre", "creme",
                "mozzarella", "parmesan", "gruyere", "camembert", "ricotta",
                "riz", "pates", "pain", "farine", "avoine", "quinoa",
                "couscous", "semoule", "cereale", "muesli", "boulgour",
                "lentille", "soja", "tofu", "tempeh",
                "amande", "noix", "noisette", "cajou", "pistache", "cacahuete",
                "sucre", "miel", "chocolat", "confiture", "nutella",
                "cafe", "the", "jus", "coca", "eau", "smoothie", "lait",
                "huile", "margarine", "jambon", "saucisse", "bacon",
                "merguez", "kefta", "harissa", "brik", "ojja", "chorba",
                "makroudh", "samsa", "mlawi", "asida", "lablabi"
        };

        for (String aliment : alimentsConnus) {
            if (q.equals(aliment)) return true;
            for (String mot : mots) {
                if (mot.equals(aliment)) return true;
            }
        }

        // Règle 4 : pour tout le reste, on demande à Gemini de valider.
        // MAIS on filtre les mots qui ressemblent à des noms propres
        // (commence par une majuscule dans la saisie originale → ville, prénom...)
        String original = query.trim();
        // Si tous les mots commencent par une majuscule ET ne sont pas dans alimentsConnus
        // → probablement un nom propre, on refuse
        boolean tousNomsPropres = true;
        for (String mot : original.split("\\s+")) {
            if (mot.length() > 0 && Character.isLowerCase(mot.charAt(0))) {
                tousNomsPropres = false;
                break;
            }
        }
        if (tousNomsPropres && original.split("\\s+").length == 1
                && original.length() > 2) {
            // Mot unique avec majuscule initiale et pas dans alimentsConnus
            // → laisser Gemini décider (cas : "Quinoa", "Tofu" avec majuscule)
            // On n'applique PAS de refus automatique, Gemini tranche
        }

        // Laisser Gemini décider pour tout le reste
        // Gemini retournera {"erreur":"non_alimentaire"} si ce n'est pas un aliment
        return true;
    }

    // ══════════════════════════════════════════════════
    //  APPEL API GEMINI — prompt renforcé
    // ══════════════════════════════════════════════════

    private static Aliment appellerGemini(String query) throws Exception {
        // Prompt beaucoup plus strict : Gemini doit rejeter tout ce qui
        // n'est pas explicitement un aliment comestible
        String prompt =
                "Tu es une base de données nutritionnelle. Tu reponds UNIQUEMENT aux aliments comestibles. "
                        + "Analyse la saisie utilisateur : \"" + query + "\"\n\n"
                        + "ETAPE 1 - VALIDATION STRICTE :\n"
                        + "Est-ce que \"" + query + "\" est un aliment ou une boisson que l'être humain consomme ?\n"
                        + "- Si c'est une VILLE, un PAYS, un NOM PROPRE, un CONCEPT, une PERSONNE, "
                        + "un OBJET, un SPORT, un FILM, une SALUTATION, ou QUOI QUE CE SOIT qui n'est PAS mangeable : "
                        + "reponds UNIQUEMENT ce JSON exact, rien d'autre : {\"erreur\":\"non_alimentaire\"}\n"
                        + "- Exemples de refus : paris, france, einstein, voiture, bonjour, sport, calcul\n\n"
                        + "ETAPE 2 - SI et SEULEMENT SI c'est un aliment :\n"
                        + "Donne les valeurs nutritionnelles pour 100g avec ce JSON exact, sans texte avant ou apres :\n"
                        + "{\"nomAliment\":\"nom officiel en francais\","
                        + "\"calories\":52,"
                        + "\"proteines\":0.3,"
                        + "\"glucides\":14.0,"
                        + "\"lipides\":0.2,"
                        + "\"indexGlycemique\":38,"
                        + "\"estExcitant\":false,"
                        + "\"typeAliment\":\"Fruit\"}\n"
                        + "estExcitant = true UNIQUEMENT pour cafe, the, boissons energisantes, chocolat.\n"
                        + "typeAliment = Fruit | Legume | Viande | Poisson | Feculent | Produit laitier | "
                        + "Legumineuse | Oleagineux | Boisson | Confiserie | Corps gras | Charcuterie | Autre\n"
                        + "RAPPEL : si le moindre doute que ce n'est pas un aliment, reponds {\"erreur\":\"non_alimentaire\"}";

        String requestBody = "{"
                + "\"contents\":[{\"parts\":[{\"text\":\""
                + escapeJson(prompt) + "\"}]}],"
                + "\"generationConfig\":{"
                + "\"temperature\":0.0,"
                + "\"maxOutputTokens\":200"
                + "}"
                + "}";

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) response.append(scanner.nextLine());
        }

        return parserReponseGemini(response.toString(), query);
    }

    // ══════════════════════════════════════════════════
    //  PARSING
    // ══════════════════════════════════════════════════

    private static Aliment parserReponseGemini(String response, String query) {
        try {
            String text = extraireTexteGemini(response);
            if (text == null || text.isEmpty())
                return fallbackBaseLocale(query.toLowerCase());

            text = text.trim()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Détecter la réponse de refus de Gemini
            if (text.contains("\"erreur\"") && text.contains("non_alimentaire"))
                return null;

            // Détecter si Gemini a répondu en texte libre (non JSON) → refus
            if (!text.startsWith("{"))
                return null;

            return parserJsonAliment(text, query);

        } catch (Exception e) {
            System.err.println("Parsing error: " + e.getMessage());
            return fallbackBaseLocale(query.toLowerCase());
        }
    }

    private static String extraireTexteGemini(String response) {
        String marker = "\"text\":\"";
        int start = response.indexOf(marker);
        if (start == -1) return null;
        start += marker.length();

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < response.length(); i++) {
            char c = response.charAt(i);
            if (escape) {
                if      (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else if (c == '"') sb.append('"');
                else if (c == '\\') sb.append('\\');
                else sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Aliment parserJsonAliment(String json, String query) {
        Aliment aliment = new Aliment();

        String nomOfficiel = extraireString(json, "nomAliment", null);
        if (nomOfficiel == null || nomOfficiel.trim().isEmpty()) {
            String[] mots = query.trim().split("\\s+");
            String premier = mots[0];
            nomOfficiel = Character.toUpperCase(premier.charAt(0))
                    + premier.substring(1).toLowerCase();
        }

        aliment.setNomAliment(nomOfficiel);
        aliment.setCalories((int) extraireDouble(json, "calories", 50));
        aliment.setProteines(extraireDouble(json, "proteines", 1.0));
        aliment.setGlucides(extraireDouble(json, "glucides", 10.0));
        aliment.setLipides(extraireDouble(json, "lipides", 0.5));
        aliment.setIndexGlycemique((int) extraireDouble(json, "indexGlycemique", 0));
        aliment.setEstExcitant(extraireBoolean(json, "estExcitant", false));
        aliment.setTypeAliment(extraireString(json, "typeAliment", "Autre"));

        return aliment;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS JSON
    // ══════════════════════════════════════════════════

    private static String extraireString(String json, String key, String defaut) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return defaut;
        start += marker.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return defaut;
        return json.substring(start, end);
    }

    private static double extraireDouble(String json, String key, double defaut) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return defaut;
        start += marker.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length()
                && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.'
                || json.charAt(end) == '-')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return defaut;
        }
    }

    private static boolean extraireBoolean(String json, String key, boolean defaut) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return defaut;
        start += marker.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start + 4 <= json.length()
                && json.substring(start, start + 4).equals("true")) return true;
        if (start + 5 <= json.length()
                && json.substring(start, start + 5).equals("false")) return false;
        return defaut;
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }

    // ══════════════════════════════════════════════════
    //  FALLBACK BASE LOCALE
    // ══════════════════════════════════════════════════

    private static Aliment fallbackBaseLocale(String q) {
        Aliment a = new Aliment();

        if (q.contains("pomme de terre") || q.contains("patate")) {
            a.setNomAliment("Pomme de terre"); a.setCalories(77); a.setProteines(2.0);
            a.setGlucides(17.0); a.setLipides(0.1); a.setIndexGlycemique(65);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("pomme")) {
            a.setNomAliment("Pomme"); a.setCalories(52); a.setProteines(0.3);
            a.setGlucides(14.0); a.setLipides(0.2); a.setIndexGlycemique(38);
            a.setEstExcitant(false); a.setTypeAliment("Fruit");
        } else if (q.contains("banane")) {
            a.setNomAliment("Banane"); a.setCalories(89); a.setProteines(1.1);
            a.setGlucides(23.0); a.setLipides(0.3); a.setIndexGlycemique(55);
            a.setEstExcitant(false); a.setTypeAliment("Fruit");
        } else if (q.contains("orange")) {
            a.setNomAliment("Orange"); a.setCalories(47); a.setProteines(0.9);
            a.setGlucides(12.0); a.setLipides(0.1); a.setIndexGlycemique(40);
            a.setEstExcitant(false); a.setTypeAliment("Fruit");
        } else if (q.contains("avocat")) {
            a.setNomAliment("Avocat"); a.setCalories(160); a.setProteines(2.0);
            a.setGlucides(9.0); a.setLipides(15.0); a.setIndexGlycemique(10);
            a.setEstExcitant(false); a.setTypeAliment("Fruit");
        } else if (q.contains("poulet")) {
            a.setNomAliment("Poulet grille"); a.setCalories(165); a.setProteines(31.0);
            a.setGlucides(0.0); a.setLipides(3.6); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Viande");
        } else if (q.contains("boeuf")) {
            a.setNomAliment("Boeuf"); a.setCalories(250); a.setProteines(26.0);
            a.setGlucides(0.0); a.setLipides(15.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Viande");
        } else if (q.contains("saumon")) {
            a.setNomAliment("Saumon"); a.setCalories(208); a.setProteines(20.0);
            a.setGlucides(0.0); a.setLipides(13.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Poisson");
        } else if (q.contains("thon")) {
            a.setNomAliment("Thon"); a.setCalories(132); a.setProteines(28.0);
            a.setGlucides(0.0); a.setLipides(1.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Poisson");
        } else if (q.contains("moule")) {
            a.setNomAliment("Moules cuites"); a.setCalories(86); a.setProteines(11.9);
            a.setGlucides(3.7); a.setLipides(2.2); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Fruit de mer");
        } else if (q.contains("crevette")) {
            a.setNomAliment("Crevettes cuites"); a.setCalories(99); a.setProteines(24.0);
            a.setGlucides(0.2); a.setLipides(0.3); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Fruit de mer");
        } else if (q.contains("oeuf")) {
            a.setNomAliment("Oeuf entier"); a.setCalories(155); a.setProteines(13.0);
            a.setGlucides(1.1); a.setLipides(11.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Proteine");
        } else if (q.contains("quinoa")) {
            a.setNomAliment("Quinoa cuit"); a.setCalories(120); a.setProteines(4.4);
            a.setGlucides(22.0); a.setLipides(1.9); a.setIndexGlycemique(53);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("couscous")) {
            a.setNomAliment("Couscous cuit"); a.setCalories(112); a.setProteines(3.8);
            a.setGlucides(23.0); a.setLipides(0.2); a.setIndexGlycemique(65);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("riz")) {
            a.setNomAliment("Riz blanc cuit"); a.setCalories(130); a.setProteines(2.7);
            a.setGlucides(28.0); a.setLipides(0.3); a.setIndexGlycemique(73);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("pates")) {
            a.setNomAliment("Pates cuites"); a.setCalories(131); a.setProteines(5.0);
            a.setGlucides(25.0); a.setLipides(1.1); a.setIndexGlycemique(50);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("pain")) {
            a.setNomAliment("Pain blanc"); a.setCalories(265); a.setProteines(9.0);
            a.setGlucides(49.0); a.setLipides(3.2); a.setIndexGlycemique(70);
            a.setEstExcitant(false); a.setTypeAliment("Feculent");
        } else if (q.contains("lentille")) {
            a.setNomAliment("Lentilles cuites"); a.setCalories(116); a.setProteines(9.0);
            a.setGlucides(20.0); a.setLipides(0.4); a.setIndexGlycemique(32);
            a.setEstExcitant(false); a.setTypeAliment("Legumineuse");
        } else if (q.contains("pois chiche")) {
            a.setNomAliment("Pois chiches cuits"); a.setCalories(164); a.setProteines(8.9);
            a.setGlucides(27.0); a.setLipides(2.6); a.setIndexGlycemique(28);
            a.setEstExcitant(false); a.setTypeAliment("Legumineuse");
        } else if (q.contains("cafe")) {
            a.setNomAliment("Cafe expresso"); a.setCalories(2); a.setProteines(0.1);
            a.setGlucides(0.0); a.setLipides(0.0); a.setIndexGlycemique(0);
            a.setEstExcitant(true); a.setTypeAliment("Boisson");
        } else if (q.contains("the")) {
            a.setNomAliment("The infuse"); a.setCalories(1); a.setProteines(0.0);
            a.setGlucides(0.2); a.setLipides(0.0); a.setIndexGlycemique(0);
            a.setEstExcitant(true); a.setTypeAliment("Boisson");
        } else if (q.contains("lait")) {
            a.setNomAliment("Lait demi-ecreme"); a.setCalories(46); a.setProteines(3.2);
            a.setGlucides(4.8); a.setLipides(1.5); a.setIndexGlycemique(30);
            a.setEstExcitant(false); a.setTypeAliment("Produit laitier");
        } else if (q.contains("yaourt")) {
            a.setNomAliment("Yaourt nature"); a.setCalories(59); a.setProteines(3.5);
            a.setGlucides(4.7); a.setLipides(3.3); a.setIndexGlycemique(35);
            a.setEstExcitant(false); a.setTypeAliment("Produit laitier");
        } else if (q.contains("fromage")) {
            a.setNomAliment("Fromage"); a.setCalories(402); a.setProteines(25.0);
            a.setGlucides(1.3); a.setLipides(33.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Produit laitier");
        } else if (q.contains("tomate")) {
            a.setNomAliment("Tomate crue"); a.setCalories(18); a.setProteines(0.9);
            a.setGlucides(3.9); a.setLipides(0.2); a.setIndexGlycemique(15);
            a.setEstExcitant(false); a.setTypeAliment("Legume");
        } else if (q.contains("carotte")) {
            a.setNomAliment("Carotte crue"); a.setCalories(41); a.setProteines(0.9);
            a.setGlucides(10.0); a.setLipides(0.2); a.setIndexGlycemique(47);
            a.setEstExcitant(false); a.setTypeAliment("Legume");
        } else if (q.contains("courgette")) {
            a.setNomAliment("Courgette"); a.setCalories(17); a.setProteines(1.2);
            a.setGlucides(3.1); a.setLipides(0.3); a.setIndexGlycemique(15);
            a.setEstExcitant(false); a.setTypeAliment("Legume");
        } else if (q.contains("chocolat")) {
            a.setNomAliment("Chocolat noir 70%"); a.setCalories(546); a.setProteines(5.0);
            a.setGlucides(60.0); a.setLipides(31.0); a.setIndexGlycemique(23);
            a.setEstExcitant(true); a.setTypeAliment("Confiserie");
        } else if (q.contains("amande")) {
            a.setNomAliment("Amandes"); a.setCalories(579); a.setProteines(21.0);
            a.setGlucides(22.0); a.setLipides(50.0); a.setIndexGlycemique(15);
            a.setEstExcitant(false); a.setTypeAliment("Oleagineux");
        } else if (q.contains("miel")) {
            a.setNomAliment("Miel"); a.setCalories(304); a.setProteines(0.3);
            a.setGlucides(82.0); a.setLipides(0.0); a.setIndexGlycemique(61);
            a.setEstExcitant(false); a.setTypeAliment("Sucrant");
        } else if (q.contains("beurre")) {
            a.setNomAliment("Beurre"); a.setCalories(717); a.setProteines(0.9);
            a.setGlucides(0.1); a.setLipides(81.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Corps gras");
        } else if (q.contains("huile")) {
            a.setNomAliment("Huile d'olive"); a.setCalories(884); a.setProteines(0.0);
            a.setGlucides(0.0); a.setLipides(100.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Corps gras");
        } else if (q.contains("merguez")) {
            a.setNomAliment("Merguez grillee"); a.setCalories(310); a.setProteines(14.0);
            a.setGlucides(1.5); a.setLipides(27.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Charcuterie");
        } else if (q.contains("jambon")) {
            a.setNomAliment("Jambon blanc"); a.setCalories(107); a.setProteines(18.0);
            a.setGlucides(1.0); a.setLipides(3.0); a.setIndexGlycemique(0);
            a.setEstExcitant(false); a.setTypeAliment("Charcuterie");
        } else {
            String nomCapitalise = !q.isEmpty()
                    ? Character.toUpperCase(q.charAt(0)) + q.substring(1) : q;
            a.setNomAliment(nomCapitalise);
            a.setCalories(100); a.setProteines(3.0);
            a.setGlucides(15.0); a.setLipides(2.0);
            a.setIndexGlycemique(50); a.setEstExcitant(false);
            a.setTypeAliment("Autre");
        }

        return a;
    }
}