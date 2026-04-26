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

    // revenu net
    private int rNetDecl1 = 0;
    private int rNetDecl2 = 0;
    // nb enfants
    private int nbEnf = 0;
    // nb enfants handicapés
    private int nbEnfH = 0;

    // revenu fiscal de référence
    private double rFRef = 0;

    // revenu imposable
    private double rImposable = 0;

    // abattement
    private double abt = 0;

    // nombre de parts des  déclarants
    private double nbPtsDecl = 0;
    // nombre de parts du foyer fiscal
    private double nbPts = 0;

    // decote
    private double decote = 0;
    // impôt des déclarants
    private double mImpDecl = 0;
    // impôt du foyer fiscal
    private double mImp = 0;
    private double mImpAvantDecote = 0;
    // parent isolé
    private boolean parIso = false;
    // Contribution exceptionnelle sur les hauts revenus
    private double contribExceptionnelle = 0;

    // Getters pour adapter le code legacy pour les tests unitaires

    public double getRevenuReference() {
        return rFRef;
    }

    public double getDecote() {
        return decote;
    }


    public double getAbattement() {
        return abt;
    }

    public double getNbParts() {
        return nbPts;
    }

    public double getImpotAvantDecote() {
        return mImpAvantDecote;
    }

    public double getImpotNet() {
        return mImp;
    }

    public int getRevenuNetDeclatant1() {
        return rNetDecl1;
    }

    public int getRevenuNetDeclatant2() {
        return rNetDecl2;
    }

    public double getContribExceptionnelle() {
        return contribExceptionnelle;
    }


    // Fonction de calcul de l'impôt sur le revenu net en France en 2024 sur les revenu 2023

    public int calculImpot(int revNetDecl1, int revNetDecl2, SituationFamiliale sitFam, int nbEnfants, int nbEnfantsHandicapes, boolean parentIsol) {

        // Préconditions
        if ( revNetDecl1  < 0 || revNetDecl2 < 0 ) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif");
        }

        if ( nbEnfants < 0 ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif");
        }

        if ( nbEnfantsHandicapes < 0 ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être négatif");
        }

        if ( sitFam == null ) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }

        if ( nbEnfantsHandicapes > nbEnfants ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants");
        }

        if ( nbEnfants > 7 ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être supérieur à 7");
        }

        if ( parentIsol && ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) ) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }

        boolean seul = sitFam == SituationFamiliale.CELIBATAIRE || sitFam == SituationFamiliale.DIVORCE || sitFam == SituationFamiliale.VEUF;
        if (  seul && revNetDecl2 > 0 ) {
            throw new IllegalArgumentException("Un célibataire, un divorcé ou un veuf ne peut pas avoir de revenu pour le déclarant 2");
        }

        // Initialisation des variables

        rNetDecl1 = revNetDecl1;
        rNetDecl2 = revNetDecl2;

        nbEnf = nbEnfants;
        nbEnfH = nbEnfantsHandicapes;
        parIso = parentIsol;

        System.out.println("--------------------------------------------------");
        System.out.println( "Revenu net declarant1 : " + rNetDecl1 );
        System.out.println( "Revenu net declarant2 : " + rNetDecl2 );
        System.out.println( "Situation familiale : " + sitFam.name() );

        // Abattement
        // EXIGENCE : EXG_IMPOT_02
        abt = new CalculateurAbattement().calculer(rNetDecl1, rNetDecl2, sitFam);
        System.out.println( "Abattement : " + abt );

        rFRef = rNetDecl1 + revNetDecl2 - abt;
        if ( rFRef < 0 ) {
            rFRef = 0;
        }

        System.out.println( "Revenu fiscal de référence : " + rFRef );


        // Parts fiscales
        // EXIGENCE : EXG_IMPOT_03
        CalculateurParts calculateurParts = new CalculateurParts();
        nbPtsDecl = calculateurParts.calculerPartsDeclarants(sitFam);
        nbPts = calculateurParts.calculerPartsFoyer(sitFam, nbEnf, nbEnfH, parIso);


        // EXIGENCE : EXG_IMPOT_07
        // Contribution exceptionnelle sur les hauts revenus
        contribExceptionnelle = new CalculateurContributionExceptionnelle()
                .calculer(rFRef, nbPtsDecl);

        System.out.println( "Contribution exceptionnelle sur les hauts revenus : " + contribExceptionnelle );

        // Calcul impôt des declarants
        // EXIGENCE : EXG_IMPOT_04
        rImposable = rFRef / nbPtsDecl;
        CalculateurImpotProgressif calculateurImpotProgressif = new CalculateurImpotProgressif();
        mImpDecl = Math.round(
                calculateurImpotProgressif.calculer(rImposable, LIMITES_TRANCHES_IMPOT, TAUX_IMPOT)
                        * nbPtsDecl
        );

        System.out.println( "Impôt brut des déclarants : " + mImpDecl );

        // Calcul impôt foyer fiscal complet
        // EXIGENCE : EXG_IMPOT_04
        rImposable = rFRef / nbPts;
        mImp = Math.round(
                calculateurImpotProgressif.calculer(rImposable, LIMITES_TRANCHES_IMPOT, TAUX_IMPOT)
                        * nbPts
        );

        System.out.println( "Impôt brut du foyer fiscal complet : " + mImp );

        // Vérification de la baisse d'impôt autorisée
        // EXIGENCE : EXG_IMPOT_05
        // baisse impot

        double baisseImpot = mImpDecl - mImp;

        System.out.println( "Baisse d'impôt : " + baisseImpot );

        // dépassement plafond
        double ecartPts = nbPts - nbPtsDecl;

        double plafond = (ecartPts / 0.5) * PLAFOND_DEMI_PART;

        System.out.println( "Plafond de baisse autorisée " + plafond );

        if ( baisseImpot >= plafond ) {
            mImp = mImpDecl - plafond;
        }

        System.out.println( "Impôt brut après plafonnement avant decote : " + mImp );
        mImpAvantDecote = mImp;

        // Calcul de la decote
        // EXIGENCE : EXG_IMPOT_06
        decote = new CalculateurDecote().calculer(mImp, nbPtsDecl);

        System.out.println( "Decote : " + decote );

        mImp = mImp - decote;

        mImp += contribExceptionnelle;

        mImp = Math.round( mImp );

        System.out.println( "Impôt sur le revenu net final : " + mImp );
        return  (int)mImp;
    }
}
