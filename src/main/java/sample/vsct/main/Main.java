package sample.vsct.main;

import java.util.Scanner;

import org.apache.log4j.Logger;

import sample.vsct.exception.TailleColisException;
import sample.vsct.service.EmballageService;
import sample.vsct.service.EmballageServiceImpl;

public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);

	/**
	 * Méthode de test avec entrée utilisateur
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Merci d'indiquer ici les tailles des colis en entrée");
		// Récupération de l'entrée standard
		Scanner stdin = new Scanner(System.in);
		String colisEntree = stdin.nextLine();
		
		EmballageService emballageService = new EmballageServiceImpl();
		try {
			// S'il y a plus de 30 colis, on affiche de manière optimisée
			boolean affichageOptimise = false;
			if (colisEntree.length() > 30) {
				affichageOptimise = true;
			}
			// Appel de la méthode principale
			String cartonsEmballes = emballageService.emballerCartons(colisEntree, affichageOptimise);
			System.out.print("Voici les cartons à emballer :");
			if (affichageOptimise) {
				System.out.println("(Avec entre parenthèses le nombre de cartons pour chaque combinaison de tailles)");
			}
			System.out.println(cartonsEmballes);
		} catch (TailleColisException e) {
			logger.error(e.getMessage());
		}

	}
}
