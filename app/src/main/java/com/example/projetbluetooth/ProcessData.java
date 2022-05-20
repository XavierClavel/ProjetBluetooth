package com.example.projetbluetooth;

/*Cette classe a deux utilités principales :
1) Elle sert de conteneur pour stocker toutes les données relatives à un processus.
    Ce conteneur est utilisé par le serveur lors de l'initialisation de l'affichage de ses processus.
    A chaque ajout d'un RelativeLayout au LinearLayout, un conteneur est créé et ajouté à une liste.
    Ceci permet ensuite de récupérer les données et de les envoyer au client une fois que la connexion est initalisée.
2) Elle contient des méthodes permettant de formater les données pour envoyer des messages du serveur vers le client,
    dans le cas de l'initialisation de l'affichage des données côté client et de la mise à jour de ces données pour le monitoring du RSS.
 */
public class ProcessData {
    String processName;
    String uid;
    String RSS;

    static String separator = "|";
    static String endSeparator = "||";
    //Le string endSeparator est utilisé car il arrive que plusieurs messages soient envoyés en même temps, dans le même string.
    //Cette séquence permet de séparer les messages et de les traiter individuellement, sans quoi il y aurait une perte de données.

    public ProcessData(String processName, String uid, String RSS) {    //constructeur
        this.processName = processName;
        this.uid = uid;
        this.RSS = RSS;
    }

    public String FormatDataForInitializing()
    {
        return "data" + separator + processName + separator + uid + separator + RSS + endSeparator;
        //permet de formater les données d'un processus au format |processName|uid|RSS
        //utilisé par le serveur lors de l'initialisation de la connexion pour transmettre les données des processus
    }

    public static String StringFormatDataForUpdate(String processName, String uid, String RSS)
    {
        return "update" + separator + processName + separator + uid + separator + RSS + endSeparator;
        //permet de formater les données d'un processus au format update|processName|uid|RSS||
        //utilisé par le serveur pour mettre à jour les données du client
    }
}
