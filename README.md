# Gestion Pharmacie — PharmaSys

## Prérequis
- Java 21+
- MySQL 8+
- mysql-connector-j-9.6.0.jar (à ajouter dans les libraries IntelliJ)

## Configuration base de données
Modifier `src/entitebd/ConnectionBD.java` :
```java
private static final String URL      = "jdbc:mysql://localhost:3306/PHARMACIE";
private static final String USER     = "root";
private static final String PASSWORD = "mdp";   // ← votre mot de passe
```

## Lancer l'application
Point d'entrée : `src/interfaces/LoginFrame.java` → méthode `main()`

Identifiants par défaut : renseignez un employé en base (table `employe` + `personne`).

## Architecture
```
src/
  entite/          Modèles métier (Medicament, Vente, Client…)
  entitebd/        Accès base de données (JDBC)
  exception/       Exceptions métier personnalisées
  gestion/         Logique applicative (services)
  interfaces/
    theme/         Système de design (PharmTheme, PharmSidebar, PharmBaseFrame)
    produit/       Écrans médicaments
    vente/         Écrans ventes & factures
    client/        Écrans clients & fidélité
    commande/      Écrans commandes fournisseurs
    stock/         Écrans stock & alertes
    employe/       Écrans RH
    fournisseur/   Écrans fournisseurs
    rapport/       Écrans rapports & statistiques
```

## Palette couleurs
| Rôle            | Hex       |
|-----------------|-----------|
| Vert primaire   | `#0A3D2E` |
| Accent lime     | `#C8F56A` |
| Fond page       | `#F0EDE8` |
| Fond carte      | `#FFFFFF` |
| Texte principal | `#1A1A16` |
| Danger          | `#C62828` |
| Succès          | `#2E7D32` |
