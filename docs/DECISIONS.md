# Journal des décisions — Épure Notes

> Les entrées ne sont jamais effacées. Une décision révisée reçoit une nouvelle entrée qui renvoie à l'ancienne.
> Statuts : **Décidée** · **Proposée** (à confirmer par Alain) · **[À DÉCIDER]** (options ouvertes) · **Remplacée par DEC-xxxx**.

## Modèle d'entrée

```
## DEC-xxxx — Titre court
- Date :
- Statut :
- Contexte :
- Options :
- Décision :
- Justification :
- Réversibilité :
- Références : SPEC.md F-x.y / T-n
```

---

## DEC-0001 — Nom et identifiants

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026
- Contexte : le brief indique « Épure PDF », `EpurePdf`, `com.alain.epure.pdf`, `epure-pdf`, qui sont les identifiants de l'app de lecture PDF. L'`applicationId` ne peut plus changer après une publication et doit rester stable dès le premier build.
- Options : reprendre les valeurs du brief (collision avec Épure PDF, impossible d'installer les deux apps) ; valeurs propres à l'app de notes.
- Décision : « Épure Notes », `EpureNotes`, `com.alain.epure.notes`, dépôt `epure-notes`.
- Justification : cohérence avec la famille Épure (A.2) ; deux apps distinctes doivent avoir deux identifiants.
- Réversibilité : facile avant le premier build ; ensuite, changer l'`applicationId` revient à créer une autre app (données perdues).
- Références : SPEC § 2.

## DEC-0002 — Licence

- Date : 28/10/2026
- Statut : Décidée
- Décision : GPL v3, fichier `LICENSE` dès le premier commit.
- Justification : brief ; cohérence avec Épure et Épure PDF.
- Réversibilité : difficile une fois des contributions externes acceptées.

## DEC-0003 — App distincte

- Date : 28/10/2026
- Statut : Décidée
- Décision : dépôt, identifiant et cycle de développement séparés du launcher Épure et d'Épure PDF ; aucune dépendance de code ; cohérence visuelle seulement (le thème est copié, pas partagé).
- Réversibilité : facile.

## DEC-0004 — Pile technique

- Date : 28/10/2026
- Statut : Décidée
- Décision : charte A.3 sans changement (Kotlin, Compose, Material 3, `minSdk` 31, `targetSdk` / `compileSdk` 36, une activité, `ViewModel` + `StateFlow`, `AppContainer`, DataStore Preferences, `domain/` sans dépendance Android). Pas de code natif, pas de Room tant que des fichiers texte suffisent.
- Réversibilité : moyenne.

## DEC-0005 — Aucune permission INTERNET

- Date : 28/10/2026
- Statut : Décidée
- Décision : aucune permission `INTERNET` ni aucune autre permission dans le manifeste fusionné. Vérification de l'APK à chaque ajout de dépendance ; une permission injectée est retirée par `tools:node="remove"` ou la bibliothèque est écartée.
- Justification : garantie de confidentialité vérifiable par quiconque.
- Réversibilité : techniquement facile, mais contraire à l'objet de l'app.

## DEC-0006 — Inspiration freewrite, aucun code repris

- Date : 28/10/2026
- Statut : Décidée
- Contexte : deux références. (1) freewrite de Farza (`github.com/farzaa/freewrite`) : app macOS en Swift / SwiftUI, sous licence MIT. (2) freewrite pour Android de Mohammed Farseen (`com.farseen.freewrite`) : aucun dépôt de code source public trouvé le 28/10/2026, licence inconnue.
- Décision : inspiration d'ergonomie seulement (écran d'écriture nu, barre basse discrète, réglages en panneau, désactivation de l'effacement). Aucun code, texte ou visuel repris. Le code Swift n'est de toute façon pas transposable.
- Mentions : README et « À propos » : « Interface inspirée de freewrite », sans logo ni suggestion d'affiliation.
- Prudence : « Freewrite » est aussi un nom de produit commercial (machines à écrire numériques) ; ne jamais l'employer dans le nom, le paquet ou une fiche de store. À vérifier avant toute publication.
- Réversibilité : —

## DEC-0007 — Lieu et format de stockage des notes

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 — option A (stockage interne), complétée par une sauvegarde optionnelle (DEC-0019)
- Contexte : le brief reprend la règle d'Épure PDF (« l'app ne copie pas les fichiers, elle conserve seulement leur URI »), qui n'a pas d'équivalent ici : les notes sont créées par l'app et doivent être stockées quelque part.
- Options :
  - A. Un fichier texte `.md` par note dans le stockage interne privé de l'app (`filesDir/notes/`). Aucune permission, aucun choix au premier lancement, le plus simple. Fichiers invisibles depuis « Mes fichiers » ; perdus à la désinstallation si `allowBackup` est à `false`.
  - B. Fichiers `.md` dans un dossier choisi par l'utilisateur (`ACTION_OPEN_DOCUMENT_TREE`, autorisation persistante). Aucune permission ; notes visibles et sauvegardables avec les outils de l'utilisateur. Mais un choix de dossier au premier lancement (contraire à « ouvrir et écrire tout de suite »), accès aux fichiers plus lent et plus complexe, risque de modification externe pendant l'édition.
  - C. Base Room. Recherche plus rapide sur de gros volumes, mais nouvelle dépendance, données non lisibles hors de l'app ; écarté par la charte tant que les données ne sont pas structurées.
- Proposition initiale : A pour les jalons 1 à 5 ; nom de fichier fondé sur la date de création (ex. `2026-10-28-163012.md`), titre jamais dans le nom.
- Décision (28/10/2026) : A par défaut. Le dossier choisi par l'utilisateur (B) n'est pas le lieu de travail des notes, mais une **copie de sauvegarde optionnelle** (DEC-0019).
- Réversibilité : A → B possible par une fonction d'export / migration.
- Références : SPEC F-3.5, § 8, DEC-0008.

## DEC-0008 — Sauvegarde Android (`allowBackup`)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 — sauvegarde système fermée ; sauvegarde par l'utilisateur en option (DEC-0019)
- Contexte : la charte A.3 fixe `allowBackup` à `false` « pour un outil qui ne stocke que des préférences locales ». Épure Notes stocke du contenu personnel ; la prémisse ne s'applique plus.
- Options :
  - A. `false` : rien ne quitte le téléphone ; notes perdues en cas de désinstallation, de réinitialisation ou de perte du téléphone ; seule protection, le partage manuel.
  - B. `true` avec règles d'extraction limitées aux notes et aux tâches (`dataExtractionRules`) : sauvegarde par le système Android (Google, et transfert d'appareil à appareil, ex. Smart Switch **[À VÉRIFIER]**), chiffrée de bout en bout si un verrouillage d'écran est défini. L'app ne demande toujours pas `INTERNET`, mais les données quittent le téléphone par le système.
  - C. `false` + export manuel de toutes les notes en un fichier (via le partage ou le sélecteur de fichiers).
- Proposition initiale : A au jalon 1 ; trancher entre B et C au jalon 2.
- Fait technique : `allowBackup` est un attribut **figé dans le manifeste** ; il ne peut pas devenir un réglage que l'utilisateur active. De plus, pour une app qui cible Android 12 ou plus, `allowBackup="false"` bloque la sauvegarde cloud et la sauvegarde ADB, mais **pas le transfert d'appareil à appareil**. Pour que rien ne sorte par le système, il faut aussi des règles `dataExtractionRules` qui excluent tout, pour `<cloud-backup>` comme pour `<device-transfer>`.
- Décision (28/10/2026) :
  - `android:allowBackup="false"` ;
  - `android:dataExtractionRules="@xml/data_extraction_rules"`, qui exclut tous les domaines pour `<cloud-backup>` et `<device-transfer>` ;
  - `android:fullBackupContent` de même pour Android 11 et moins : sans objet, puisque `minSdk` vaut 31.
  - Le besoin de transfert et de sauvegarde longue est couvert par une fonction de l'app, que l'utilisateur active lui-même (DEC-0019, option C élargie).
- Justification : « aucune donnée ne quitte le téléphone » reste vrai par défaut ; l'utilisateur choisit lui-même où va sa copie.
- **À reporter dans la charte** : la règle A.3 (`allowBackup` à `false`) ne suffit pas à elle seule sous Android 12 et plus. Le launcher Épure et Épure PDF sont concernés aussi : ajouter les mêmes `dataExtractionRules`, ou accepter le transfert d'appareil à appareil de leurs préférences.
- Réversibilité : facile.
- Références : SPEC T-13, F-18, § 8.

## DEC-0009 — Thème clair (dérogation à la charte A.5)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (Nuit par défaut, Jour en option) ; **valeurs des couleurs du thème Jour à valider sur l'appareil**
- Remarque : dans Épure PDF, le mode nuit inverse seulement les pages et l'interface reste noire. Dans Épure Notes, il n'y a pas de page : le texte est l'interface. Le thème Jour change donc le fond de tout l'écran (blanc) et pas seulement une page.
- Contexte : la charte A.5 impose le thème sombre seul. L'écriture en plein jour justifie un thème clair, proposé par freewrite (« Light Mode »).
- Décision : thème Nuit par défaut, thème Jour en option. Toujours sans couleur dynamique ni couleur d'accent ; trois jetons par thème.
- Proposition de jetons du thème Jour : fond `#FFFFFF`, texte principal `#000000`, texte secondaire `#5F5F5F` (le `#B0B0B0` du thème sombre serait illisible sur blanc). Contraste à vérifier (WCAG AA : au moins 4,5:1 pour le texte secondaire) **[À VÉRIFIER]**.
- Réversibilité : facile.
- Références : SPEC F-8.2.

## DEC-0010 — Police

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 : police système (DEC-0028)
- Contexte : le brief demande un rendu proche de freewrite, qui utilise Lato par défaut (captures). La charte A.5 prévoit la police système et exige une décision pour toute police tierce.
- Options :
  - A. Police système (Samsung One) : aucun fichier ajouté, rendu One UI, moins proche de freewrite.
  - B. Lato embarquée (`res/font/`, deux fichiers `.ttf`, quelques centaines de Ko), licence SIL Open Font License 1.1, disponible sur Google Fonts. Distribution avec une app GPL v3 : la police reste un fichier séparé sous sa propre licence (agrégat) ; copier `OFL.txt` dans le dépôt et la citer dans « À propos ».
  - C. Polices téléchargeables (Google Play services) : écartées, elles dépendent d'un service Google et d'un téléchargement.
- Proposition : B, si la différence de rendu est jugée utile sur l'appareil ; sinon A.
- Réversibilité : facile.
- Références : SPEC F-2.4.

## DEC-0011 — Aucune icône dans la barre de commandes

- Date : 28/10/2026
- Statut : Décidée sur le principe (charte A.5) ; **libellés [À DÉCIDER]**
- Contexte : freewrite utilise des icônes (étincelles, +, historique, chevron). La charte exclut les icônes ; l'exception des raccourcis d'Épure ne s'applique pas ici.
- Décision : libellés texte. Proposition : « Nouvelle », « Notes », « Tâches », « Réglages », puis « → Tâche » au jalon 5.
- Point de vigilance : 4 à 5 libellés de 20 sp sur la largeur d'un téléphone, avec la police système agrandie **[À VÉRIFIER]** ; à défaut, retour à la ligne de la barre ou libellés plus courts.
- Réversibilité : facile.

## DEC-0012 — Éléments du brief propres à un lecteur PDF

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 — appliquée dans le code jusqu'au jalon 6 (statut mis à jour d'après le code ; **à confirmer par Alain**)
- Contexte : le brief d'Épure Notes reprend plusieurs éléments d'Épure PDF (zoom, demi-page, indicateur « 12 / 48 », saut de page, inversion des couleurs des pages, intent `VIEW` `application/pdf`, URI persistantes, PdfRenderer / pdfium, « retirer de la liste sans supprimer le fichier »).
- Décision proposée : les écarter ou les remplacer selon le tableau SPEC § 5. En particulier : réception de texte partagé (`ACTION_SEND`, `text/plain`) à la place de l'ouverture de PDF ; taille du texte réglable à la place du zoom ; thème Nuit / Jour à la place de l'inversion.
- Justification : le double toucher et le pincement ont déjà un rôle dans un champ de texte (sélection) ; la notion de page n'existe pas dans une note.
- Réversibilité : facile tant que rien n'est codé.

## DEC-0013 — Ajouter une ligne d'une note aux tâches

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 — option A
- Contexte : le brief prévoit l'appui long sur une ligne. Dans un champ de texte Android, l'appui long sélectionne un mot et ouvre le menu Copier / Coller ; le détourner casse un comportement attendu.
- Options :
  - A. Action « → Tâche » dans la barre : ajoute la ligne du curseur. Simple, fiable, sans conflit de geste, conforme à « un libellé pour chaque action » (A.9).
  - B. Entrée « Ajouter aux tâches » dans le menu de sélection du système. Naturelle (appui long → sélection → menu), mais l'API Compose de menu de texte personnalisé est récente : faisabilité à vérifier selon la version de Compose au jalon 5 ; ajoute la sélection, pas forcément la ligne.
  - C. Remplacer l'appui long : écartée.
- Proposition : A ; B en complément si l'API le permet simplement.
- Décision (28/10/2026) : A, action « → Tâche » dans la barre de commandes. B reste une piste à réévaluer au jalon 5, sans engagement.
- Réversibilité : facile.
- Références : SPEC F-13.

## DEC-0014 — Format de la liste de tâches

- Date : 28/10/2026
- Statut : **Décidée** — option A, appliquée au jalon 5 (`taches.md` dans le stockage interne ; statut mis à jour d'après le code ; **à confirmer par Alain**)
- Options :
  - A. Fichier unique `taches.md`, une tâche par ligne : `- [ ] texte` / `- [x] texte`. Lisible hors de l'app, partageable tel quel, cohérent avec les notes.
  - B. DataStore (liste sérialisée). Plus simple à coder, illisible hors de l'app.
  - C. Room : surdimensionné.
- Proposition : A, lecture / écriture dans `domain/` avec tests.
- Réversibilité : facile.
- Références : SPEC F-12.7.

## DEC-0015 — Désactivation de l'effacement

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (DEC-0028) ; comportement des claviers **[À VÉRIFIER]**
- Contexte : freewrite propose « Backspace On / Off » pour écrire sans se corriger. Sur Android, le clavier virtuel n'envoie pas toujours une touche « Retour arrière » : il remplace souvent directement le texte (mot en cours de composition, correction automatique, balayage pour effacer).
- Décision proposée : filtrer les **modifications** du texte plutôt que les touches. Avec le champ de texte récent de Compose (`BasicTextField` avec `TextFieldState`), une `InputTransformation` refuse toute modification qui retire des caractères ; la règle « insertion pure ou non » est une fonction du `domain/`, testée. Quand l'effacement est désactivé, désactiver aussi la correction automatique (`KeyboardOptions`), sinon chaque correction serait refusée.
- Points à vérifier sur l'appareil : clavier Samsung (composition des mots, suggestions, balayage), Gboard si installé ; collage sur une sélection.
- Réversibilité : facile.
- Références : SPEC F-8.3.

## DEC-0016 — Tâches sur l'écran d'accueil

- Date : 28/10/2026
- Statut : **Décidée** — option B, appliquée au jalon 6 (`RemoteViews`, sans dépendance ; voir DEC-0037 ; statut mis à jour d'après le code ; **à confirmer par Alain**)
- Options :
  - A. Widget avec Jetpack Glance (bibliothèque AndroidX, Apache 2.0, compatible GPL v3) : affiche les tâches ; nouvelle dépendance à justifier.
  - B. Widget avec les `RemoteViews` classiques : aucune dépendance, code plus verbeux.
  - C. Raccourci d'app (appui long sur l'icône → « Tâches ») : aucune dépendance, très simple, mais n'affiche pas les tâches.
- Préalable **[À VÉRIFIER]** : le launcher Épure héberge-t-il des widgets et des raccourcis d'app ?
- Réversibilité : facile.
- Références : SPEC F-14.

## DEC-0017 — Fonctions de freewrite non retenues

- Date : 28/10/2026
- Statut : **Proposée**
- Décision proposée : pas de fonction d'IA (bouton étincelles de freewrite) — elle exigerait le réseau, contraire à DEC-0005 ; pas de minuteur d'écriture (« 15:00 ») ni de choix de police multiple (Lato / Serif / Mono / System), absents de la liste des fonctions du brief.
- Réversibilité : le minuteur pourrait être ajouté plus tard sans permission.

## DEC-0018 — Enregistrement automatique

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain, jalon 1)
- Décision proposée : enregistrement 1 s après la dernière frappe et en `onStop`, par `AtomicFile` (AndroidX Core, déjà présent dans le projet) ; aucune indication à l'écran ; une note vide n'est jamais écrite.
- Justification : pas de perte de texte, même après un arrêt du processus par le système ; pas d'écriture à chaque caractère.
- Réversibilité : facile.
- Références : SPEC F-3.

## DEC-0019 — Sauvegarde optionnelle dans un dossier choisi par l'utilisateur

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (principe et modalités)
- Contexte : par défaut, les notes ne sont que dans le stockage interne, sans sauvegarde système (DEC-0007, DEC-0008). Alain souhaite pouvoir, en option, transférer ses notes vers un nouveau téléphone et les conserver durablement.
- Options écartées :
  - activer la sauvegarde Android à la demande : impossible simplement, car `allowBackup` est figé dans le manifeste. Un agent de sauvegarde personnalisé qui lirait un réglage serait complexe, dépendrait du compte Google, et son comportement avec Smart Switch serait incertain ;
  - export manuel seul : couvre le transfert, mais pas la sauvegarde continue (il faut y penser).
- Décision :
  - Réglage « Sauvegarde » : **désactivé par défaut**. L'activer ouvre le sélecteur de dossier du système (`ACTION_OPEN_DOCUMENT_TREE`). L'utilisateur choisit un dossier, par exemple `Documents/Épure Notes`, ou un dossier synchronisé par l'outil de son choix. L'autorisation sur ce dossier est conservée (`takePersistableUriPermission`). Aucune permission n'est déclarée.
  - Quand la sauvegarde est active, chaque enregistrement d'une note ou des tâches est **recopié** dans ce dossier, sous forme de fichiers `.md` lisibles par n'importe quelle app.
  - « Restaurer depuis un dossier » (nouveau téléphone) : choisir le dossier, puis importer les notes et les tâches qu'il contient. Une note déjà présente n'est jamais écrasée.
  - Désactiver la sauvegarde libère l'autorisation sur le dossier et n'efface pas les copies.
  - Avertissement affiché sous le réglage, en italique et en `TextSecondary`, tant que la sauvegarde est désactivée : *« Tes notes sont enregistrées seulement sur ce téléphone. Elles seront perdues si tu désinstalles l'app ou si tu perds le téléphone. »* L'italique est une petite dérogation typographique à la charte A.5, à reporter dans la Partie B.
- Modalités (décidées le 28/10/2026) :
  - recopie à chaque enregistrement de la note (et non seulement en `onStop`) ;
  - fichiers inconnus déjà présents dans le dossier : ignorés, jamais modifiés ni supprimés.
- [À VÉRIFIER] : Smart Switch recopie-t-il le dossier `Documents` vers le nouveau téléphone ? Comportement si l'utilisateur supprime ou renomme le dossier hors de l'app : message « Dossier de sauvegarde introuvable. », sauvegarde suspendue, notes intactes.
- Confidentialité : si le dossier choisi est synchronisé par une app de cloud, les notes quittent le téléphone **par cette app**, à l'initiative de l'utilisateur ; Épure Notes ne demande toujours pas `INTERNET`. À écrire tel quel dans le README.
- Décision complémentaire (28/10/2026) : supprimer une note dans l'app supprime aussi sa copie dans le dossier de sauvegarde (« supprimée partout »). La copie reflète l'état de l'app ; ce n'est pas une archive. La confirmation de suppression (F-6.4) le précise.
- Réversibilité : facile.
- Références : SPEC F-18, § 7, § 8.

## DEC-0020 — `compileSdk` 37, `targetSdk` 36 (écart avec la charte A.3)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (option A, appliquée dans `app/build.gradle.kts`)
- Contexte : la charte A.3 fixe `compileSdk` et `targetSdk` à 36. Le modèle Empty Activity d'Android Studio (octobre 2026) installe `androidx.core` 1.19.1 et `androidx.lifecycle` 2.11.0, qui exigent `compileSdk` 37 (vérification « AAR metadata » au build).
- Options :
  - A. `compileSdk` 37, `targetSdk` 36. Le `compileSdk` sert seulement à la compilation (les API qu'on *peut* appeler) ; il ne change pas le comportement de l'app. Le `targetSdk`, qui active les nouveaux comportements d'Android, reste à 36, la version du téléphone de test. Aucune action supplémentaire ; on garde des bibliothèques à jour.
  - B. Tout à 36 et rétrograder les bibliothèques AndroidX vers des versions compatibles. Conforme à la lettre de la charte, mais versions plus anciennes à retrouver et à maintenir, et le problème reviendra à chaque mise à jour.
- Décision : A.
- Lint : l'avertissement `OldTargetApi` (« Not targeting the latest versions ») est désactivé dans `app/lint.xml`, tant que `targetSdk` reste à 36. Relever `targetSdk` à 37 reste possible plus tard, après test sur l'appareil.
- Garde-fou : ne pas appeler d'API propre à Android 17 sans vérification de version (`Build.VERSION.SDK_INT`) ; lint le signale (`NewApi`), puisque `minSdk` vaut 31.
- **À reporter dans la charte** (A.3) : `compileSdk` = dernière version requise par les bibliothèques AndroidX ; `targetSdk` = version du téléphone de test. Le launcher Épure et Épure PDF rencontreront le même cas à leur prochaine mise à jour de bibliothèques.
- Réversibilité : facile.
- Références : SPEC T-5.

## DEC-0021 — Choix de l'éditeur (jalon 1)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Contexte : plusieurs points de SPEC F-1 à F-2 étaient [À DÉCIDER] ; le jalon 1 demande un éditeur utilisable.
- Décision :
  - F-1.4 : le clavier ne s'ouvre pas tout seul à l'arrivée ; un toucher dans le texte l'ouvre.
  - F-1.3 : texte indicatif « Commence à écrire » en `TextSecondary` sur note vide, validé par Alain le 28/10/2026 (ajouté après la première version de DEC-0021).
  - F-8.1 : taille du texte fixée à 20 sp, valeur par défaut proposée (le réglage arrive au jalon 3).
  - F-2.3 : interligne par défaut de Material 3, provisoire, en attendant la décision sur la valeur.
  - F-3.1 : enregistrement 1 s après la dernière frappe, et à l'arrêt de l'app (DEC-0018).
- Justification : rendre l'éditeur testable sur l'appareil sans trancher à la place d'Alain les points de confort.
- Réversibilité : facile.
- Références : SPEC F-1.3, F-1.4, F-2.3, F-3.1, F-8.1.

## DEC-0022 — Gris secondaire du thème Nuit (révise DEC-0009)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (demandée par Alain après essai sur l'appareil)
- Contexte : DEC-0009 donne `TextSecondary` = `#B0B0B0` pour le thème Nuit. Sur l'appareil, le texte indicatif « Commence à écrire » paraissait trop clair.
- Décision : `TextSecondary` = `#999999`. Le jeton reste unique et sert à tous les textes secondaires du thème Nuit.
- Justification : assombrir un peu le gris tout en gardant un contraste d'environ 7,4:1 sur fond noir (au-dessus du seuil WCAG AA de 4,5:1 pour le texte).
- Réversibilité : facile (une seule valeur dans `Color.kt`).
- Références : DEC-0009, SPEC F-1.3, F-8.2.

## DEC-0023 — Choix du jalon 2 (liste, suppression, partage, réception)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision :
  - F-4.3 : titre dans la liste sur 2 lignes au plus, puis « … ».
  - F-6.4 : suppression seule, avec confirmation ; pas d'archivage.
  - F-10.1 : Épure Notes apparaît dans la feuille de partage pour du texte ; le texte reçu crée une nouvelle note. F-10.2 (ajout aux tâches) reste ouvert pour le jalon 5.
  - F-9.2 : partage du texte seulement, pas de fichier `.md` pour l'instant.
- Justification : choix simples, qui suivent les propositions de la spec, et qui n'ajoutent ni permission ni bibliothèque.
- Réversibilité : facile.
- Références : SPEC F-4.3, F-6.4, F-9.2, F-10.1.

## DEC-0024 — Forme de l'écran Réglages

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision : les réglages sont un écran « Liste de réglages » à part, ouvert par la commande « Réglages » de la barre. Pas de panneau dépliable.
- Justification : écran simple à lire et à tester sur le téléphone ; même principe que « Notes ».
- Réversibilité : facile.
- Références : SPEC §3, F-7.2, F-8.

## DEC-0025 — Retrait de la mention « Interface inspirée de freewrite » dans « À propos »

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (demandée par Alain)
- Contexte : DEC-0006 prévoit la mention « Interface inspirée de freewrite » dans le README et dans « À propos ».
- Décision : la mention est retirée de l'écran « À propos » et du README, sur confirmation d'Alain. DEC-0006 reste inchangée dans son texte ; cette décision la remplace pour ces deux endroits.
- Alignement : `SPEC.md` F-15 et `CHARTE.md` (tableau des marques et « À propos ») mis à jour sur décision d'Alain.
- Justification : demande explicite d'Alain.
- Réversibilité : facile.
- Références : DEC-0006, SPEC F-15.

## DEC-0026 — Bibliothèque DataStore Preferences (réglages)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Contexte : le réglage « Sauvegarde » (dossier choisi, F-18) et les autres réglages doivent être mémorisés. La charte A.3 prévoit DataStore Preferences pour les réglages (DEC-0004).
- Options :
  - A. `androidx.datastore:datastore-preferences` 1.2.1 (AndroidX, licence Apache 2.0, compatible GPL v3). Bibliothèque officielle, sans permission, asynchrone.
  - B. `SharedPreferences` (Android, sans bibliothèque). Rien à ajouter, mais s'écarte de la charte et du choix de DEC-0004.
- Décision : A, version 1.2.1 (stable, publiée avant la 1.3.0 du 21/10/2026 pour un peu de recul).
- Vérification : après ajout, `aapt2 dump permissions` ne doit lister que `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (SPEC T-1, T-2).
- Réversibilité : facile (réglages peu nombreux, à migrer si besoin).
- Références : DEC-0004, DEC-0019, SPEC F-8, F-18, § 7.

## DEC-0027 — Tailles du texte de l'éditeur : 12 à 24 sp

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (choix d'Alain)
- Contexte : F-8.1 prévoyait 16, 18, 20, 22, 24 sp. Sur l'appareil, Alain souhaite pouvoir descendre plus bas.
- Décision : sept paliers, 12, 14, 16, 18, 20, 22, 24 sp. Un toucher passe au palier suivant ; après 24 sp, retour à 12 sp. La valeur par défaut reste 20 sp (proposition de F-8.1, toujours [À DÉCIDER] pour la valeur par défaut).
- Justification : demande d'Alain pour un texte plus petit.
- Réversibilité : facile (liste de tailles dans `domain/TextSize.kt`).
- Références : SPEC F-8.1, CHARTE B (B.4 ou tableau des textes), DEC-0021.

## DEC-0028 — Choix du jalon 3 (police, effacement, thème Jour, options écartées)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision :
  - Police (DEC-0010) : police système, pas de fichier de police embarqué. Aucune licence à copier.
  - Effacement « Effacer : non » (DEC-0015) : approche proposée. Une modification qui retire des caractères est refusée ; la correction automatique est désactivée dans ce mode. Comportement du clavier Samsung à vérifier sur l'appareil.
  - Thème Jour (DEC-0009) : fond `#FFFFFF`, texte `#000000`, texte secondaire `#5F5F5F`. À valider sur l'appareil (contraste).
  - Écran allumé (F-16) et compteur de mots (F-17) : écartés en version 1.
- Justification : choix simples, qui limitent les réglages et évitent toute dépendance.
- Réversibilité : facile.
- Références : SPEC F-2.4, F-8.3, F-8.2, F-16, F-17 ; DEC-0009, DEC-0010, DEC-0015.

## DEC-0029 — Barre de commandes et mode immersif (jalon 3)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision :
  - F-7.4 : masquage automatique de la barre. Elle disparaît dès qu'on tape ; elle réapparaît au toucher du texte, à la fermeture du clavier, ou après 3 s sans frappe. Sans animation.
  - F-2.7 : barre d'état masquée pendant l'écriture (écran éditeur) ; la barre de navigation reste visible. À vérifier sur One UI.
- Justification : écrire sans distraction, tout en gardant les commandes à un geste.
- Réversibilité : facile.
- Références : SPEC F-2.7, F-7.4.

## DEC-0030 — Fermeture du jalon 2 avec un test reporté ; Gboard non applicable

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision :
  - Le jalon 2 est fermé. Le test du transfert Smart Switch (un autre téléphone recopie-t-il `Documents/Épure Notes` ?) est reporté au prochain changement de téléphone, et reste marqué [À VÉRIFIER] dans la roadmap et DEC-0019.
  - La sauvegarde Google est vérifiée : Android refuse la sauvegarde de l'app (`bmgr backupnow` : « Backup is not allowed »).
  - Le test Gboard du jalon 3 est non applicable : Gboard n'est pas installé.
- Justification : le test Smart Switch demande un second téléphone et ne bloque pas les fonctions suivantes. Décision d'Alain.
- Réversibilité : facile.
- Références : SPEC F-18, § 8 ; DEC-0019.

## DEC-0031 — Recherche dans la note (jalon 4)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision : pas de recherche à l'intérieur de la note en version 1 (F-11.5). La recherche ne sert qu'à trouver une note dans la liste (F-11.1 à F-11.4).
- Justification : choix simple, proposé par la spec.
- Réversibilité : facile.
- Références : SPEC F-11.5.

## DEC-0032 — Import d'un fichier .md partagé (F-10.4)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (demandée par Alain)
- Décision : Épure Notes apparaît dans la feuille de partage pour les fichiers `.md` (type `text/markdown`). Le contenu du fichier devient une nouvelle note. Le fichier d'origine n'est ni modifié ni supprimé.
- Justification : importer des notes écrites dans un autre éditeur, sans permission supplémentaire (l'accès est donné par le partage).
- Limite : les fichiers partagés avec un type générique (`application/octet-stream`) ne sont pas proposés ; un fichier `.md` avec un titre `# …` garde le `#` dans le titre affiché.
- Réversibilité : facile.
- Références : SPEC F-10.1, F-10.3, F-10.4.

## DEC-0033 — Choix du jalon 5 (liste de tâches)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision :
  - DEC-0014 : fichier `taches.md`, une tâche par ligne : `- [ ] texte` ou `- [x] texte`. Copié dans le dossier de sauvegarde quand elle est active (F-18.4).
  - F-12.3 : une tâche faite est grisée et descend en bas de la liste. Pas de texte barré.
  - F-13.5 : après « → Tâche », le libellé devient « Ajoutée » pendant 2 secondes, sans animation.
- Justification : choix proposés par la spec, retenus par Alain.
- Réversibilité : facile.
- Références : SPEC F-12, F-13 ; DEC-0014, DEC-0013.

## DEC-0034 — Barre de l'éditeur à quatre commandes (jalon 5)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (choix d'Alain)
- Décision : la barre de l'éditeur contient « Nouvelle », « Notes », « Tâches » et « → Tâche ». « Réglages » n'y figure plus ; il reste accessible depuis la page Notes.
- Justification : la barre à cinq commandes ne tient pas sur un téléphone avec la police agrandie (DEC-0011).
- Réversibilité : facile.
- Références : SPEC F-7.2, DEC-0011.

## DEC-0035 — Widget « Tâches » sur l'écran verrouillé (option)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (demandée par Alain)
- Décision : un second widget « Tâches », réservé à l'écran verrouillé, est proposé. Son contenu n'affiche les tâches que si l'option « Tâches sur l'écran verrouillé » est activée dans Réglages (désactivée par défaut). Sinon, un texte neutre s'affiche. Le widget de l'écran d'accueil montre toujours les tâches.
- Justification : confidentialité. Les textes des tâches ne doivent pas être visibles sans déverrouiller le téléphone, sauf choix explicite d'Alain.
- Constat (sur le téléphone d'Alain, One UI) : le widget verrouillé n'est pas proposé. Aucun widget tiers n'utilise la catégorie standard d'écran verrouillé ; One UI n'expose que ses propres catégories (non documentées). Décision d'Alain : garder le code, sans l'exposer. La ligne de Réglages reste, sans effet sur ce téléphone, jusqu'à décision contraire.
- Réversibilité : facile.
- Références : SPEC F-14, F-8 ; DEC-0016.

## DEC-0036 — « Ouvrir avec » pour un fichier .md (F-10.3)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision : Épure Notes apparaît dans « Ouvrir avec » pour les fichiers `.md`. Le contenu est copié dans une nouvelle note ; le fichier d'origine n'est ni modifié ni supprimé. Mention ajoutée dans « À propos ».
- Justification : même règle que le partage (F-10.4). Cela remplace l'exclusion de F-10.3 pour les seuls fichiers `.md`.
- Limite : le type reçu dépend du système ; à tester sur le téléphone.
- Réversibilité : facile.
- Références : SPEC F-10.3, F-10.4 ; DEC-0032.

## DEC-0037 — Widget et lanceur Épure (jalon 6)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (constat sur le téléphone d'Alain)
- Constat : le lanceur Épure Launcher, dans sa configuration minimaliste, n'affiche pas de widgets sur sa première page (liste de favoris, horloge, date, bouton des apps, trois icônes). Le widget « Tâches » fonctionne avec le lanceur One UI, et apparaît dans sa liste de widgets.
- Décision : le widget reste proposé à tous les lanceurs qui le supportent ; Épure Launcher ne l'affichera pas tant qu'il n'a pas de zone de widgets.
- Réversibilité : facile.
- Références : DEC-0016, SPEC F-14.4.

## DEC-0038 — Restauration de la liste de tâches (F-18.7)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision : « Restaurer depuis un dossier » importe `taches.md` seulement si la liste de tâches de l'app est vide. Sinon, la liste de l'app est conservée et le fichier du dossier est ignoré.
- Justification : ne jamais écraser ce que l'utilisateur a déjà, comme pour les notes (DEC-0019).
- Réversibilité : facile.
- Références : SPEC F-18.7 ; DEC-0019, DEC-0033.

## DEC-0039 — Libellés des barres en symboles (révise DEC-0011)

- Date : 28/10/2026
- Statut : **Décidée** le 28/10/2026 (validée par Alain)
- Décision : les barres du bas utilisent des symboles courts : « + » (Nouvelle note), « > tâche » (ajouter la ligne aux tâches), « ^ » (Réglages), « - tâches » (effacer les tâches faites). « Notes », « Tâches » et « Retour » restent en texte.
- Répartition :
  - éditeur : + | > tâche | Notes | Tâches | ^
  - Notes : + | Retour | Tâches | ^
  - Tâches : + | - tâches | Retour | Notes | ^
  - Réglages : Retour | Notes | Tâches
- Disposition : les actions (+, > tâche, - tâches) sont alignées à gauche ; la navigation (Retour, Notes, Tâches, ^) est alignée à droite.
- Accessibilité : chaque symbole a un nom complet lu par les lecteurs d'écran (SPEC F-7.5).
- Justification : barres lisibles avec la police agrandie, plus homogènes d'un écran à l'autre.
- Réversibilité : facile.
- Références : SPEC F-7.2, F-7.5 ; DEC-0011.

## DEC-0040 — « > » ajoute la sélection en note (hors jalon)

- Date : 30/10/2026
- Statut : **Décidée** le 30/10/2026 (demandée par Alain)
- Décision : le bouton « > » de l'éditeur a deux rôles selon la sélection.
  - Sans sélection (curseur dans une ligne) : ajoute la ligne aux tâches, comme avant (DEC-0013, DEC-0039).
  - Avec une sélection (texte sélectionné par appui long) : crée une nouvelle note dont le contenu est le texte sélectionné. Sa première ligne sert de titre (F-4.1). La note en cours n'est pas modifiée.
- Libellé : « > note » pendant la sélection, « > tâche » sinon.
- Justification : un seul bouton pour envoyer le texte utile ailleurs, sans ajouter de commande dans la barre (DEC-0034, barre à quatre commandes).
- Réversibilité : facile.
- Références : SPEC F-7.2, F-13 ; DEC-0013, DEC-0039. **À reporter dans SPEC.md** après validation sur le téléphone.

## DEC-0041 — Affichage des tâches : simple ou kanban (F-8, F-12, F-14)

- Date : 07/11/2026
- Statut : **Décidée** le 07/11/2026 (choix d'Alain). Points 2 à 7 : choix proposés par l'assistant, validés par Alain, y compris le menu de changement d'état ci-dessous.
- Contexte : Alain souhaite pouvoir suivre ses tâches en quatre états (à faire, en cours, bloquée, terminée) sans imposer ce mode à tout le monde. Le format actuel (`- [ ]` / `- [x]`, DEC-0014) reste la base.
- Décision :
  1. **Réglage** dans Réglages : « Tâches : simple / kanban ». Par défaut : simple.
  2. **Un seul format**, lu et écrit dans les deux modes, dans `taches.md` :
     - `- [ ]` à faire
     - `- [>]` en cours
     - `- [!]` bloquée
     - `- [x]` terminée
     Le fichier reste donc le même et la copie de sauvegarde (F-18.4) ne change pas. `[>]` et `[!]` ne sont pas des cases Markdown standard : elles restent lisibles en texte brut.
  3. **Mode simple** : `[>]` et `[!]` s'affichent comme « à faire » et ne sont pas modifiées tant que la tâche n'est pas cochée. Appui court : `[ ]` ou `[>]` ou `[!]` devient `[x]` ; `[x]` redevient `[ ]`. Le mode kanban n'est pas nécessaire pour retrouver l'état d'une tâche.
  4. **Mode kanban** : une liste unique, chaque tâche porte son libellé d'état (« À faire », « En cours », « Bloquée », « Terminée »). Les tâches sont triées dans cet ordre : à faire, en cours, bloquées, terminées en bas (même principe que F-12.3). Pas de filtres en version 1. Libellés regroupés : un en-tête par état, affiché une seule fois devant son groupe (non affiché si le groupe est vide).
  5. **Effacer les tâches faites** (F-12.5) supprime uniquement les `[x]`, dans les deux modes, après confirmation.
  6. **Widget** (F-14) : affiche toutes les tâches non terminées (à faire, en cours, bloquées), dans les deux modes.
  7. Changer de mode **ne modifie aucune tâche**.
  8. **Changement d'état en mode kanban** : appui long sur une tâche ouvre un menu en texte, séparé par un trait horizontal :
     - au-dessus du trait : « À faire », « En cours », « Bloquée », « Terminée » ;
     - en dessous du trait : « Modifier », « Supprimer » (F-12.4).
     En mode simple, l'appui long garde le menu actuel « Modifier », « Supprimer ».
- Justification : un seul format évite de convertir le fichier quand on change de mode ; le réglage ne touche que l'affichage ; aucune tâche n'est perdue ni modifiée.
- Réversibilité : facile (le fichier reste lisible ; les marqueurs `[>]` et `[!]` peuvent être remplacés par `[ ]`).
- Références : SPEC F-8 (Réglages), F-12.3, F-12.5, F-12.7, F-14 ; DEC-0014 ; DEC-0039.
- **À reporter dans SPEC.md** après validation sur le téléphone : réglage dans F-8, états et tri dans F-12, widget dans F-14.

## DEC-0042 — Libellé « Freewriting » du réglage d'effacement (révise F-8.3)

- Date : 07/11/2026
- Statut : **Décidée** le 07/11/2026 (choix d'Alain)
- Contexte : « Effacer : oui / non » n'était pas explicite. Le mode qui interdit de supprimer du texte est appelé « freewriting » dans le cahier des charges.
- Décision :
  - Libellé : « Freewriting : oui » (effacement interdit) ou « Freewriting : non » (effacement autorisé). Par défaut : « Freewriting : non ».
  - Mention en italique, 16 sp, sous le libellé : « Le texte ne peut pas être effacé. » si « oui » ; « Le texte peut être effacé. » si « non ».
  - La valeur stockée ne change pas (effacement autorisé, true par défaut, DataStore) : seul l'affichage est inversé.
- Justification : un mot plus explicite, déjà utilisé dans la documentation.
- Réversibilité : facile.
- Références : SPEC F-8.3 ; DEC-0015.
- **À reporter dans SPEC.md** après validation sur le téléphone : F-8.3.

## DEC-0043 — Clavier Samsung, tâches faites et widget (F-8.3, F-12.3, F-14.2)

- Date : 07/11/2026
- Statut : **Décidée** le 07/11/2026 (choix d'Alain)
- Décision :
  1. **Freewriting et clavier Samsung** : sur le téléphone, « Freewriting : oui » bloque le retour arrière, couper et la correction automatique. Point validé.
  2. **Tâches faites (F-12.3)** : une tâche faite passe en gris (`TextSecondary`) et descend en bas de la liste, sans case à cocher et sans texte barré. Point validé.
  3. **Widget (F-14.2)** : on ne coche pas une tâche depuis le widget. Un toucher sur le widget ouvre l'écran « Tâches ».
- Justification : choix d'Alain, après test sur le téléphone pour le point 1.
- Réversibilité : facile.
- Références : SPEC F-8.3, F-12.3, F-14.2 ; DEC-0015, DEC-0042.
- **À reporter dans SPEC.md** : F-8.3 (clavier vérifié), F-12.3 (plus de [À DÉCIDER]), F-14.2 (décidé).

## DEC-0044 — Espacement des tâches et des groupes en kanban (F-12.8)

- Date : 07/11/2026
- Statut : **Décidée** le 07/11/2026 (choix d'Alain, après test sur le téléphone)
- Contexte : les tâches étaient trop espacées ; les groupes du mode kanban se distinguaient mal.
- Décision :
  1. **Tâches** : ligne visible de 40 dp (8 dp de marge haut et bas). La zone de toucher reste à 48 dp et déborde de 4 dp en haut et en bas (charte, cible tactile).
  2. **Groupes en kanban** : 32 dp au-dessus de chaque en-tête (16 dp avant). Les tâches d'un même groupe restent serrées.
  3. **Trait vertical** à gauche des groupes : testé, non retenu.
- Justification : choix d'Alain après comparaison sur le téléphone.
- Réversibilité : facile (valeurs de marge).
- Références : SPEC F-12.8 ; DEC-0041 ; charte A.5 (cibles tactiles 48 dp).

## DEC-0045 — Ajout de tâche depuis la barre de l'écran « Tâches » (F-7.2, F-12.2)

- Date : 08/11/2026
- Statut : **Décidée** le 08/11/2026 (demandée par Alain), **validée sur le téléphone** le 08/11/2026. SPEC F-7.2 et F-12.2 reportés le même jour.
- Contexte : sur l'écran « Tâches », le « + » de la barre créait une nouvelle note, et un champ « Ajouter une tâche » restait fixe en haut de la liste.
- Décision :
  1. Sur l'écran « Tâches », « + » ouvre le champ d'ajout de tâche (et non plus une note). Le champ n'est visible qu'après cet appui, avec le clavier ouvert.
  2. Le libellé fixe « Ajouter une tâche » en haut de la liste est supprimé. Son nom reste pour les lecteurs d'écran (« Ajouter une tâche »).
  3. Entrée valide la tâche et garde le champ ouvert (F-12.2). Le retour ferme d'abord le champ, puis revient à l'éditeur (T-8).
  4. Sur les écrans « Éditeur » et « Notes », « + » crée toujours une nouvelle note.
- Justification : la barre devient cohérente avec l'écran ; le libellé fixe ne servait plus.
- Réversibilité : facile.
- Références : SPEC F-7.2, F-12.2, T-8 ; DEC-0039.

## DEC-0047 — Appui sur une tâche ferme le clavier (précise DEC-0045)

- Date : 08/11/2026
- Statut : **Décidée** le 08/11/2026 (demandée par Alain), **validée sur le téléphone** le 08/11/2026.
- Décision : un appui court sur une tâche (qui la marque faite ou à faire) ferme le clavier du champ d'ajout. Le champ reste ouvert, vide ou non.
- Justification : le clavier ne doit pas rester affiché quand on touche la liste.
- Réversibilité : facile.
- Références : SPEC F-12.2, F-12.3 ; DEC-0045.

## DEC-0049 — Optimisation R8 désactivée en release (corrige un plantage au démarrage)

- Date : 08/11/2026
- Statut : **Décidée** le 08/11/2026 (choix proposé par l'assistant), **validée sur le téléphone** le 08/11/2026 par la version 1.0 (2) de test interne. Le code de version passe de 1 à 2, car la version 1 est déjà enregistrée dans la console.
- Contexte : la version publiée en test interne (version 1) plantait au démarrage sur le téléphone, avec `IllegalAccessError` sur `kotlin.sequences` (journal `adb logcat`). Le build de debug fonctionnait.
- Décision : dans `app/build.gradle.kts`, l'optimisation R8 de la version release est désactivée (`enable = false`), et la restriction `packageScope` est retirée. Le build release est plus gros mais se comporte comme le debug.
- Justification : solution simple, sûre. Une version qui plante vaut pire qu'un APK plus gros. Une optimisation réactivée pourra être réétudiée plus tard, avec des règles de conservation testées.
- Réversibilité : facile (une ligne).
- Références : ROADMAP « Avant une éventuelle publication » ; DEC-0020.

## DEC-0048 — « + » réouvre le clavier (précise DEC-0045, DEC-0047)

- Date : 08/11/2026
- Statut : **Décidée** le 08/11/2026 (demandée par Alain), **validée sur le téléphone** le 08/11/2026.
- Décision : un appui sur « + » donne le focus au champ d'ajout et réaffiche le clavier, même si le champ est déjà ouvert (par exemple après un appui sur une tâche, DEC-0047).
- Justification : « + » doit toujours permettre d'écrire tout de suite.
- Réversibilité : facile.
- Références : SPEC F-12.2 ; DEC-0045, DEC-0047.

## DEC-0046 — Libellé « - » pour effacer les tâches faites (révise DEC-0039)

- Date : 08/11/2026
- Statut : **Décidée** le 08/11/2026 (demandée par Alain), **validée sur le téléphone** le 08/11/2026.
- Décision : sur l'écran « Tâches », le libellé de la barre passe de « - tâches » à « - ». L'action ne change pas : efface les tâches faites, après confirmation (F-12.5). Le nom lu par les lecteurs d'écran reste « Effacer les tâches faites ».
- Justification : avec « + » qui ajoute une tâche, « - » est le geste opposé et se comprend seul.
- Réversibilité : facile (une valeur de chaîne).
- Références : SPEC F-7.2, F-12.5 ; DEC-0039, DEC-0045.
