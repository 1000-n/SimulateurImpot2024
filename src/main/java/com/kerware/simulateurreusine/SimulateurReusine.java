package com.kerware.simulateurreusine;

import com.kerware.simulateur.SituationFamiliale;

public class SimulateurReusine {


    // ===== Constantes EXG_IMPOT_04 : barème progressif de l'impôt sur le revenu =====
    /** Limites des tranches d'imposition (en euros). 6 valeurs pour 5 tranches. */
    private static final int[] LIMITES_TRANCHES_IMPOT = {
            0, 11294, 28797, 82341, 177106, Integer.MAX_VALUE
    };
    /** Taux d'imposition par tranche (correspondance 1-à-1 avec LIMITES_TRANCHES_IMPOT). */
    private static final double[] TAUX_IMPOT = {
            0.0, 0.11, 0.30, 0.41, 0.45
    };

    // ===== Constantes EXG_IMPOT_05 : plafonnement du quotient familial =====
    /** Plafond du gain d'impôt apporté par chaque demi-part supplémentaire (en euros). */
    private static final double PLAFOND_DEMI_PART = 1759;

    /** Nombre maximum d'enfants à charge supporté par le simulateur. */
    private static final int NB_MAX_ENFANTS = 7;



    // revenu fiscal de référence
    private double revenuFiscalReference = 0;

    // revenu imposable
    private double revenuImposable = 0;

    // abattement
    private double abattement = 0;

    // nombre de parts des  déclarants
    private double nombrePartsDeclarants = 0;
    // nombre de parts du foyer fiscal
    private double nombrePartsFoyer = 0;

    // decote
    private double decote = 0;
    // impôt des déclarants
    private double impotDeclarants = 0;
    // impôt du foyer fiscal
    private double impotNet = 0;
    private double impotAvantDecote = 0;
    // Contribution exceptionnelle sur les hauts revenus
    private double contribExceptionnelle = 0;

    // Getters pour adapter le code legacy pour les tests unitaires

    public double getRevenuReference() {
        return revenuFiscalReference;
    }

    public double getDecote() {
        return decote;
    }


    public double getAbattement() {
        return abattement;
    }

    public double getNbParts() {
        return nombrePartsFoyer;
    }

    public double getImpotAvantDecote() {
        return impotAvantDecote;
    }

    public double getImpotNet() {
        return impotNet;
    }

    public double getContribExceptionnelle() {
        return contribExceptionnelle;
    }


    // Fonction de calcul de l'impôt sur le revenu net en France en 2024 sur les revenu 2023

    public int calculImpot(int revenuNetDeclarant1,
                           int revenuNetDeclarant2,
                           SituationFamiliale situationFamiliale,
                           int nombreEnfants, int nombreEnfantsHandicapes,
                           boolean parentIsole) {

        verifierPreconditions(revenuNetDeclarant1, revenuNetDeclarant2,
                situationFamiliale, nombreEnfants, nombreEnfantsHandicapes, parentIsole);

        // Abattement
        // EXIGENCE : EXG_IMPOT_02
        abattement = new CalculateurAbattement().calculer(revenuNetDeclarant1, revenuNetDeclarant2, situationFamiliale);

        revenuFiscalReference = revenuNetDeclarant1 + revenuNetDeclarant2 - abattement;
        if ( revenuFiscalReference < 0 ) {
            revenuFiscalReference = 0;
        }

        // Parts fiscales
        // EXIGENCE : EXG_IMPOT_03
        CalculateurParts calculateurParts = new CalculateurParts();
        nombrePartsDeclarants = calculateurParts.calculerPartsDeclarants(situationFamiliale);
        nombrePartsFoyer = calculateurParts.calculerPartsFoyer(situationFamiliale, nombreEnfants, nombreEnfantsHandicapes, parentIsole);

        // EXIGENCE : EXG_IMPOT_07
        // Contribution exceptionnelle sur les hauts revenus
        contribExceptionnelle = new CalculateurContributionExceptionnelle()
                .calculer(revenuFiscalReference, nombrePartsDeclarants);

        // Calcul impôt des declarants
        // EXIGENCE : EXG_IMPOT_04
        revenuImposable = revenuFiscalReference / nombrePartsDeclarants;
        CalculateurImpotProgressif calculateurImpotProgressif = new CalculateurImpotProgressif();
        impotDeclarants = Math.round(
                calculateurImpotProgressif.calculer(revenuImposable, LIMITES_TRANCHES_IMPOT, TAUX_IMPOT)
                        * nombrePartsDeclarants
        );

        // Calcul impôt foyer fiscal complet
        // EXIGENCE : EXG_IMPOT_04
        revenuImposable = revenuFiscalReference / nombrePartsFoyer;
        impotNet = Math.round(
                calculateurImpotProgressif.calculer(revenuImposable, LIMITES_TRANCHES_IMPOT, TAUX_IMPOT)
                        * nombrePartsFoyer
        );

        // Vérification de la baisse d'impôt autorisée
        // EXIGENCE : EXG_IMPOT_05
        // baisse impot

        double baisseImpot = impotDeclarants - impotNet;

        // dépassement plafond
        double ecartPts = nombrePartsFoyer - nombrePartsDeclarants;

        double plafond = (ecartPts / 0.5) * PLAFOND_DEMI_PART;

        if ( baisseImpot >= plafond ) {
            impotNet = impotDeclarants - plafond;
        }

        impotAvantDecote = impotNet;

        // Calcul de la decote
        // EXIGENCE : EXG_IMPOT_06
        decote = new CalculateurDecote().calculer(impotNet, nombrePartsDeclarants);

        impotNet = impotNet - decote;

        impotNet += contribExceptionnelle;

        impotNet = Math.round( impotNet );

        return  (int)impotNet;
    }

    /**
     * Vérifie les préconditions sur les paramètres d'entrée.
     *
     * @throws IllegalArgumentException si une combinaison de paramètres est invalide
     */
    private void verifierPreconditions(int revenuNetDeclarant1, int revenuNetDeclarant2,
                                       SituationFamiliale situationFamiliale,
                                       int nbEnfants, int nbEnfantsHandicapes,
                                       boolean parentIsole) {
        if (revenuNetDeclarant1 < 0 || revenuNetDeclarant2 < 0) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif");
        }
        if (nbEnfants < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif");
        }
        if (nbEnfantsHandicapes < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être négatif");
        }
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }
        if (nbEnfantsHandicapes > nbEnfants) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants");
        }
        if (nbEnfants > NB_MAX_ENFANTS) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants ne peut pas être supérieur à " + NB_MAX_ENFANTS);
        }
        if (parentIsole && estCouple(situationFamiliale)) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }
        if (estSeul(situationFamiliale) && revenuNetDeclarant2 > 0) {
            throw new IllegalArgumentException(
                    "Un célibataire, un divorcé ou un veuf ne peut pas avoir de revenu pour le déclarant 2");
        }
    }

    private boolean estCouple(SituationFamiliale situationFamiliale) {
        return situationFamiliale == SituationFamiliale.MARIE || situationFamiliale == SituationFamiliale.PACSE;
    }

    private boolean estSeul(SituationFamiliale situationFamiliale) {
        return situationFamiliale == SituationFamiliale.CELIBATAIRE
                || situationFamiliale == SituationFamiliale.DIVORCE
                || situationFamiliale == SituationFamiliale.VEUF;
    }
}
