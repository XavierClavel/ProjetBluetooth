# ProjetBluetooth

Projet réalisé en java avec Android Studio. L'objectif était de réliser une apk permettant la transmission de données via bluetooth entre un appareil client et un appareil serveur. Le serveur affiche la liste des applications installées, ainsi que leur identifiant (UID) et leur consommation en énergie (RSS), et envoie ces données au client. Le client dispose de boutons permettant d'activer un monitoring sur une application, auquel cas l'appareil envoît de manière périodique des requêtes, auxquelles le serveur va répondre pour mettre à jour la valeur du RSS sur les deux appareils.

Il est nécessaire que les deux appareils aient été appairés via Bluetooth au préalable.
