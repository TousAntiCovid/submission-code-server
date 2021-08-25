# Description du fonctionnement de l'outil de génération des codes longs

## Cinématique

Pour générer 300 000 codes un opérateur réalisera l'opération suivante :  
Il ne peut générer plus de 40 000 codes d'un coup (limitation due au code).  
Il va donc découper ces appels et réaliser (7 _ 40 000) + (1 _ 20 000) = 300 000.

Les appels se font via une requête POST sur la route `/api/v1/back-office/codes/generate/request`.  
En passant en paramètre les informations suivantes dans le Body :

```
{
    "from":"2021-07-01T00:00:00.000Z",
    "to":"2021-07-09T23:59:59.000Z",
    "dailyAmount":40000
}
```
from : correspond à la date du début de validité des codes.  
to : correspond à la fin de validité des codes et doit être postérieure à la date from.    

L'opérateur effectuera donc 7 appels avec le paramètre dailyAmount à 40000 et un appel avec le paramètre dailyAmount à 20000.  
Tous les appels auront les mêmes paramères from et to qui correspondent au début et à la fin de validité des codes.

Chaque appel donne lieu à la création de deux fichiers (voir § ci-dessous).

Une exécution du script est finie quand ce message s'affiche dans les logs.
```
INFO fr.gouv.stopc.submission.code.server.ws.service.impl.FileServiceImpl [task-{}] It took {} seconds to generate {} codes
```

## Fichiers de sortie

### Fichiers

Deux fichiers par appels sont générés :

- Un fichier `.tgz` contenant la liste des codes
- Un fichier de contrôle `.sha256` (somme de contrôle) permettant de contrôler l'intégrité du fichier .tgz

### Nomenclature

Les noms des fichiers sont générés de la manière suivante :  
Pour le premier fichier, le nom est composé de la date de génération au format `yyyyMMddHHmmss` (en zone "Europe/Paris") concaténé avec `%s_stopcovid_qrcode_batch.tgz` ce qui donne pour mon exemple  
`20210819092922_stopcovid_qrcode_batch.tgz`  
La nomenclature est la même pour le second fichier seule l'extention diffère `.sha256`.  
`20210819092922_stopcovid_qrcode_batch.sha256`

### Contenu

#### Premier fichier (yyyyMMddHHmmss_stopcovid_qrcode_batch.tgz)

Le fichier contenant la liste des codes est une archive `tgz` contenant une seconde archive `tar` contenant un `csv` qui est la liste des codes.  
La nomenclature du nom du fichier csv est la suivante `(séquence) + AAMMdd.csv ==> 25210825.csv`  
La séquence correspond à un incrément lié à un triptyque temporel (AAMMdd) pour éviter de générer deux fichiers de même nom.  
Voici un exemple avec un seul code généré

| code_pour_qr  |   code_brut   |           validite_debut |             validite_fin |
| ------------- | :-----------: | -----------------------: | -----------------------: |
| ...41 char... | ...36 char... | 2021-08-25T22:00:00.000Z | 2021-09-02T21:59:00.000Z |

Le séparateur de ce csv est une virgule `,` et il y a un retour chariot à la fin du fichier (LF).

#### Second fichier (yyyyMMddHHmmss_stopcovid_qrcode_batch.sha256)

Ce fichier est un fichier plat contenant une empreinte de 65 caractères du fichier tgz.  
Ce fichier ne contient pas de retour chariot à la fin du fichier.

## Gestion des différents cas fonctionnels (changement du nb de code, reprise/rejeux …)

### Changement du nombre de codes

S'il faut générer plus ou moins de codes, il faut réaliser plus ou moins de requêtes en réalisant le calcul suivant.

Exemple :  
Je veux générer `500 000` codes :  
`500 000` / 40 000 = 12,5 ==> 12 appels de 40 000 codes  
`500 000` mod 40 000 = 20 000 ==> 1 appel de 20 000 codes

### Reprise / Rejeux

Dans le cas où un problème serait survenu, il faut invalider les codes générés ou lister les codes non sauvegardés en base.  
Pour ce faire, il faut réaliser des requêtes en base de données ou sur le serveur sftp.

```
# Utile pour repérer s'il n'y a pas de désynchro entre l'indice connu et le dernier fichier envoyé
select * from seq_fichier where jour = '10' and mois = '08' and annee = '2021';

# Compter le nombre de codes ajoutés (en tenant compte de l'UTC)
select count(*) from submission_code where date_available between '2021-08-09 22:00:00' and '2021-08-17 22:00:00';

# Lancer le script python de vérification des codes dans la base de données
python verify-codes.py [dbName] [dbHost] [dbPort] [dbUser] [qrcodes-file-path]
```

S'il manque des codes, il faut regénérer le nombre de code souhaité.
