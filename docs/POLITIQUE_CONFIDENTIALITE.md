# Politique de confidentialité — Épure Notes

> Brouillon à relire par Alain avant publication. Source : `docs/SPEC.md` § 8 et F-18.

Épure Notes est une application Android de prise de notes. Elle fonctionne entièrement sur ton téléphone.

## Quelles données sont traitées

- **Le texte de tes notes et ta liste de tâches** sont enregistrés dans le stockage privé de l'application, sur ton téléphone.
- **Tes réglages** (taille du texte, thème, effacement, sauvegarde) sont enregistrés sur ton téléphone.

## Ce que l'application ne fait pas

- Elle n'a **aucune permission** Android déclarée. Elle n'accède ni au réseau, ni aux contacts, ni à la localisation, ni au stockage partagé, ni au micro ou à la caméra.
- Elle n'envoie **aucune donnée** à Alain ni à un tiers.
- Elle ne contient **aucune publicité**, **aucun outil d'analyse** et **aucun identifiant** de suivi.
- Elle ne tient **aucun journal** du contenu de tes notes.

## Sauvegarde facultative

Par défaut, aucune sauvegarde n'est faite. Si tu choisis un dossier de sauvegarde dans les réglages, l'application y copie tes notes et tes tâches au format `.md`. Ce dossier est choisi par toi. Si tu le synchronises avec un service de stockage en ligne, c'est ce service qui envoie les fichiers, à ton initiative. L'application elle-même n'envoie rien.

Les sauvegardes Android (dans le cloud Google et lors d'un transfert vers un autre téléphone) sont désactivées pour l'application.

## Suppression des données

- Supprimer une note ou une tâche la supprime de l'application, et de la copie de sauvegarde si elle est activée.
- Désinstaller l'application efface les notes et les réglages stockés dans l'application. Les copies déjà présentes dans un dossier de sauvegarde choisi par toi ne sont pas effacées.

## Enfants

L'application ne collecte aucune donnée, quel que soit l'âge de l'utilisateur.

## Modifications

Si cette politique change, la nouvelle version sera publiée au même endroit, avec sa date.

Date de dernière mise à jour : 08/11/2026

Contact : via le dépôt GitHub de l'application, compte [AlainBeuchee](https://github.com/AlainBeuchee) (ouvrir une issue sur `epure-notes`).
