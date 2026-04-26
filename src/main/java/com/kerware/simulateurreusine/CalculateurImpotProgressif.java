package com.kerware.simulateurreusine;

/**
 * Calculateur générique d'un montant par tranches progressives.
 *
 * Utilisé pour :
 *  - le barème de l'impôt sur le revenu (EXG_IMPOT_04)
 *  - la contribution exceptionnelle sur les hauts revenus (EXG_IMPOT_07)
 *
 * Le principe : pour un revenu donné, chaque tranche traversée est taxée
 * à son propre taux. Le résultat est la somme des contributions de toutes
 * les tranches traversées.
 *
 * Exemple : pour un revenu de 30000€ avec les tranches de l'impôt 2024
 * (0% jusqu'à 11294€, 11% jusqu'à 28797€, 30% au-delà) :
 *   (28797 - 11294) * 11% + (30000 - 28797) * 30% = 1925 + 361 = 2286€
 */
public class CalculateurImpotProgressif {

    /**
     * Calcule un montant progressif sur les tranches.
     *
     * @param montantImposable montant à imposer (ex : revenu fiscal de référence par part)
     * @param limites          limites des tranches (longueur N+1 pour N tranches,
     *                         première = 0, dernière = Integer.MAX_VALUE)
     * @param taux             taux de chaque tranche (longueur N)
     * @return le montant calculé (non arrondi)
     */
    public double calculer(double montantImposable, int[] limites, double[] taux) {
        double montant = 0;
        for (int i = 0; i < taux.length; i++) {
            if (montantImposable >= limites[i] && montantImposable < limites[i + 1]) {
                // On est dans la tranche courante : on prend ce qui dépasse la limite basse
                montant += (montantImposable - limites[i]) * taux[i];
                break;
            } else {
                // On a déjà dépassé cette tranche : on la compte en plein
                montant += (limites[i + 1] - limites[i]) * taux[i];
            }
        }
        return montant;
    }
}