# Submission Code Server

Ce projet gitlab.inria.fr est un des composants de la solution plus globale [StopCovid](https://gitlab.inria.fr/stopcovid19/accueil/-/blob/master/README.md).

Ce composant propose les services suivants :
* Service Generate : permet la generation de code UUIDv4 (destine à laboratoires, ecc.)  or code Alphanumerique
* Service Verify : permet la verification de code insere par l utilisateur que declare sa positivite au Covid-19

Pendant la generation dans le cas des code UUIDv4 sont genere plusieurs fiches CVS un pour chaque jour.
En suite les fiches CSV sont zippé et envoiés via connexion SFTP.


Pendant les deploiment on a plusieurs variables d'environnement à setter :

* SUBMISSION_CODE_SERVER_DB_PWD password pour access au database;
* SUBMISSION_CODE_SERVER_DB_URL url database;
* SUBMISSION_CODE_SERVER_DB_USER user que utilise le database;
* SUBMISSION_CODE_SERVER_SFTP_TRANSFER activation transfert SFTP (true or false: default true);
* SUBMISSION_CODE_SERVER_SFTP_KEY chemin de la cle prive de l'user;
* SUBMISSION_CODE_SERVER_SFTP_USER user que fait le transfert SFTP du fie zip;
* SUBMISSION_CODE_SERVER_SFTP_PORT port où on ouvre la connexion SFTP;
* SUBMISSION_CODE_SERVER_SFTP_PASSPHRASE passphrase lie à cle prive;
* SUBMISSION_CODE_SERVER_SFTP_PATH dossier du SFTP où les archives générées sont stockées.