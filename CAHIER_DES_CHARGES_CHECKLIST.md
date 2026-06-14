## Améliorations proposées et orientation du projet

En plus des exigences du cahier des charges initial, le projet ChezVous doit respecter une orientation claire : créer une application réaliste, fluide, utile et proche des applications modernes disponibles sur le marché.

L’objectif n’est pas d’ajouter beaucoup d’écrans, d’icônes ou de fonctionnalités inutiles, mais de construire un parcours utilisateur simple, cohérent et fonctionnel.

### Philosophie du projet : “الموجز النافع”

Le développement doit suivre le principe du **code utile, simple et maintenable**.

Cela signifie :

* éviter le code lourd ou trop abstrait sans nécessité ;
* éviter les fonctionnalités décoratives qui ne servent pas le parcours utilisateur ;
* privilégier les fonctionnalités bien terminées plutôt que beaucoup de fonctionnalités incomplètes ;
* garder une architecture claire et facile à expliquer pendant la soutenance ;
* écrire un code propre, lisible et organisé par fonctionnalités.

### Expérience utilisateur attendue

L’application doit donner l’impression d’une vraie application de commande de repas.

Le design doit être :

* moderne mais simple ;
* fluide dans la navigation ;
* clair pour l’utilisateur ;
* adapté à une utilisation quotidienne ;
* sans surcharge d’icônes ou d’éléments visuels inutiles.

Les écrans doivent suivre un parcours logique :

1. authentification ou accès utilisateur ;
2. accueil avec restaurants disponibles ;
3. détails d’un restaurant ;
4. liste des plats ;
5. panier ;
6. paiement ;
7. confirmation de commande ;
8. suivi de livraison en temps réel.

### Design system

Un design system simple sera utilisé afin de garder une cohérence visuelle dans toute l’application.

Il doit contenir :

* une palette de couleurs cohérente ;
* une typographie lisible ;
* des boutons réutilisables ;
* des cartes restaurant/plat réutilisables ;
* des espacements réguliers ;
* des états visuels clairs : chargement, erreur, vide, succès.

Le design ne doit pas dépendre uniquement des icônes. Les informations importantes doivent être visibles à travers le texte, la hiérarchie visuelle et la disposition des éléments.

### Fonctionnalités prioritaires

Les fonctionnalités principales à finaliser sont :

* inscription et connexion classique ;
* connexion avec Google ;
* affichage des restaurants ;
* affichage des plats par restaurant ;
* ajout et suppression des plats dans le panier ;
* calcul du total de la commande ;
* validation de commande ;
* paiement en ligne en mode test ou simulation sécurisée ;
* suivi de commande avec changement d’état ;
* gestion simple des restaurants partenaires ;
* interface multilingue.

### Paiement en ligne

Le paiement peut être implémenté sous forme de simulation réaliste si l’intégration d’un vrai service de paiement est trop complexe pour le délai du projet.

Le parcours doit rester crédible :

* choix du moyen de paiement ;
* saisie ou simulation des informations de paiement ;
* validation ;
* message de succès ou d’échec ;
* génération d’une commande.

L’objectif est de montrer la logique fonctionnelle du paiement, même si le paiement réel n’est pas activé en production.

### Suivi en temps réel

Le suivi de commande doit permettre à l’utilisateur de voir l’évolution de sa commande.

Exemple d’états :

* commande confirmée ;
* préparation en cours ;
* commande prête ;
* livreur en route ;
* commande livrée.

Pour rester simple et efficace, le temps réel peut être géré avec Firebase Firestore, Supabase Realtime ou une simulation contrôlée dans l’application.

### Gestion des restaurants partenaires

Une partie simple de gestion doit exister pour montrer le rôle des restaurants partenaires.

Elle peut permettre de :

* consulter les commandes reçues ;
* changer l’état d’une commande ;
* voir les plats liés au restaurant.

Cette partie peut être limitée mais doit être claire pendant la démonstration.

### Internationalisation

L’application doit prévoir une interface multilingue.

Les langues recommandées sont :

* français ;
* anglais ;
* arabe.

Les textes importants de l’interface ne doivent pas être écrits directement dans le code. Ils doivent être placés dans les fichiers de ressources afin de respecter le principe d’internationalisation.

### Qualité technique attendue

Le projet doit respecter une architecture claire de type MVVM.

Organisation recommandée :

* `data` : modèles, sources de données, repository ;
* `domain` si nécessaire : logique métier simple ;
* `presentation` : écrans, ViewModels, états UI ;
* `ui` : thème, composants réutilisables ;
* `navigation` : gestion des routes ;
* `di` : injection de dépendances avec Hilt si utilisée.

Le code doit éviter :

* les écrans trop longs ;
* la duplication ;
* les composants inutiles ;
* les dépendances non nécessaires ;
* les animations lourdes ;
* les fonctionnalités non terminées.

### États d’interface à gérer

Pour rendre l’application plus professionnelle, chaque écran important doit gérer :

* l’état de chargement ;
* l’état vide ;
* l’état d’erreur ;
* l’état succès.

Exemples :

* aucun restaurant disponible ;
* panier vide ;
* erreur de connexion ;
* paiement refusé ;
* commande confirmée.

### Critères de réussite du projet

Le projet sera considéré comme réussi si :

* l’application compile sans erreur ;
* le parcours utilisateur principal fonctionne de bout en bout ;
* l’utilisateur peut choisir un restaurant, ajouter des plats, passer une commande et suivre son état ;
* l’interface est propre, cohérente et proche d’une vraie application mobile ;
* le code est simple, organisé et explicable ;
* le README explique clairement le projet, l’architecture, les fonctionnalités et les captures d’écran ;
* la démonstration peut être réalisée en moins de 10 minutes.

### Limites assumées

Certaines fonctionnalités peuvent être présentées en mode test ou simulation réaliste, comme le paiement en ligne ou le suivi de livraison.

Cependant, ces simulations doivent être propres, compréhensibles et intégrées dans un vrai parcours utilisateur. Elles ne doivent pas donner l’impression d’être des écrans décoratifs sans logique.
