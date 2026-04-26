package com.kerware.simulateurreusine;

/**
 * Calculateur de la décote pour les foyers fiscaux modestes.
 *
 * Implémente l'exigence EXG_IMPOT_06 :
 *  - Si l'impôt est inférieur à un seuil, une décote (réduction) s'applique
 *  - Seuils différents pour personne seule (1929€) et couple (3191€)
 *  - Formule : decote = decoteMax - (impot * tauxDecote)
 *  - La décote ne peut pas dépasser le montant de l'impôt lui-même
 */
public class CalculateurDecote {

    /** Seuil d'impôt en deçà duquel la décote s'applique pour un déclarant seul. */
    private static final double SEUIL_DECOTE_DECLARANT_SEUL = 1929;
    /** Seuil d'impôt en deçà duquel la décote s'applique pour un couple. */
    private static final double SEUIL_DECOTE_DECLARANT_COUPLE = 3191;
    /** Décote maximale pour un déclarant seul (en euros). */
    private static final double DECOTE_MAX_DECLARANT_SEUL = 873;
    /** Décote maximale pour un couple (en euros). */
    private static final double DECOTE_MAX_DECLARANT_COUPLE = 1444;
    /** Taux appliqué à l'impôt dans le calcul de la décote. */
    private static final double TAUX_DECOTE = 0.4525;

    /** Nombre de parts d'un déclarant seul. */
    private static final double PARTS_DECLARANT_SEUL = 1.0;
    /** Nombre de parts d'un couple. */
    private static final double PARTS_COUPLE = 2.0;

    /**
     * Calcule la décote applicable au foyer fiscal.
     *
     * @param impotAvantDecote   montant d'impôt avant application de la décote
     * @param nbPartsDeclarants  nombre de parts des déclarants seuls (1 ou 2)
     * @return la décote (toujours ≤ impotAvantDecote)
     */
    public double calculer(double impotAvantDecote, double nbPartsDeclarants) {
        double decote = calculerDecoteBrute(impotAvantDecote, nbPartsDeclarants);
        decote = Math.round(decote);

        // La décote ne peut pas faire descendre l'impôt en-dessous de zéro
        if (impotAvantDecote <= decote) {
            decote = impotAvantDecote;
        }
        return decote;
    }

    private double calculerDecoteBrute(double impotAvantDecote, double nbPartsDeclarants) {
        if (nbPartsDeclarants == PARTS_DECLARANT_SEUL
                && impotAvantDecote < SEUIL_DECOTE_DECLARANT_SEUL) {
            return DECOTE_MAX_DECLARANT_SEUL - (impotAvantDecote * TAUX_DECOTE);
        }
        if (nbPartsDeclarants == PARTS_COUPLE
                && impotAvantDecote < SEUIL_DECOTE_DECLARANT_COUPLE) {
            return DECOTE_MAX_DECLARANT_COUPLE - (impotAvantDecote * TAUX_DECOTE);
        }
        return 0;
    }
}