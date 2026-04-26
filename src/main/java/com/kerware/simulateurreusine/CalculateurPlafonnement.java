package com.kerware.simulateurreusine;

/**
 * Calculateur du plafonnement du gain lié au quotient familial.
 *
 * Implémente l'exigence EXG_IMPOT_05 :
 *  - Le quotient familial (parts apportées par les enfants/situations particulières)
 *    permet de réduire l'impôt
 *  - Cette réduction est cependant plafonnée à un montant fixe par demi-part supplémentaire
 *  - Si le gain réel dépasse ce plafond, on applique la limite au lieu du gain réel
 */
public class CalculateurPlafonnement {

    /** Plafond du gain d'impôt apporté par chaque demi-part supplémentaire (en euros). */
    private static final double PLAFOND_DEMI_PART = 1759;
    /** Une demi-part = 0.5 part. */
    private static final double VALEUR_DEMI_PART = 0.5;

    /**
     * Applique le plafonnement à l'impôt du foyer si le gain dépasse le plafond légal.
     *
     * @param impotDeclarants    impôt calculé sur les seules parts des déclarants
     * @param impotFoyer         impôt calculé sur le nombre total de parts du foyer
     * @param nbPartsDeclarants  nombre de parts des déclarants seuls
     * @param nbPartsFoyer       nombre total de parts du foyer
     * @return l'impôt du foyer après application éventuelle du plafonnement
     */
    public double appliquerPlafonnement(double impotDeclarants, double impotFoyer,
                                        double nbPartsDeclarants, double nbPartsFoyer) {
        double gainReel = impotDeclarants - impotFoyer;
        double plafond = calculerPlafond(nbPartsDeclarants, nbPartsFoyer);

        if (gainReel >= plafond) {
            return impotDeclarants - plafond;
        }
        return impotFoyer;
    }

    /**
     * Calcule le plafond de gain autorisé selon le nombre de demi-parts supplémentaires.
     */
    private double calculerPlafond(double nbPartsDeclarants, double nbPartsFoyer) {
        double ecartParts = nbPartsFoyer - nbPartsDeclarants;
        double nbDemiParts = ecartParts / VALEUR_DEMI_PART;
        return nbDemiParts * PLAFOND_DEMI_PART;
    }
}