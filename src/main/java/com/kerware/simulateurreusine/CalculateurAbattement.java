package com.kerware.simulateurreusine;

import com.kerware.simulateur.SituationFamiliale;

/**
 * Calculateur d'abattement sur les revenus nets déclarés.
 *
 * Implémente l'exigence EXG_IMPOT_02 :
 *  - taux de 10% appliqué SEPAREMENT à chaque déclarant
 *  - plafonné à PLAFOND_ABATTEMENT par déclarant
 *  - minimum MINIMUM_ABATTEMENT par déclarant
 *  - l'abattement du foyer est la somme des deux abattements individuels
 */
public class CalculateurAbattement {

    /** Plafond annuel de l'abattement par déclarant en 2024 (en euros). */
    private static final int PLAFOND_ABATTEMENT = 14171;
    /** Minimum annuel de l'abattement par déclarant en 2024 (en euros). */
    private static final int MINIMUM_ABATTEMENT = 495;
    /** Taux de l'abattement appliqué aux revenus nets déclarés (10%). */
    private static final double TAUX_ABATTEMENT = 0.1;

    /**
     * Calcule l'abattement total du foyer fiscal.
     *
     * @param revenuNetDeclarant1  revenu net du déclarant 1
     * @param revenuNetDeclarant2  revenu net du déclarant 2 (0 si déclarant unique)
     * @param situationFamiliale   situation familiale du foyer
     * @return l'abattement total du foyer
     */
    public long calculer(int revenuNetDeclarant1, int revenuNetDeclarant2,
                         SituationFamiliale situationFamiliale) {
        long abattementDeclarant1 = calculerAbattementIndividuel(revenuNetDeclarant1);
        long abattementDeclarant2 = 0;

        if (estCouple(situationFamiliale)) {
            abattementDeclarant2 = calculerAbattementIndividuel(revenuNetDeclarant2);
        }

        return abattementDeclarant1 + abattementDeclarant2;
    }

    /**
     * Calcule l'abattement individuel pour un déclarant en appliquant
     * le taux puis les bornes (min et max).
     */
    private long calculerAbattementIndividuel(int revenuNet) {
        long abattement = Math.round(revenuNet * TAUX_ABATTEMENT);
        if (abattement > PLAFOND_ABATTEMENT) {
            abattement = PLAFOND_ABATTEMENT;
        }
        if (abattement < MINIMUM_ABATTEMENT) {
            abattement = MINIMUM_ABATTEMENT;
        }
        return abattement;
    }

    private boolean estCouple(SituationFamiliale situationFamiliale) {
        return situationFamiliale == SituationFamiliale.MARIE
                || situationFamiliale == SituationFamiliale.PACSE;
    }
}