-- ═══════════════════════════════════════════════════════════════
-- BioSync – Seed : 6 Quiz Santé Mentale + 48 Questions
-- Thèmes : Stress, Anxiété, Sommeil, Mémoire, Bien-être, Relations
-- ═══════════════════════════════════════════════════════════════

SET @uid = (SELECT id FROM utilisateur ORDER BY id LIMIT 1);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 1 : Gestion du Stress
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Gestion du Stress', 6, 70, 'Or', NOW(), @uid, 'disponible',
        'Évaluez votre compréhension des mécanismes du stress et des stratégies pour le maîtriser au quotidien.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q1, 'Quelle hormone est principalement libérée lors d''une réponse au stress aigu ?',
 'Le cortisol', 'La mélatonine|La sérotonine|L''insuline', 50),
(@q1, 'La technique de respiration 4-7-8 consiste à inspirer pendant combien de secondes ?',
 '4 secondes', '7 secondes|8 secondes|2 secondes', 50),
(@q1, 'Quel signe physique N''est PAS une manifestation du stress chronique ?',
 'Amélioration de la mémoire à long terme', 'Troubles du sommeil|Maux de tête fréquents|Problèmes digestifs', 100),
(@q1, 'La technique STOP en gestion du stress signifie :',
 'Stopper, Prendre du recul, Observer, Poursuivre consciemment',
 'Souffler, Tenir, Observer, Parler|Stimuler, Tolérer, Oublier, Partir|Stopper, Tendre, Oublier, Persister', 100),
(@q1, 'Quelle activité réduit le plus efficacement le taux de cortisol ?',
 'L''exercice physique modéré régulier', 'Le travail intense|Regarder des écrans|Sauter des repas', 50),
(@q1, 'Le stress aigu diffère du stress chronique car il est :',
 'Court, intense et potentiellement utile', 'Long et bénéfique|Inconscient et permanent|Toujours pathologique', 50),
(@q1, 'La cohérence cardiaque pratiquée 5 min/jour agit principalement sur :',
 'Le système nerveux autonome', 'Les muscles squelettiques|La glycémie|La température corporelle', 50),
(@q1, 'Parmi ces habitudes, laquelle aggrave le stress chronique ?',
 'La consommation excessive de caféine', 'La pratique régulière du yoga|Les promenades en nature|L''écriture d''un journal', 50);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 2 : Comprendre l'Anxiété
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Comprendre l''Anxiété', 5, 65, 'Argent', NOW(), @uid, 'disponible',
        'Identifiez les mécanismes de l''anxiété et découvrez les approches validées pour la réduire efficacement.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q2, 'L''anxiété se distingue de la peur car elle est orientée vers :',
 'Un danger futur incertain', 'Un danger présent et réel|Un souvenir douloureux|Une émotion passée', 50),
(@q2, 'Quelle thérapie est considérée comme la plus efficace contre l''anxiété ?',
 'La thérapie cognitivo-comportementale (TCC)', 'La psychanalyse|La sophrologie seule|L''hypnose seule', 100),
(@q2, 'Le trouble anxieux le plus répandu dans la population est :',
 'Le trouble anxieux généralisé (TAG)', 'La phobie sociale|Le trouble panique|L''agoraphobie', 50),
(@q2, 'La pensée catastrophiste consiste à :',
 'Anticiper le pire scénario possible de façon excessive',
 'Analyser les risques réels|Planifier des solutions|Se préparer à l''échec', 50),
(@q2, 'Quel symptôme physique accompagne souvent une crise d''anxiété ?',
 'Palpitations cardiaques et essoufflement', 'Hypersomnie|Augmentation de l''appétit|Ralentissement du pouls', 50),
(@q2, 'La pleine conscience (mindfulness) aide contre l''anxiété en :',
 'Ancrant l''attention sur le moment présent', 'Supprimant les pensées négatives|Évitant les situations stressantes|Accélérant la respiration', 50),
(@q2, 'L''exposition progressive est une technique qui consiste à :',
 'Confronter graduellement la situation anxiogène', 'Éviter complètement les déclencheurs|Prendre des médicaments|Distraire l''esprit', 100),
(@q2, 'Quelle affirmation sur l''anxiété est VRAIE ?',
 'Un niveau modéré d''anxiété peut améliorer les performances',
 'L''anxiété est toujours pathologique|L''anxiété ne se traite pas|L''anxiété n''a pas de composante physique', 100);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 3 : Qualité du Sommeil
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Qualité du Sommeil', 3, 50, 'Bronze', NOW(), @uid, 'disponible',
        'Évaluez vos connaissances sur le sommeil réparateur et les bonnes pratiques d''hygiène du sommeil.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q3, 'Quelle est la durée de sommeil recommandée pour un adulte ?',
 '7 à 9 heures', '4 à 5 heures|10 à 12 heures|5 à 6 heures', 50),
(@q3, 'La mélatonine est une hormone qui régule :',
 'Le rythme circadien et l''endormissement', 'L''appétit|La croissance musculaire|La température corporelle', 50),
(@q3, 'Pourquoi la lumière bleue des écrans perturbe-t-elle le sommeil ?',
 'Elle inhibe la sécrétion de mélatonine', 'Elle augmente la température|Elle stimule la faim|Elle accélère le rythme cardiaque', 100),
(@q3, 'Le trouble du sommeil caractérisé par des arrêts répétés de la respiration s''appelle :',
 'L''apnée du sommeil', 'L''insomnie primaire|La narcolepsie|Le somnambulisme', 50),
(@q3, 'Quelle température ambiante favorise le plus l''endormissement ?',
 'Entre 16 et 19 °C', 'Entre 24 et 27 °C|Entre 10 et 14 °C|Entre 20 et 23 °C', 50),
(@q3, 'Combien de cycles de sommeil (environ 90 min) réalise-t-on par nuit ?',
 '4 à 6 cycles', '1 à 2 cycles|7 à 9 cycles|10 cycles ou plus', 50),
(@q3, 'Quelle phase du sommeil est indispensable à la consolidation de la mémoire ?',
 'Le sommeil paradoxal (REM)', 'Le sommeil léger (N1)|Le sommeil lent profond uniquement|L''hypnagogique', 100),
(@q3, 'La sieste idéale pour recharger sans perturber le sommeil nocturne dure :',
 '10 à 20 minutes', '45 à 60 minutes|2 heures|5 minutes', 50);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 4 : Mémoire et Concentration
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Mémoire et Concentration', 3, 55, 'Argent', NOW(), @uid, 'disponible',
        'Testez vos connaissances sur le fonctionnement de la mémoire et les techniques pour booster votre concentration.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q4, 'Quelle région du cerveau joue le rôle central dans la formation de nouveaux souvenirs ?',
 'L''hippocampe', 'L''amygdale|Le cervelet|Le cortex préfrontal', 100),
(@q4, 'La technique mnémotechnique du "palais de mémoire" repose sur :',
 'L''association spatiale des informations', 'La répétition mécanique|Les cartes flash|L''écriture manuscrite', 100),
(@q4, 'Selon la loi de Miller, la mémoire à court terme peut stocker combien d''éléments ?',
 '7 ± 2 éléments', '3 ± 1 éléments|15 ± 5 éléments|20 éléments', 50),
(@q4, 'Quelle habitude améliore le plus la concentration sur le long terme ?',
 'L''exercice aérobique régulier', 'Boire plus de café|Travailler sans pause|Écouter de la musique forte', 50),
(@q4, 'La technique Pomodoro consiste à travailler par tranches de :',
 '25 minutes suivies de 5 minutes de pause', '45 minutes sans pause|10 minutes avec 10 min de pause|1 heure avec 1h de pause', 50),
(@q4, 'Quel neurotransmetteur est fortement associé à l''attention et à la vigilance ?',
 'La noradrénaline', 'La mélatonine|L''insuline|Le glucagon', 50),
(@q4, 'La répétition espacée est efficace car elle exploite :',
 'La courbe de l''oubli d''Ebbinghaus', 'La fatigue cognitive|Le sommeil paradoxal|L''effet de primauté', 100),
(@q4, 'Quelle activité stimule la neuroplasticité cérébrale ?',
 'Apprendre une nouvelle compétence ou langue', 'Regarder la télévision passivement|Répéter les mêmes tâches|Réduire les interactions sociales', 50);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 5 : Bien-être Mental au Quotidien
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Bien-être Mental au Quotidien', 2, 45, 'Bronze', NOW(), @uid, 'disponible',
        'Découvrez les piliers du bien-être psychologique et les habitudes positives pour une santé mentale durable.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q5, 'Selon le modèle PERMA de Seligman, la lettre R représente :',
 'Relationships (Relations positives)', 'Résilience|Récompense|Rationalité', 100),
(@q5, 'Quelle pratique quotidienne est la plus associée au bonheur durable ?',
 'La gratitude et les relations sociales positives', 'Accumuler des biens matériels|Éviter les émotions négatives|Comparer ses progrès aux autres', 50),
(@q5, 'L''auto-compassion consiste à :',
 'Se traiter avec bienveillance comme on traiterait un ami',
 'Se valoriser en se comparant aux autres|Ignorer ses émotions négatives|Ne jamais s''évaluer', 100),
(@q5, 'Quelle activité libère le plus de sérotonine naturellement ?',
 'L''exposition à la lumière solaire et l''exercice', 'Le travail nocturne|La privation de sommeil|L''isolement social', 50),
(@q5, 'Parmi ces éléments, lequel N''est PAS un pilier reconnu de la santé mentale ?',
 'La perfection dans toutes les tâches', 'Le sens et le but|Les relations de qualité|La capacité à gérer les émotions', 50),
(@q5, 'Le concept de "résilience" en psychologie désigne :',
 'La capacité à rebondir après une adversité', 'L''absence totale de souffrance|La résistance au changement|L''indifférence émotionnelle', 50),
(@q5, 'Quel effet a un acte altruiste (aider quelqu''un) sur le bien-être ?',
 'Il libère de la dopamine et améliore l''humeur', 'Il augmente le stress|Il n''a aucun effet prouvé|Il diminue l''énergie', 50),
(@q5, 'La psychologie positive s''intéresse principalement à :',
 'Les forces humaines et les facteurs d''épanouissement',
 'Les troubles mentaux graves|Les traumatismes uniquement|Les médicaments psychotropes', 50);

-- ───────────────────────────────────────────────────────────────
-- QUIZ 6 : Dépression et Résilience
-- ───────────────────────────────────────────────────────────────
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, date_quiz, utilisateur_id, statut, agilite_cognitive)
VALUES ('Dépression et Résilience', 7, 75, 'Or', NOW(), @uid, 'disponible',
        'Approfondissez vos connaissances sur la dépression, ses mécanismes biologiques et les voies vers la résilience.');
SET @q6 = LAST_INSERT_ID();

INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(@q6, 'Quel neurotransmetteur est principalement déficitaire dans la dépression ?',
 'La sérotonine', 'L''adrénaline|Le cortisol|Le glucagon', 50),
(@q6, 'La durée minimale des symptômes pour diagnostiquer un épisode dépressif majeur est :',
 '2 semaines consécutives', '24 heures|3 jours|6 mois', 100),
(@q6, 'Quel facteur N''est PAS reconnu comme facteur de risque de la dépression ?',
 'La pratique régulière d''exercice physique', 'L''isolement social|Les antécédents familiaux|Les événements de vie traumatisants', 100),
(@q6, 'L''anhédonie, symptôme clé de la dépression, désigne :',
 'L''incapacité à ressentir du plaisir', 'Une douleur physique intense|La perte de mémoire|L''hyperactivité', 50),
(@q6, 'Quelle approche thérapeutique combine médicaments et psychothérapie avec le meilleur taux de réussite ?',
 'Le traitement combiné (antidépresseurs + TCC)',
 'Les antidépresseurs seuls|La psychanalyse seule|Le repos total sans traitement', 100),
(@q6, 'La luminothérapie est particulièrement efficace contre :',
 'La dépression saisonnière (trouble affectif saisonnier)',
 'La dépression post-partum|La dépression bipolaire|La dysthymie chronique', 50),
(@q6, 'Quel comportement est un signal d''alarme nécessitant une aide professionnelle immédiate ?',
 'Des pensées récurrentes de mort ou de suicide',
 'Pleurer occasionnellement|Se sentir fatigué après une nuit courte|Avoir des moments de tristesse', 50),
(@q6, 'L''exercice physique agit comme antidépresseur naturel car il :',
 'Stimule la production de BDNF et d''endorphines',
 'Augmente le cortisol|Diminue la neuroplasticité|Ralentit le métabolisme', 50);
