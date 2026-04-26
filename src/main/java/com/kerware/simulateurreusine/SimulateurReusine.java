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

    // ===== Constantes EXG_IMPOT_06 : décote pour les foyers modestes =====
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

    // ===== Constantes EXG_IMPOT_07 : contribution exceptionnelle sur les hauts revenus =====
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


        // parts déclarants
        // EXIG  : EXG_IMPOT_03
        switch ( sitFam ) {
            case CELIBATAIRE:
                nbPtsDecl = 1;
                break;
            case MARIE:
                nbPtsDecl = 2;
                break;
            case DIVORCE:
                nbPtsDecl = 1;
                break;
            case VEUF:
                nbPtsDecl = 1;
                break;
            case PACSE:
                nbPtsDecl = 2;
                break;
        }

        System.out.println( "Nombre d'enfants  : " + nbEnf );
        System.out.println( "Nombre d'enfants handicapés : " + nbEnfH );

        // parts enfants à charge
        if ( nbEnf <= 2 ) {
            nbPts = nbPtsDecl + nbEnf * 0.5;
        } else if ( nbEnf > 2 ) {
            nbPts = nbPtsDecl+  1.0 + ( nbEnf - 2 );
        }

        // parent isolé

        System.out.println( "Parent isolé : " + parIso );

        if ( parIso ) {
            if ( nbEnf > 0 ){
                nbPts = nbPts + 0.5;
            }
        }

        // Veuf avec enfant
        if ( sitFam == SituationFamiliale.VEUF && nbEnf > 0 ) {
            nbPts = nbPts + 1;
        }

        // enfant handicapé
        nbPts = nbPts + nbEnfH * 0.5;

        System.out.println( "Nombre de parts : " + nbPts );

        // EXIGENCE : EXG_IMPOT_07:
        // Contribution exceptionnelle sur les hauts revenus
        contribExceptionnelle = 0;
        int i = 0;
        do {
            if ( rFRef >= LIMITES_TRANCHES_CEHR[i] && rFRef < LIMITES_TRANCHES_CEHR[i+1] ) {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( rFRef - LIMITES_TRANCHES_CEHR[i] ) * TAUX_CEHR_CELIBATAIRE[i];
                } else {
                    contribExceptionnelle += ( rFRef - LIMITES_TRANCHES_CEHR[i] ) * TAUX_CEHR_COUPLE[i];
                }
                break;
            } else {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( LIMITES_TRANCHES_CEHR[i+1] - LIMITES_TRANCHES_CEHR[i] ) * TAUX_CEHR_CELIBATAIRE[i];
                } else {
                    contribExceptionnelle += ( LIMITES_TRANCHES_CEHR[i+1] - LIMITES_TRANCHES_CEHR[i] ) * TAUX_CEHR_COUPLE[i];
                }
            }
            i++;
        } while( i < 5);

        contribExceptionnelle = Math.round( contribExceptionnelle );
        System.out.println( "Contribution exceptionnelle sur les hauts revenus : " + contribExceptionnelle );

        // Calcul impôt des declarants
        // EXIGENCE : EXG_IMPOT_04
        rImposable = rFRef / nbPtsDecl ;

        mImpDecl = 0;

        i = 0;
        do {
            if ( rImposable >= LIMITES_TRANCHES_IMPOT[i] && rImposable < LIMITES_TRANCHES_IMPOT[i+1] ) {
                mImpDecl += ( rImposable - LIMITES_TRANCHES_IMPOT[i] ) * TAUX_IMPOT[i];
                break;
            } else {
                mImpDecl += ( LIMITES_TRANCHES_IMPOT[i+1] - LIMITES_TRANCHES_IMPOT[i] ) * TAUX_IMPOT[i];
            }
            i++;
        } while( i < 5);

        mImpDecl = mImpDecl * nbPtsDecl;
        mImpDecl = Math.round( mImpDecl );

        System.out.println( "Impôt brut des déclarants : " + mImpDecl );

        // Calcul impôt foyer fiscal complet
        // EXIGENCE : EXG_IMPOT_04
        rImposable =  rFRef / nbPts;
        mImp = 0;
        i = 0;

        do {
            if ( rImposable >= LIMITES_TRANCHES_IMPOT[i] && rImposable < LIMITES_TRANCHES_IMPOT[i+1] ) {
                mImp += ( rImposable - LIMITES_TRANCHES_IMPOT[i] ) * TAUX_IMPOT[i];
                break;
            } else {
                mImp += ( LIMITES_TRANCHES_IMPOT[i+1] - LIMITES_TRANCHES_IMPOT[i] ) * TAUX_IMPOT[i];
            }
            i++;
        } while( i < 5);

        mImp = mImp * nbPts;
        mImp = Math.round( mImp );

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

        decote = 0;
        // decote
        if ( nbPtsDecl == 1 ) {
            if ( mImp < SEUIL_DECOTE_DECLARANT_SEUL ) {
                decote = DECOTE_MAX_DECLARANT_SEUL - ( mImp  * TAUX_DECOTE );
            }
        }
        if (  nbPtsDecl == 2 ) {
            if ( mImp < SEUIL_DECOTE_DECLARANT_COUPLE ) {
                decote =  DECOTE_MAX_DECLARANT_COUPLE - ( mImp  * TAUX_DECOTE  );
            }
        }
        decote = Math.round( decote );

        if ( mImp <= decote ) {
            decote = mImp;
        }

        System.out.println( "Decote : " + decote );

        mImp = mImp - decote;

        mImp += contribExceptionnelle;

        mImp = Math.round( mImp );

        System.out.println( "Impôt sur le revenu net final : " + mImp );
        return  (int)mImp;
    }
}
