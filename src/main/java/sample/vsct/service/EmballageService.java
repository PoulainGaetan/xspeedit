package sample.vsct.service;

/**
 * Permet d'emballer des colis dans des cartons
 * @author Gaetan
 *
 */
public interface EmballageService {

	/**
	 * Emballe des colis dans des cartons de manière optimal (minimum de cartons).
	 * Par exemple. Si l'entrée est "9551". La sortie devra être "91/55" ou "55/91"
	 * 
	 * @param cartonsEntree
	 *            Décrit les tailles des colis en entrée
	 * @param affichageOptimisee
	 *            Dans le cas où il y aurait beaucoup de cartons, affiche les
	 *            données de manière optimisé en indiquant le nombre de cartons
	 *            entre paraenthèse pour chaque combinaison de taille. Par exemple
	 *            Entrée : 91919191. Retour :91(4)
	 * @return Les cartons avec les tailles des colis le composant.
	 */
	String emballerCartons(String cartonsEntree, boolean affichageOptimisee);

}
