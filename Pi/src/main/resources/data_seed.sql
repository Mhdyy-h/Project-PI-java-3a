-- ============================================================
-- BioSync – Données de démonstration
-- Quiz mentaux & questions (thème bien-être / santé mentale)
-- ============================================================
-- Pré-requis : au moins un utilisateur dans la table utilisateur.
-- Le script utilise le premier id trouvé comme créateur des quiz.
-- ============================================================

SET @admin_id = (SELECT id FROM utilisateur ORDER BY id LIMIT 1);

-- ============================================================
-- QUIZ 1 – Évaluation du Stress au Quotidien  (Modéré – niveau 5)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Évaluation du Stress au Quotidien', 5, 60, 'Argent', NOW(),
   @admin_id, 'disponible', 25.0,
   'Mesurez votre niveau de stress quotidien et découvrez des stratégies pour mieux le gérer.');

SET @q1 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q1,
 'Quelle est la principale hormone libérée lors d\'une réaction de stress aigu ?',
 'Le cortisol',
 'La mélatonine|La sérotonine|L\'insuline',
 50),

(@q1,
 'Parmi les symptômes suivants, lequel est caractéristique d\'un stress chronique ?',
 'Fatigue persistante et troubles du sommeil',
 'Augmentation de l\'énergie|Amélioration de la concentration|Gain d\'appétit',
 50),

(@q1,
 'Quelle technique de respiration est reconnue pour réduire rapidement le stress ?',
 'La respiration abdominale lente (4-7-8)',
 'L\'hyperventilation volontaire|La respiration thoracique rapide|La rétention d\'air prolongée',
 50),

(@q1,
 'Combien de minutes d\'activité physique modérée par jour sont recommandées pour réduire le stress ?',
 '30 minutes',
 '5 minutes|2 heures|1 heure',
 50),

(@q1,
 'Quel terme désigne le phénomène d\'épuisement professionnel lié au stress chronique au travail ?',
 'Le burn-out',
 'Le bore-out|La dépression saisonnière|L\'hyperactivité',
 50),

(@q1,
 'Parmi ces habitudes, laquelle aggrave le stress chronique ?',
 'La consommation excessive de caféine',
 'La pratique régulière du yoga|Les promenades en nature|La méditation quotidienne',
 50),

(@q1,
 'La technique STOP en gestion du stress signifie :',
 'Stopper, Prendre du recul, Observer, Poursuivre consciemment',
 'Souffler, Tenir, Observer, Parler|Stimuler, Tolérer, Oublier, Progresser|Stopper, Traiter, Optimiser, Planifier',
 100);

-- ============================================================
-- QUIZ 2 – Qualité du Sommeil  (Léger – niveau 2)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Qualité du Sommeil et Récupération', 2, 50, 'Bronze', NOW(),
   @admin_id, 'disponible', 20.0,
   'Évaluez vos habitudes de sommeil et identifiez les pistes pour améliorer votre récupération nocturne.');

SET @q2 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q2,
 'Quelle est la durée de sommeil recommandée pour un adulte en bonne santé ?',
 '7 à 9 heures',
 '4 à 5 heures|10 à 12 heures|5 à 6 heures',
 50),

(@q2,
 'Quelle hormone joue un rôle clé dans la régulation du cycle veille-sommeil ?',
 'La mélatonine',
 'L\'adrénaline|La dopamine|Le glucagon',
 50),

(@q2,
 'Quelle pratique améliore significativement la qualité du sommeil ?',
 'Se coucher et se lever à des heures régulières',
 'Regarder un écran lumineux au lit|Faire une sieste de 2 heures le soir|Pratiquer un sport intensif juste avant dormir',
 50),

(@q2,
 'Le trouble du sommeil caractérisé par des arrêts respiratoires répétés pendant la nuit s\'appelle :',
 'L\'apnée du sommeil',
 'L\'insomnie primaire|La narcolepsie|Le somnambulisme',
 50),

(@q2,
 'Quelle température ambiante favorise le plus l\'endormissement ?',
 'Entre 16 et 19 °C',
 'Entre 24 et 27 °C|Entre 10 et 14 °C|Entre 20 et 23 °C',
 50),

(@q2,
 'Combien de cycles de sommeil (d\'environ 90 min) réalise-t-on en moyenne par nuit ?',
 '4 à 6 cycles',
 '1 à 2 cycles|7 à 9 cycles|10 cycles ou plus',
 50),

(@q2,
 'Quelle est la phase du sommeil indispensable à la consolidation de la mémoire ?',
 'Le sommeil paradoxal (REM)',
 'Le sommeil léger (N1)|Le sommeil lent profond uniquement|L\'éveil nocturne bref',
 100);

-- ============================================================
-- QUIZ 3 – Gestion de l'Anxiété  (Élevé – niveau 7)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Comprendre et Gérer l\'Anxiété', 7, 70, 'Or', NOW(),
   @admin_id, 'disponible', 30.0,
   'Approfondissez votre compréhension des mécanismes anxieux et des outils thérapeutiques validés pour les gérer.');

SET @q3 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q3,
 'Quelle est la distinction principale entre le stress et l\'anxiété ?',
 'Le stress a une cause identifiable, l\'anxiété persiste souvent sans déclencheur précis',
 'L\'anxiété est toujours plus intense que le stress|Le stress est une maladie, l\'anxiété non|Ils sont parfaitement synonymes',
 100),

(@q3,
 'La TCC (Thérapie Cognitive et Comportementale) agit principalement sur :',
 'Les pensées automatiques négatives et les comportements d\'évitement',
 'Les souvenirs d\'enfance refoulés|La chimie cérébrale exclusivement|Les relations familiales uniquement',
 100),

(@q3,
 'Quel symptôme physique est le plus fréquemment associé à une crise d\'anxiété ?',
 'Palpitations cardiaques et sensation d\'oppression thoracique',
 'Somnolence intense|Amélioration soudaine de la vue|Ralentissement du rythme cardiaque',
 50),

(@q3,
 'La technique de "grounding 5-4-3-2-1" consiste à :',
 'Identifier 5 choses vues, 4 entendues, 3 touchées, 2 senties, 1 goûtée',
 'Compter jusqu\'à 5 puis retenir sa respiration|Répéter 5 affirmations positives|Faire 5 mouvements de relaxation',
 50),

(@q3,
 'Quel trouble anxieux est caractérisé par une peur intense et irrationnelle d\'un objet ou d\'une situation spécifique ?',
 'Une phobie spécifique',
 'Le trouble obsessionnel-compulsif|Le trouble de stress post-traumatique|Le trouble panique',
 50),

(@q3,
 'La pleine conscience (mindfulness) réduit l\'anxiété principalement en :',
 'Ramenant l\'attention au moment présent sans jugement',
 'Supprimant les pensées négatives|Induisant un état de sommeil léger|Augmentant la production de cortisol',
 50),

(@q3,
 'Parmi ces applications pratiques, laquelle est contre-productive face à l\'anxiété ?',
 'L\'évitement systématique des situations anxiogènes',
 'L\'exposition progressive et graduelle|La relaxation musculaire de Jacobson|La cohérence cardiaque',
 100),

(@q3,
 'Le trouble d\'anxiété généralisée (TAG) se caractérise par des inquiétudes excessives durant au moins :',
 '6 mois',
 '2 semaines|1 mois|3 ans',
 50);

-- ============================================================
-- QUIZ 4 – Performance Cognitive et Mémoire  (Léger – niveau 2)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Performance Cognitive et Mémoire', 2, 60, 'Argent', NOW(),
   @admin_id, 'disponible', 22.0,
   'Testez vos connaissances sur le fonctionnement de la mémoire, de l\'attention et des habitudes qui optimisent vos capacités cognitives.');

SET @q4 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q4,
 'Quelle structure cérébrale est essentielle à la formation des souvenirs à long terme ?',
 'L\'hippocampe',
 'Le cervelet|L\'amygdale|Le corps calleux',
 50),

(@q4,
 'La méthode des "loci" (palais mental) est une technique permettant de :',
 'Mémoriser des informations en les associant à des lieux imaginaires',
 'Améliorer la vitesse de lecture|Calculer mentalement plus vite|Réduire le temps de sommeil nécessaire',
 50),

(@q4,
 'Quelle habitude quotidienne a le plus d\'impact positif prouvé sur les fonctions cognitives ?',
 'L\'exercice physique aérobique régulier',
 'Regarder la télévision 3h par jour|Consommer des suppléments de vitamine C en excès|Travailler 12 heures sans pause',
 50),

(@q4,
 'Qu\'est-ce que l\'effet de position sérielle en mémorisation ?',
 'On retient mieux le début et la fin d\'une liste que le milieu',
 'On retient mieux le milieu d\'une liste|La position assise améliore la mémorisation|On mémorise uniquement les informations colorées',
 100),

(@q4,
 'La technique Pomodoro améliore la concentration en alternant :',
 '25 minutes de travail concentré et 5 minutes de pause',
 '1 heure de travail et 1 heure de repos|10 minutes de travail et 50 minutes de repos|45 minutes de travail sans aucune pause',
 50),

(@q4,
 'Quel micronutriment est particulièrement important pour la santé neuronale et la mémoire ?',
 'Les acides gras oméga-3',
 'Le fer uniquement|Le calcium|Le sodium',
 50),

(@q4,
 'Le "brain fog" (brouillard mental) est souvent associé à :',
 'Manque de sommeil, stress chronique ou carences nutritionnelles',
 'Un QI trop élevé|Un excès de vitamine D|La pratique régulière de sport',
 50),

(@q4,
 'Quelle est la capacité approximative de la mémoire de travail (court terme) chez l\'adulte ?',
 '7 éléments (±2), soit entre 5 et 9 unités d\'information',
 'Plus de 100 éléments simultanés|Exactement 3 éléments|2 éléments seulement',
 100);

-- ============================================================
-- QUIZ 5 – Bien-être Émotionnel  (Modéré – niveau 5)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Bien-être Émotionnel et Intelligence Affective', 5, 60, 'Argent', NOW(),
   @admin_id, 'disponible', 28.0,
   'Explorez les fondements de l\'intelligence émotionnelle, apprenez à identifier et réguler vos émotions pour un bien-être durable.');

SET @q5 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q5,
 'Daniel Goleman définit l\'intelligence émotionnelle principalement comme :',
 'La capacité à reconnaître, comprendre et gérer ses émotions et celles des autres',
 'Le quotient intellectuel lié aux émotions|L\'absence totale d\'émotions négatives|La capacité à simuler des émotions',
 100),

(@q5,
 'Parmi ces émotions, lesquelles sont considérées comme des "émotions primaires universelles" selon Paul Ekman ?',
 'Joie, tristesse, peur, colère, dégoût, surprise',
 'Jalousie, honte, culpabilité, fierté|Amour, haine, envie, satisfaction|Anxiété, dépression, euphorie, nostalgie',
 50),

(@q5,
 'La "roue des émotions" est un outil conçu pour :',
 'Identifier et nommer précisément les nuances d\'une émotion ressentie',
 'Prédire les émotions des autres|Mesurer le bonheur sur une échelle de 1 à 10|Supprimer les émotions négatives',
 50),

(@q5,
 'Quelle pratique favorise la régulation émotionnelle selon les neurosciences ?',
 'Nommer l\'émotion ressentie (affect labeling)',
 'Réprimer l\'émotion jusqu\'à ce qu\'elle disparaisse|Éviter toute situation émotionnellement chargée|Rester seul face aux émotions difficiles',
 50),

(@q5,
 'La "résilience émotionnelle" désigne :',
 'La capacité à se remettre des épreuves et à s\'adapter positivement aux adversités',
 'L\'insensibilité totale aux émotions|La capacité à ne jamais souffrir|L\'absence de réactions émotionnelles',
 50),

(@q5,
 'Dans la communication non-violente (CNV) de Marshall Rosenberg, quelle est la première étape ?',
 'Observer les faits sans jugement ni évaluation',
 'Exprimer immédiatement sa colère|Formuler des demandes exigeantes|Analyser les intentions de l\'autre',
 100),

(@q5,
 'L\'auto-compassion (Kristin Neff) se distingue de la complaisance envers soi en ce qu\'elle :',
 'Traite ses propres difficultés avec la même bienveillance qu\'on offrirait à un ami',
 'Justifie tous ses comportements négatifs|Supprime la motivation à s\'améliorer|Ignore les erreurs commises',
 100);

-- ============================================================
-- QUIZ 6 – Santé Mentale au Travail  (Modéré – niveau 5)
-- ============================================================
INSERT INTO quiz_mental
  (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz,
   utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive)
VALUES
  ('Santé Mentale et Équilibre Vie Pro/Perso', 5, 65, 'Or', NOW(),
   @admin_id, 'disponible', 27.0,
   'Évaluez votre équilibre professionnel et identifiez les signaux d\'alarme du burn-out pour agir avant l\'épuisement.');

SET @q6 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q6,
 'Quels sont les 3 dimensions principales du burn-out selon Christina Maslach ?',
 'Épuisement émotionnel, dépersonnalisation, sentiment de perte d\'efficacité',
 'Dépression, anxiété, insomnie|Fatigue physique, migraine, troubles digestifs|Tristesse, colère, désespoir',
 100),

(@q6,
 'Parmi ces signaux, lequel indique un risque élevé de burn-out ?',
 'Travailler le week-end de manière compulsive et ressentir une fatigue qui ne disparaît pas avec le repos',
 'Prendre des congés régulièrement|Déléguer certaines tâches à des collègues|Demander de l\'aide à son manager',
 50),

(@q6,
 'La méthode "Getting Things Done" (GTD) aide principalement à :',
 'Vider son esprit en externalisant les tâches dans un système fiable',
 'Travailler plus de 12 heures par jour|Supprimer toutes les réunions d\'équipe|Automatiser le travail cognitif',
 50),

(@q6,
 'Le "quiet quitting" désigne :',
 'Réduire son engagement au strict minimum requis par le contrat sans démissionner',
 'Partir sans prévenir son employeur|Demander une mutation en silence|Travailler depuis chez soi sans le dire',
 50),

(@q6,
 'Pour préserver sa santé mentale au travail, il est recommandé de :',
 'Établir des limites claires entre temps de travail et temps personnel',
 'Répondre aux emails professionnels la nuit pour montrer son engagement|Ne jamais exprimer ses difficultés à son manager|Supprimer toutes les pauses pour être plus productif',
 50),

(@q6,
 'Le droit à la déconnexion en France, consacré par la loi El Khomri (2017), concerne :',
 'Le droit des salariés à ne pas être connectés aux outils numériques professionnels en dehors des heures de travail',
 'L\'obligation de couper son téléphone personnel au bureau|Le droit de refuser tout télétravail|L\'interdiction des réunions après 18h',
 100),

(@q6,
 'Quelle pratique organisationnelle améliore durablement le bien-être des équipes ?',
 'Donner de l\'autonomie et de la reconnaissance aux collaborateurs',
 'Augmenter la pression des délais pour stimuler la productivité|Multiplier les réunions de contrôle|Réduire les congés payés',
 50),

(@q6,
 'La psychologie positive (Seligman) propose le modèle PERMA. Que signifie le "R" ?',
 'Relations positives (Relationships)',
 'Résilience|Rationalité|Rémunération',
 100);

-- ============================================================
-- Vérification rapide
-- ============================================================
SELECT
  q.id,
  q.titre,
  q.statut,
  q.medaille_quiz,
  COUNT(qn.id) AS nb_questions,
  SUM(qn.points_valeur) AS total_points
FROM quiz_mental q
LEFT JOIN question qn ON qn.quiz_id = q.id
GROUP BY q.id, q.titre, q.statut, q.medaille_quiz
ORDER BY q.id;
