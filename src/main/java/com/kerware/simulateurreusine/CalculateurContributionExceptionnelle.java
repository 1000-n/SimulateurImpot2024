package com.kerware.simulateurreusine;

/**
 * Calculateur de la contribution exceptionnelle sur les hauts revenus (CEHR).
 *
 * Implémente l'exigence EXG_IMPOT_07 :
 *  - Calculée sur le revenu fiscal de référence du foyer entier
 *  - Barèmes différents pour personne seule et couple
 *  - Calcul progressif identique au barème de l'impôt principal
 */
public class CalculateurContributionExceptionnelle {

    /** Limites des tranches de la CEHR (en euros). 5 valeurs pour 4 tranches. */
    private static final int[] LIMITES_TRANCHES_CEHR = {
            0, 250000, 500000, 1000000, Integer.MAX_VALUE
    };
    /** Taux de la CEHR pour une personne seule (célibataire, divorcé, veuf). */
    private static final double[] TAUX_CEHR_CELIBATAIRE = {
            0.0, 0.03, 0.04, 0.04
    };
    /** Taux de la CEHR pour un couple (marié, pacsé). */
    private static final double[] TAUX_CEHR_COUPLE = {
            0.0, 0.0, 0.03, 0.04
    };

    /** Nombre de parts d'un déclarant seul. */
    private static final double PARTS_DECLARANT_SEUL = 1.0;

    private final CalculateurImpotProgressif calculateurProgressif = new CalculateurImpotProgressif();

    /**
     * Calcule la contribution exceptionnelle sur les hauts revenus.
     *
     * @param revenuFiscalReference  revenu fiscal de référence du foyer
     * @param nbPartsDeclarants      nombre de parts des déclarants seuls (1 ou 2)
     * @return la CEHR arrondie à l'euro le plus proche
     */
    public double calculer(double revenuFiscalReference, double nbPartsDeclarants) {
        double[] tauxApplicables = (nbPartsDeclarants == PARTS_DECLARANT_SEUL)
                ? TAUX_CEHR_CELIBATAIRE
                : TAUX_CEHR_COUPLE;

        return Math.round(
                calculateurProgressif.calculer(revenuFiscalReference, LIMITES_TRANCHES_CEHR, tauxApplicables)
        );
    }
}