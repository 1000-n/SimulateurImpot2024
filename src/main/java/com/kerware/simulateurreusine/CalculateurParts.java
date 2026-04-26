package com.kerware.simulateurreusine;

import com.kerware.simulateur.SituationFamiliale;

/**
 * Calculateur du nombre de parts fiscales pour le foyer.
 *
 * Implémente l'exigence EXG_IMPOT_03 :
 *  - 1 part par déclarant (2 pour un couple)
 *  - 0,5 part pour chacun des 2 premiers enfants
 *  - 1 part par enfant à partir du 3e
 *  - 0,5 part supplémentaire par enfant en situation de handicap
 *  - 0,5 part supplémentaire pour un parent isolé avec enfants
 *  - 1 part supplémentaire pour un veuf avec enfants à charge
 */
public final class CalculateurParts {

    /** Demi-part par enfant pour les 2 premiers. */
    private static final double DEMI_PART_PAR_ENFANT = 0.5;
    /** Part complète par enfant à partir du 3e. */
    private static final double PART_PAR_ENFANT_AU_DELA_DE_2 = 1.0;
    /** Demi-part supplémentaire pour parent isolé avec enfants. */
    private static final double DEMI_PART_PARENT_ISOLE = 0.5;
    /** Part supplémentaire pour un veuf avec enfants. */
    private static final double PART_SUPPLEMENTAIRE_VEUF_AVEC_ENFANT = 1.0;
    /** Demi-part par enfant en situation de handicap. */
    private static final double DEMI_PART_ENFANT_HANDICAPE = 0.5;
    /** Seuil au-delà duquel les enfants comptent pour 1 part complète. */
    private static final int SEUIL_NB_ENFANTS_PART_COMPLETE = 2;

    /**
     * Calcule le nombre de parts des déclarants seuls (sans tenir compte des enfants).
     * Sert au calcul du plafonnement du quotient familial (EXG_IMPOT_05).
     */
    public double calculerPartsDeclarants(SituationFamiliale situationFamiliale) {
        return estCouple(situationFamiliale) ? 2.0 : 1.0;
    }

    /**
     * Calcule le nombre total de parts du foyer fiscal.
     */
    public double calculerPartsFoyer(SituationFamiliale situationFamiliale,
                                     int nbEnfants,
                                     int nbEnfantsHandicapes,
                                     boolean parentIsole) {
        double parts = calculerPartsDeclarants(situationFamiliale);
        parts += partsPourEnfants(nbEnfants);
        parts += partsPourParentIsole(parentIsole, nbEnfants);
        parts += partsPourVeufAvecEnfants(situationFamiliale, nbEnfants);
        parts += nbEnfantsHandicapes * DEMI_PART_ENFANT_HANDICAPE;
        return parts;
    }

    private double partsPourEnfants(int nbEnfants) {
        if (nbEnfants <= SEUIL_NB_ENFANTS_PART_COMPLETE) {
            return nbEnfants * DEMI_PART_PAR_ENFANT;
        }
        // 1 part pour les 2 premiers enfants (0.5 + 0.5) + 1 part complète par enfant
        // supplémentaire
        return 1.0 + (nbEnfants - SEUIL_NB_ENFANTS_PART_COMPLETE) * PART_PAR_ENFANT_AU_DELA_DE_2;
    }

    private double partsPourParentIsole(boolean parentIsole, int nbEnfants) {
        if (parentIsole && nbEnfants > 0) {
            return DEMI_PART_PARENT_ISOLE;
        }
        return 0.0;
    }

    private double partsPourVeufAvecEnfants(SituationFamiliale situationFamiliale, int nbEnfants) {
        if (situationFamiliale == SituationFamiliale.VEUF && nbEnfants > 0) {
            return PART_SUPPLEMENTAIRE_VEUF_AVEC_ENFANT;
        }
        return 0.0;
    }

    private boolean estCouple(SituationFamiliale situationFamiliale) {
        return situationFamiliale == SituationFamiliale.MARIE
                || situationFamiliale == SituationFamiliale.PACSE;
    }
}