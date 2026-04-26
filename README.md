# Simulateur Impôt 2024

Mini-projet IUT — Tests unitaires fonctionnels et refactoring de code hérité.

📊 **[Rapports qualité en ligne](https://TON_PSEUDO.github.io/SimulateurImpot2024/)**
(tests, couverture JaCoCo, analyse CheckStyle)

## Structure du projet

- `com.kerware.simulateur` : code hérité (legacy) avec son adaptateur
- `com.kerware.simulateurreusine` : code réusiné, modulaire, avec ses 6 calculateurs
- `simulateur.TestsSimulateur` : 53 tests sur le code hérité
- `simulateur.TestsSimulateurReusine` : 53 tests sur le code réusiné

## Lancer les tests

```bash
mvn clean verify site
```

## Exigences couvertes

- EXG_IMPOT_02 : abattement
- EXG_IMPOT_03 : nombre de parts
- EXG_IMPOT_04 : barème progressif
- EXG_IMPOT_05 : plafonnement du quotient familial
- EXG_IMPOT_06 : décote
- EXG_IMPOT_07 : contribution exceptionnelle (CEHR)
- Robustesse : 9 préconditions
