# Submission Code Server

Ce projet gitlab.inria.fr est un des composants de la solution plus globale [StopCovid](https://gitlab.inria.fr/stopcovid19/accueil/-/blob/master/README.md).

Ce composant propose les services suivants :

- génération de codes courts et longs : pour les professionnels de santé (laboratoires, médecins...)
- vérification et consommation de codes par la partie de la plateforme StopCovid

## Quickstart

Démarrer une base postgres en local :

    docker run -d --name db-scs -p 5432:5432 -e POSTGRES_PASSWORD=1234 -e POSTGRES_DB=dev-submission-code-server-schema postgres:13

L'application peut être démarrée depuis l'IDE.
