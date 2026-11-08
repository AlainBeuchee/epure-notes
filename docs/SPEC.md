# Cahier des charges — Épure Notes

> Version 0.3 du 7 novembre 2026. Statut : **brouillon à valider**.
> Documents associés : `ROADMAP.md` (jalons), `DECISIONS.md` (décisions `DEC-xxxx`), `CHARTE.md` (charte graphique et fonctionnelle, Partie A commune aux projets d'Alain).
> En cas d'écart entre ce document et le code, l'écart est signalé et tranché ; ce document n'est pas modifié pour s'aligner sur le code.
> **[À DÉCIDER]** : point non tranché, à consigner dans `DECISIONS.md` avant toute implémentation.
> **[À VÉRIFIER]** : point à tester sur l'appareil (Samsung, Android 16 / One UI 8.0) ; résultat consigné dans `DECISIONS.md` ou `ROADMAP.md`.

---

## 1. Objet

Épure Notes est une application Android de prise de notes **minimaliste et entièrement hors ligne** : écrire sans distraction, sans réseau, sans collecte de données.

Usages visés :

- prise de notes rapide (ouvrir l'app, écrire tout de suite) ;
- écriture libre (« freewriting ») : texte continu, sans mise en forme, avec possibilité de désactiver l'effacement ;
- extraction d'une ligne d'une note vers une liste de tâches simple.

Usage personnel en priorité ; code public sur GitHub (GPL v3) ; publication sur le Play Store éventuelle, non prioritaire.

Référence ergonomique : l'app Android « freewrite » (Mohammed Farseen), elle-même inspirée de l'app macOS freewrite de Farza (`github.com/farzaa/freewrite`). Inspiration d'interface seulement, aucun code repris (DEC-0006).

## 2. Identité (DEC-0001, décidée)

| Élément | Valeur |
|---|---|
| Nom affiché | « Épure Notes » |
| Nom technique | `EpureNotes` |
| `applicationId` | `com.alain.epure.notes` |
| Dépôt Git | `epure-notes` |
| Licence | GPL v3 (DEC-0002) |
| Relation avec Épure et Épure PDF | App distincte : dépôt, identifiant et cycle séparés ; aucune dépendance ; cohérence visuelle seulement (DEC-0003) |

> Le brief du 28/10/2026 indiquait « Épure PDF », `EpurePdf`, `com.alain.epure.pdf` et `epure-pdf` : ce sont les valeurs de l'app de lecture PDF, reprises par erreur. Les valeurs ci-dessus ont été validées le 28/10/2026 ; l'`applicationId` ne change plus après le premier build.

Avant une publication sur le Play Store : recherche d'homonymes, vérification INPI / EUIPO (classe 9, logiciels), nom affiché distinctif.

## 3. Écrans

| Écran | Rôle | Accès | Jalon |
|---|---|---|---|
| **Éditeur** | Écran principal : la note en cours, plein écran ; barre de commandes en bas | Lancement de l'app | 1 |
| **Notes** | Liste des notes, de la plus récente à la plus ancienne ; recherche (jalon 4) | Commande « Notes » | 2 |
| **Tâches** | Liste de tâches unique | Commande « Tâches » ; widget | 5 |
| **Réglages** | Sauvegarde et restauration ; puis taille du texte, thème, effacement ; « À propos » | Commande « Réglages » | 2 (sauvegarde, « À propos »), 3 (autres réglages) |
| **À propos** | Version, licence, dépôt, confidentialité, crédits | Réglages | 2 |
| Menu d'une note | « Partager », « Supprimer » | Appui long sur une note de la liste | 2 |
| Confirmation de suppression | Fenêtre de menu (charte A.6) | « Supprimer » | 2 |

- Forme des réglages : écran « Liste de réglages » (charte A.6) ou panneau dépliable au-dessus de la barre de commandes, comme dans freewrite : **[À DÉCIDER]**. Proposition : panneau dépliable (3 réglages, pas d'écran supplémentaire).
- Pas d'écran d'autorisation : l'app ne demande aucune permission (§ 7).
- Navigation : état simple géré à la main (charte A.3), pas de `navigation-compose`.

## 4. Exigences fonctionnelles

### F-1 — Démarrage et reprise (jalon 1)

- **F-1.1** Au lancement, l'app ouvre directement l'éditeur sur la **dernière note modifiée**, curseur à la fin du texte.
- **F-1.2** Au tout premier lancement, ou s'il n'existe aucune note, l'éditeur s'ouvre sur une note vide.
- **F-1.3** Une note vide affiche un texte indicatif en `TextSecondary`. Libellé : **[À DÉCIDER]**, proposition « Commence à écrire ».
- **F-1.4** Ouverture automatique du clavier à l'arrivée : **[À DÉCIDER]**. Proposition : clavier ouvert si la note est vide ; fermé sinon (on relit d'abord), un toucher dans le texte l'ouvre.
- **F-1.5** Après un arrêt de l'app par le système (mémoire, redémarrage), la reprise affiche le texte tel qu'il était à la dernière frappe enregistrée (F-3).

*Critères d'acceptation* : lancer l'app → la dernière note s'affiche sans écran intermédiaire ; forcer l'arrêt dans les réglages Android puis relancer → même note, même texte.

### F-2 — Écriture (jalon 1)

- **F-2.1** Zone de texte plein écran, texte brut (pas de gras, titres, listes ni Markdown rendu).
- **F-2.2** Défilement vertical continu ; le curseur reste visible au-dessus du clavier (gestion des `WindowInsets.ime`).
- **F-2.3** Marges latérales : 24 dp (charte A.5). Interligne : environ 1,5 fois la taille du texte, comme freewrite (**[À DÉCIDER]**, la charte prévoit 1,3 à 1,4).
- **F-2.4** Police : **[À DÉCIDER]** (DEC-0010). Proposition : Lato embarquée, pour un rendu proche de freewrite ; à défaut, police système.
- **F-2.5** Taille du texte réglable (F-8.1), en `sp`.
- **F-2.6** Correcteur et suggestions du clavier : actifs par défaut ; désactivés quand l'effacement est désactivé (F-8.3, DEC-0015).
- **F-2.7** Le mode immersif (barres système masquées) : **[À DÉCIDER]**. Proposition : barre d'état masquée pendant l'écriture, barre de navigation conservée (le clavier en a besoin sur One UI) — **[À VÉRIFIER]**.
- **F-2.8** Pas d'annulation / rétablissement dédiés en version 1 (le clavier Samsung en propose parfois) : **[À DÉCIDER]**.

*Critères* : écrire 3 écrans de texte → le défilement suit le curseur ; le texte n'est jamais masqué par le clavier ; rotation de l'écran sans perte de texte ni de position du curseur.

### F-3 — Enregistrement automatique (jalon 1)

- **F-3.1** Le texte est enregistré **sans action de l'utilisateur** : peu après chaque pause de frappe (proposition : 1 s) et à chaque passage de l'app en arrière-plan (`onStop`).
- **F-3.2** Écriture atomique (fichier temporaire puis remplacement, `AtomicFile` d'AndroidX Core) : une coupure pendant l'enregistrement ne corrompt jamais la note.
- **F-3.3** Aucune indication « enregistré » à l'écran (pas d'indicateur animé, charte A.6).
- **F-3.4** Une note **vide** (ou ne contenant que des espaces) n'est jamais conservée : quitter une note vide la supprime. (Évite les « Empty entry » qui s'accumulent dans freewrite.)
- **F-3.5** Lieu et format de stockage (DEC-0007, décidée) : un fichier texte `.md` par note, dans le stockage interne privé de l'app. Copie optionnelle dans un dossier choisi par l'utilisateur : F-18.

*Critères* : écrire une phrase puis appuyer immédiatement sur Accueil → relancer : la phrase est là. Écrire, fermer l'app depuis les apps récentes → texte conservé. Ouvrir « + », ne rien écrire, revenir → aucune note vide dans la liste.

### F-4 — Titre d'une note (jalons 1-2)

- **F-4.1** Le titre est la **première ligne non vide** du texte, sans les espaces de début et de fin. Il n'y a pas de champ titre séparé.
- **F-4.2** Le titre n'est pas stocké : il est recalculé à partir du texte (fonction du paquet `domain/`, testée).
- **F-4.3** Affichage d'un titre très long dans la liste : la charte demande un passage à la ligne sans troncature ; une première ligne peut être un paragraphe entier. **[À DÉCIDER]** : proposition, 2 lignes au plus puis « … » (dérogation à consigner).

### F-5 — Nouvelle note (jalon 1)

- **F-5.1** Commande « Nouvelle » dans la barre : enregistre la note en cours et ouvre une note vide.
- **F-5.2** Si la note en cours est déjà vide, la commande ne crée rien de plus.

### F-6 — Liste des notes (jalon 2)

- **F-6.1** Écran « Notes » : titre de l'écran, puis une ligne par note, de la plus récemment modifiée à la plus ancienne.
- **F-6.2** Chaque ligne : titre (F-4) en `TextPrimary`, seconde ligne en `TextSecondary` avec la date de dernière modification (proposition : « 16:30 » si aujourd'hui, « 28 oct. » cette année, « 28 oct. 2025 » sinon).
- **F-6.3** Appui court : ouvre la note dans l'éditeur. Appui long : menu « Partager », « Supprimer ».
- **F-6.4** « Supprimer » supprime la note **et son fichier** ; action irréversible → confirmation (charte A.9) : « Supprimer cette note ? » / « Annuler » / « Supprimer ».
  > Le brief demandait « supprimer de la liste sans supprimer le fichier » (reprise d'Épure PDF, où les fichiers appartiennent à l'utilisateur). Ici, la note n'existe que dans l'app : la retirer de la liste sans la supprimer n'a de sens que si l'on ajoute un archivage. **[À DÉCIDER]** : suppression seule (proposition) ou suppression + archivage.
- **F-6.5** Supprimer la note ouverte dans l'éditeur : l'éditeur passe à la note la plus récente restante, ou à une note vide.
- **F-6.6** Liste vide : « Aucune note » en `TextSecondary`.
- **F-6.7** Retour : revient à l'éditeur.

*Critères* : 30 notes de test → défilement fluide, ordre correct après modification d'une ancienne note ; suppression confirmée → fichier absent ; annulation → rien ne change.

### F-7 — Barre de commandes (jalons 1 et 3)

- **F-7.1** Barre en bas de l'éditeur, au-dessus du clavier quand il est ouvert ; fond `Black` (ou fond du thème clair), filet de 1 dp en haut, **texte seul, sans icône** (charte A.5, DEC-0011).
- **F-7.2** Actions de la barre (DEC-0039) : « + » (Nouvelle note ; sur l'écran « Tâches », ajout de tâche, DEC-0045), « > tâche » (ajouter la ligne du curseur aux tâches ; devient « > note » quand du texte est sélectionné, DEC-0040), « ^ » (Réglages), « - » (effacer les tâches faites ; libellé réduit à « - » sur l'écran « Tâches », DEC-0046), « Notes », « Tâches », « Retour ». Répartition par écran dans DEC-0039.
- **F-7.3** Jalon 1 : barre toujours visible (prototype).
- **F-7.4** Jalon 3 — masquage automatique : la barre disparaît dès que l'on tape ; elle réapparaît au toucher du texte, à la fermeture du clavier, ou après une pause de frappe (proposition : 3 s, **[À DÉCIDER]**). Apparition et disparition immédiates, sans animation.
  > Un toucher dans le texte sert aussi à placer le curseur : on ne peut pas réserver le toucher simple à l'affichage des commandes comme dans un lecteur. Comportement à valider sur l'appareil **[À VÉRIFIER]**.
- **F-7.5** Chaque action est une cible tactile d'au moins 48 dp, avec un libellé lisible par TalkBack.

### F-8 — Réglages (jalon 3)

Réglages limités au strict minimum, mémorisés dans DataStore (Preferences) :

- **F-8.1** **Taille du texte** : 12, 14, 16, 18, 20, 22, 24 sp (DEC-0027). Un toucher sur le réglage passe à la valeur suivante (comme « 16px » dans freewrite). Valeur par défaut : 14 sp (DEC-0050). (Le brief parle de « px » : en Android, le texte se mesure en `sp`, qui suit aussi le réglage de taille de police du téléphone.)
- **F-8.2** **Thème** : « Nuit » (par défaut) ou « Jour » (DEC-0009, décidée). Dérogation à la charte A.5 (thème sombre uniquement). Le thème Jour s'applique à tout l'écran : fond blanc, texte noir. Couleurs exactes à valider sur l'appareil.
- **F-8.3** **Freewriting** (DEC-0042) : « Freewriting : non » (par défaut) / « Freewriting : oui ». Sous le libellé, en italique 16 sp : « Le texte peut être effacé. » si « non », « Le texte ne peut pas être effacé. » si « oui ». Quand le freewriting est activé, toute suppression de texte est refusée (touche Retour arrière, couper, remplacer une sélection, correction automatique). Mise en œuvre : DEC-0015 ; comportement du clavier Samsung vérifié sur le téléphone (DEC-0043).
- **F-8.4** « À propos » (F-15).
- **F-8.5** Écran allumé pendant l'écriture : **[À DÉCIDER]** (F-16).
- **F-8.6** « Sauvegarde » et « Restaurer depuis un dossier » (F-18), avec l'avertissement en italique tant que la sauvegarde est désactivée. Disponibles dès le jalon 2, avant les autres réglages.
- **F-8.7** **Tâches** : « Tâches : simple » (par défaut) / « Tâches : kanban » (DEC-0041). Ne change que l'affichage de la liste (F-12.8).

*Critères* : chaque réglage s'applique immédiatement et survit à un redémarrage ; avec « Effacer : non », la touche Retour arrière, « Couper » et une correction automatique ne retirent aucun caractère.

### F-9 — Partage (jalon 2)

- **F-9.1** « Partager » (menu d'une note) ouvre la feuille de partage Android (`ACTION_SEND`, `text/plain`) avec le texte de la note ; le titre (F-4) est passé en sujet (`EXTRA_SUBJECT`).
- **F-9.2** Partage sous forme de fichier `.md` (pour l'enregistrer dans « Mes fichiers » ou un cloud choisi par l'utilisateur) : **[À DÉCIDER]** ; nécessiterait un `FileProvider` (sans permission).
- **F-9.3** Aucun réseau propre à l'app : c'est l'app choisie par l'utilisateur qui envoie.

### F-10 — Réception depuis d'autres apps (jalon 2)

Le brief prévoyait l'ouverture de PDF depuis d'autres apps (intent `VIEW`, `application/pdf`), sans objet ici. Proposition **[À DÉCIDER]** :

- **F-10.1** Épure Notes apparaît dans la feuille de partage pour du **texte** (`ACTION_SEND`, `text/plain`) : le texte reçu crée une nouvelle note (le sujet éventuel en première ligne).
- **F-10.2** Option : le texte reçu peut aussi être ajouté aux tâches (jalon 5) **[À DÉCIDER]**.
- **F-10.3** Ouverture d'un fichier `.md` externe (`VIEW`, « Ouvrir avec ») : le contenu est copié dans une nouvelle note, le fichier d'origine n'est ni modifié ni supprimé (DEC-0036). Les autres fichiers (`.txt`…) ne sont pas proposés.
- **F-10.4** Import d'un fichier `.md` partagé depuis une autre app, par exemple « Mes fichiers » : le contenu devient une nouvelle note, comme pour le texte reçu (F-10.1). Le fichier d'origine n'est ni modifié ni supprimé (DEC-0032).

### F-11 — Recherche (jalon 4)

- **F-11.1** Champ « Rechercher » en haut de l'écran « Notes » (composant « Champ de recherche » de la charte A.6), sans loupe.
- **F-11.2** Filtre la liste aux notes dont le texte contient la recherche, sans tenir compte des majuscules ni des accents (« eleve » trouve « élève ») — fonction du `domain/`, testée.
- **F-11.3** Ouvrir un résultat place le curseur sur la première occurrence et la sélectionne (la sélection système sert de signalement : pas besoin de couleur d'accent).
- **F-11.4** Aucun résultat : « Aucun résultat ».
- **F-11.5** Recherche à l'intérieur de la note ouverte (occurrence suivante / précédente) : **[À DÉCIDER]**, proposition : non en version 1.

### F-12 — Liste de tâches (jalon 5)

- **F-12.1** **Une seule** liste de tâches, écran « Tâches ».
- **F-12.2** Le « + » de la barre ouvre un champ d'ajout de tâche, sans libellé fixe en haut de la liste (DEC-0045) ; le champ n'apparaît qu'après cet appui. Entrée valide la tâche et garde le champ ouvert.
- **F-12.3** Appui court sur une tâche : la marque faite / à faire. Une tâche faite passe en `TextSecondary` et en bas de la liste (pas de case à cocher dessinée ni de texte barré, DEC-0043).
- **F-12.4** Appui long : menu « Modifier », « Supprimer ». En mode kanban (F-12.8), le menu commence par les quatre états « À faire », « En cours », « Bloquée », « Terminée », séparés par un trait.
- **F-12.5** Action « Effacer les tâches faites » (irréversible → confirmation). Efface uniquement les tâches terminées, dans les deux modes.
- **F-12.6** Liste vide : « Aucune tâche ».
- **F-12.7** Stockage (DEC-0014, DEC-0041) : un fichier `taches.md`, une tâche par ligne : `- [ ] …` à faire, `- [>] …` en cours, `- [!] …` bloquée, `- [x] …` terminée. Format unique pour les deux modes ; lisible tel quel s'il est partagé. `[>]` et `[!]` ne sont pas des cases Markdown standard.
- **F-12.8** Mode kanban (DEC-0041) : liste unique, regroupée par état dans l'ordre à faire, en cours, bloquée, terminée. Un en-tête affiché une seule fois devant chaque groupe ; un groupe vide n'a pas d'en-tête. Mode simple : les tâches « en cours » et « bloquées » s'affichent comme « à faire », sans changer d'état. Changer de mode ne modifie aucune tâche. Entre deux groupes, un espace de 32 dp sépare les en-têtes ; les tâches d'un même groupe restent serrées (DEC-0044).

### F-13 — Ajouter une ligne d'une note aux tâches (jalon 5)

- **F-13.1** But : depuis l'éditeur, envoyer la ligne courante vers la liste de tâches sans quitter la note.
- **F-13.2** Action « → Tâche » dans la barre de commandes : ajoute la ligne où se trouve le curseur (DEC-0013, décidée). L'appui long reste au système (sélection, copier-coller). Une entrée « Ajouter aux tâches » dans le menu de sélection du système pourra être réévaluée au jalon 5, sans engagement.
- **F-13.3** Texte ajouté : la ligne, sans espaces de début ni de fin, sans puce initiale (`-`, `*`, `•`). Une ligne vide n'ajoute rien.
- **F-13.4** La note n'est pas modifiée.
- **F-13.6** Avec une sélection de texte (appui long), le bouton « > note » crée une nouvelle note dont le contenu est le texte sélectionné ; sa première ligne sert de titre (F-4.1). La note en cours n'est pas modifiée (DEC-0040).
- **F-13.5** Retour visuel : **[À DÉCIDER]**. Proposition : le libellé de l'action devient « Ajoutée » pendant 2 s, sans animation.

### F-14 — Tâches sur l'écran d'accueil (jalon 6, conditionnel)

- **F-14.1** Widget affichant les tâches non terminées (à faire, en cours, bloquées), dans les deux modes, texte seul, fond noir ; toucher → ouvre l'écran « Tâches ».
- **F-14.2** Pas de cochage depuis le widget (DEC-0043) : un toucher ouvre l'écran « Tâches ».
- **F-14.3** Nécessite probablement la bibliothèque Jetpack Glance (DEC-0016, à justifier au jalon 6) ; vérifier qu'elle n'ajoute aucune permission.
- **F-14.4** **[À VÉRIFIER]** : le launcher Épure accueille-t-il des widgets ? Si non, le widget ne sera visible qu'avec le launcher One UI ; alternative possible : raccourci d'app (appui long sur l'icône → « Tâches »), qui ne demande aucune bibliothèque.

### F-15 — À propos (jalon 2)

Version, licence GPL v3, lien vers le dépôt (ouvert par le navigateur), confidentialité (§ 8), licence de la police si une police est embarquée (SIL OFL 1.1 pour Lato).

### F-18 — Sauvegarde et restauration optionnelles (jalon 2)

Décisions : DEC-0007, DEC-0008, DEC-0019.

- **F-18.1** Par défaut, **aucune sauvegarde** : les notes ne sont que dans le stockage interne, et la sauvegarde système Android est fermée (y compris le transfert d'appareil à appareil).
- **F-18.2** Tant que la sauvegarde est désactivée, le réglage affiche en italique, en `TextSecondary` : *« Tes notes sont enregistrées seulement sur ce téléphone. Elles seront perdues si tu désinstalles l'app ou si tu perds le téléphone. »* Le même avertissement figure dans « À propos » et dans le README.
- **F-18.3** « Sauvegarde : non » → toucher → sélecteur de dossier du système → dossier choisi → « Sauvegarde : oui », avec le nom du dossier en seconde ligne. Annuler le sélecteur ne change rien.
- **F-18.4** Sauvegarde active : chaque note et la liste de tâches sont recopiées dans le dossier, en fichiers `.md` portant le même nom que dans l'app, à chaque enregistrement (DEC-0019, décidée). Les fichiers du dossier qui ne viennent pas de l'app sont ignorés : jamais modifiés ni supprimés.
- **F-18.5** Activer la sauvegarde recopie immédiatement toutes les notes existantes.
- **F-18.6** Suppression d'une note dans l'app : sa copie dans le dossier de sauvegarde est **supprimée aussi** (DEC-0019, décidée). Si la sauvegarde est active, la confirmation l'indique : « Supprimer cette note ? Elle sera aussi supprimée du dossier de sauvegarde. » Si le dossier est inaccessible au moment de la suppression, la note est supprimée dans l'app et la copie restante est supprimée à la prochaine recopie complète (F-18.5) **[À VÉRIFIER]**.
- **F-18.7** « Restaurer depuis un dossier » : sélecteur de dossier, puis import des fichiers `.md` de notes et de `taches.md`. Une note déjà présente n'est jamais écrasée. Message final : « 12 notes importées. » (nombre réel). Aucun fichier valide : « Aucune note trouvée dans ce dossier. »
- **F-18.8** Dossier devenu inaccessible (supprimé, renommé, carte retirée) : message « Dossier de sauvegarde introuvable. » au prochain enregistrement, sauvegarde suspendue, notes de l'app intactes, réglage repassé à « Sauvegarde : non » avec l'avertissement.
- **F-18.9** « Sauvegarde : oui » → toucher → « Arrêter la sauvegarde ? » / « Annuler » / « Arrêter » : libère l'autorisation sur le dossier ; les copies existantes ne sont pas effacées.

*Critères* : activer la sauvegarde → les fichiers apparaissent dans « Mes fichiers » ; modifier une note → la copie est à jour ; désinstaller, réinstaller, restaurer → toutes les notes reviennent ; avec la sauvegarde désactivée, aucune donnée de l'app n'apparaît dans une sauvegarde Google ni dans un transfert Smart Switch **[À VÉRIFIER]**.

### F-16 — Écran allumé (conditionnel)

Le brief prévoyait « maintien de l'écran allumé pendant la lecture ». Pour l'écriture, l'écran reste allumé tant que l'on tape. **[À DÉCIDER]** : écarter (proposition, conforme au « minimum de réglages ») ou option limitée à l'éditeur (`FLAG_KEEP_SCREEN_ON`, sans permission).

### F-17 — Compteur de mots (conditionnel)

Le brief prévoyait un indicateur « 12 / 48 » et un saut de page, propres à un PDF. Équivalent possible : nombre de mots discret dans la barre de commandes. **[À DÉCIDER]**, proposition : non en version 1.

## 5. Éléments du brief sans objet pour une app de notes (DEC-0012)

| Élément du brief | Origine | Proposition |
|---|---|---|
| Nom, identifiant, dépôt « PDF » | Épure PDF | Remplacer (§ 2) |
| `ACTION_OPEN_DOCUMENT`, intent `VIEW` `application/pdf`, URI persistantes | Épure PDF | Remplacer par F-10 (réception de texte) |
| PdfRenderer, pdfium | Épure PDF | Sans objet |
| Zoom par pincement et double toucher, demi-page | Lecteur | Écarter ; le double toucher sélectionne un mot dans un champ de texte. La taille se règle dans F-8.1 |
| Indicateur « 12 / 48 », saut de page | Lecteur | Écarter ou remplacer par F-17 |
| Mode nuit par inversion des couleurs des pages | Lecteur | Remplacer par le thème Nuit / Jour (F-8.2) |
| « Retirer de la liste sans supprimer le fichier » | Lecteur | Voir F-6.4 |
| Jeu de PDF de test | Lecteur | Jeu de notes de test fictives, hors du dépôt |

## 6. Exigences transversales

- **T-1 Aucun réseau** : pas de permission `INTERNET` dans le manifeste fusionné, vérifié à chaque ajout de dépendance (DEC-0005).
- **T-2 Aucune permission** déclarée (§ 7).
- **T-3 Aucun service** en arrière-plan, aucune notification, aucune tâche planifiée.
- **T-4 Aucune perte de données** : écriture atomique, enregistrement en `onStop`, test d'arrêt forcé à chaque jalon.
- **T-5 Pile technique** : charte A.3 (Kotlin, Compose, Material 3, `minSdk` 31, `targetSdk` / `compileSdk` 36, une activité, `ViewModel` + `StateFlow`, `AppContainer`, DataStore). Aucune bibliothèque ajoutée sans entrée dans `DECISIONS.md`.
- **T-6 Logique pure testée** dans `domain/` : titre (F-4), filtrage de recherche (F-11.2), règle d'effacement (F-8.3), nettoyage d'une ligne en tâche (F-13.3), lecture / écriture du format des tâches (F-12.7), tri et format de date (F-6.2).
- **T-7 Contraintes Android 16** : edge-to-edge, `WindowInsets` (barres système et clavier), retour prédictif avec `BackHandler`.
- **T-8 Retour** : ferme d'abord le panneau ou la fenêtre ouverte, puis masque le clavier, puis : depuis « Notes » ou « Tâches », revient à l'éditeur ; depuis l'éditeur, quitte l'app (comportement standard).
- **T-9 Textes** : tous dans `strings.xml`, en français, tutoiement, ton neutre (charte A.7).
- **T-10 Accessibilité** : tailles en `sp`, cibles ≥ 48 dp, libellés TalkBack, utilisable avec la police système agrandie (charte A.8).
- **T-11 Sobriété** : pas d'animation, pas d'icône, pas de couleur d'accent (charte A.5).
- **T-12 Volume** : une note de 50 000 caractères reste fluide à la frappe **[À VÉRIFIER]** ; au-delà, comportement à mesurer.
- **T-13 Sauvegarde Android** (DEC-0008, décidée) : `allowBackup="false"` **et** `dataExtractionRules` excluant tout pour `<cloud-backup>` et `<device-transfer>`. Sous Android 12 et plus, `allowBackup="false"` seul n'empêche pas le transfert d'appareil à appareil.

## 7. Permissions

| Permission / accès | Fonction | Obtention |
|---|---|---|
| Stockage interne de l'app | Notes, tâches | Aucune permission |
| DataStore | Réglages, note en cours | Aucune permission |
| Feuille de partage (`ACTION_SEND`, émission) | Partager une note | Aucune permission |
| Filtre d'intent `ACTION_SEND` `text/plain` (réception) | Créer une note depuis une autre app | Aucune permission |
| Autorisation persistante sur un dossier (`ACTION_OPEN_DOCUMENT_TREE`) | Sauvegarde optionnelle, restauration (F-18) | Choix de l'utilisateur dans le sélecteur ; libérée quand la sauvegarde est arrêtée |
| `FLAG_KEEP_SCREEN_ON` (si F-16 retenue) | Écran allumé | Aucune permission |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `MANAGE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `POST_NOTIFICATIONS`, `WAKE_LOCK`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, `RECEIVE_BOOT_COMPLETED` | — | **Non déclarées** |

Objectif : **aucune permission** dans le manifeste fusionné.

## 8. Données stockées et confidentialité

| Donnée | Lieu (proposition) | Effacée quand |
|---|---|---|
| Texte des notes | Un fichier `.md` par note, stockage interne de l'app | Suppression de la note ; désinstallation |
| Tâches | `taches.md`, stockage interne | Suppression de la tâche ; désinstallation |
| Réglages (taille, thème, effacement) | DataStore | Désinstallation |
| Identifiant de la dernière note ouverte | DataStore | Désinstallation |
| Adresse (URI) du dossier de sauvegarde, si F-18 activée | DataStore | Arrêt de la sauvegarde ; désinstallation |
| Copies `.md` des notes et des tâches, si F-18 activée | Dossier choisi par l'utilisateur | Suppression de la note dans l'app (F-18.6) ; par l'utilisateur ; jamais par la désinstallation |

Garanties :

- Par défaut, aucune donnée ne quitte le téléphone, ni par l'app, ni par la sauvegarde système ; aucune statistique, aucun journal d'usage.
- Pas de permission `INTERNET` : garantie vérifiable dans le manifeste.
- **Conséquence à connaître** : sans sauvegarde activée, les notes sont **perdues à la désinstallation ou en cas de perte du téléphone** ; l'app le dit (F-18.2).
- Sauvegarde activée : les copies vont uniquement dans le dossier choisi par l'utilisateur. Si ce dossier est synchronisé par une app de cloud, c'est cette app qui envoie les fichiers, à l'initiative de l'utilisateur.

## 9. Hors périmètre (version 1)

Mise en forme (gras, titres, Markdown rendu), images, pièces jointes, dossiers et étiquettes, rappels et dates d'échéance, synchronisation cloud, chiffrement propre à l'app, verrouillage par code, dictée propre à l'app (celle du clavier reste disponible), minuteur d'écriture et fonctions d'IA de freewrite (DEC-0017).

## 10. Points ouverts — récapitulatif

| Réf. | Sujet | Décision liée |
|---|---|---|
| F-1.3, F-1.4 | Texte indicatif ; clavier à l'ouverture | — |
| F-2.3, F-2.4 | Interligne ; police | DEC-0010 |
| F-2.7, F-2.8 | Mode immersif ; annuler / rétablir | — |
| F-4.3 | Longueur du titre dans la liste | — |
| F-6.4 | Suppression ou archivage | — |
| F-7.2, F-7.4 | Libellés ; masquage automatique | DEC-0011 |
| F-8.1 | Taille par défaut | — |
| F-8.3 | Désactivation de l'effacement (freewriting) | DEC-0015, DEC-0042 |
| F-9.2 | Partage en fichier | — |
| F-10 | Réception de texte | DEC-0012 |
| F-11.5 | Recherche dans la note | — |
| F-12.3, F-12.7 | Rendu des tâches faites ; format | DEC-0014 |
| F-14 | Widget | DEC-0016 |
| F-16, F-17 | Écran allumé ; compteur de mots | — |

Tranchés le 28/10/2026 : identité (DEC-0001), stockage (DEC-0007), sauvegarde système (DEC-0008), thème Nuit / Jour (DEC-0009), ligne → tâche (DEC-0013), sauvegarde optionnelle (DEC-0019), y compris la suppression de la copie avec la note (F-18.6).

## Historique

- 0.1 (28/10/2026) : première version, à partir du brief du 28/10/2026, des captures de freewrite (Android) et de `CHARTE.md` (Partie A 1.0).
- 0.2 (28/10/2026) : décisions DEC-0007, 0008, 0009, 0013 et 0019 ; ajout de F-18 (sauvegarde et restauration optionnelles).
- 0.3 (07/11/2026) : mode simple ou kanban des tâches (DEC-0041) ; F-8.7, F-12.4, F-12.5, F-12.7, F-12.8 et F-14.1 mis à jour.
- 0.3 (07/11/2026, suite) : F-8.3 renommé « Freewriting » avec mention en italique (DEC-0042) ; F-8.3, F-12.3 et F-14.2 précisés (DEC-0043).
- 0.3 (08/11/2026) : « + » de l'écran « Tâches » ouvre l'ajout de tâche (DEC-0045) ; F-7.2 et F-12.2 mis à jour.
