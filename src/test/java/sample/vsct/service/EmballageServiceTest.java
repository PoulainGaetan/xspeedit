package sample.vsct.service;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import sample.vsct.exception.TailleColisException;

/**
 * Classe de test pour EmballageService
 * @author Gaetan
 *
 */
public class EmballageServiceTest {

	// Normalement, on fait ici de l'IoC avec Spring Core par exemple.
	EmballageService emballageService = new EmballageServiceImpl();

	@Test
	// Test pour pour un grand nombre de taille de colis choisies aléatoirement
	public final void testMonteeCharge1() {
		// Initialisations
		int nbColisEntree = 100000;
		StringBuilder sb = new StringBuilder();
		// Génère des chiffres entre 1 et 9
		for (int i = 0; i < nbColisEntree; i++) {
			sb.append(Math.round(Math.random() * 9 + 0.5)).toString();
		}
		Long tempsAvantExecution = new Date().getTime();

		// Appel méthode
		emballageService.emballerCartons(sb.toString(), true);

		// Temps de traitement de la méthode
		Long tempsApresExecution = new Date().getTime();
		Long tempsExecution = tempsApresExecution - tempsAvantExecution;
		System.out.println("Le temps d'exécution est de " + tempsExecution.toString() + " ms");
	}

	@Test
	// Test pour pour un très grand nombre (plus de 100 millions de colis)
	public final void testMonteeCharge2() {
		// Initialisations
		int nbRepetitions = 10000000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nbRepetitions; i++) {
			sb.append("163841689525773");
		}
		Long tempsAvantExecution = new Date().getTime();

		// Appel méthode
		emballageService.emballerCartons(sb.toString(), true);

		// Temps de traitement de la méthode
		Long tempsApresExecution = new Date().getTime();
		Long tempsExecution = tempsApresExecution - tempsAvantExecution;
		System.out.println("Le temps d'exécution est de " + tempsExecution.toString() + " ms");
	}

	@Test
	// Test nominal
	public final void testNominal() {
		// Initialisation
		String entreeColis = "163841689525773";
		Integer nbOptimalCartons = 8;

		// Appel méthode
		String result = emballageService.emballerCartons(entreeColis, false);

		// Vértifications
		assertTrue(nbCartons(result) == nbOptimalCartons);
		assertTrue(verifieMemeNombreOccurenceEntreeSortie(entreeColis, result));
		assertTrue(verifieTailleMaxCartons(result));
	}

	@Test
	// Vérifie qu'on ne tombe pas dans le piège de créer le carton 22222 ce qui
	// n'optimise pas le nombre de cartons
	public final void testPiege1() {
		// Initialisation
		String entreeColis = "2222288888";
		Integer nbOptimalCartons = 5;

		// Appel méthode
		String result = emballageService.emballerCartons(entreeColis, false);

		// Vértifications
		assertTrue(nbCartons(result) == nbOptimalCartons);
		assertTrue(verifieMemeNombreOccurenceEntreeSortie(entreeColis, result));
		assertTrue(verifieTailleMaxCartons(result));
	}

	@Test
	// Vérifie qu'on ne tombe pas dans le piège de créer le carton 811 ce qui
	// n'optimise pas le nombre de cartons
	public final void testPiege2() {
		// Initialisation
		String entreeColis = "81127272";
		Integer nbOptimalCartons = 3;

		// Appel méthode
		String result = emballageService.emballerCartons(entreeColis, false);

		// Vértifications
		assertTrue(nbCartons(result) == nbOptimalCartons);
		assertTrue(verifieMemeNombreOccurenceEntreeSortie(entreeColis, result));
		assertTrue(verifieTailleMaxCartons(result));
	}

	// Test entrée vide
	public final void testCasLimite1() {
		// Initialisation
		String entreeColis = "";

		// Appel méthode
		String result = emballageService.emballerCartons(entreeColis, false);

		// Vértifications
		assertTrue(StringUtils.isEmpty(result));
	}

	@Test
	// Test entrée unique
	public final void testCasLimite2() {
		// Initialisation
		String entreeColis = "1";
		Integer nbOptimalCartons = 1;

		// Appel méthode
		String result = emballageService.emballerCartons(entreeColis, false);

		// Vértifications
		assertTrue(nbCartons(result) == nbOptimalCartons);
		assertTrue(verifieMemeNombreOccurenceEntreeSortie(entreeColis, result));
		assertTrue(verifieTailleMaxCartons(result));
	}

	@Test(expected = TailleColisException.class)
	// Test entrée en erreur
	public final void testCasErreur1() {
		// Initialisation
		String entreeColis = "654@654";

		// Appel méthode
		emballageService.emballerCartons(entreeColis, false);
	}

	@Test(expected = TailleColisException.class)
	// Test entrée en erreur
	public final void testCasErreur2() {
		// Initialisation
		String entreeColis = "108";

		// Appel méthode
		emballageService.emballerCartons(entreeColis, false);
	}

	// Vérifie qu'il y a les mêmes colis en entrée et en sortie
	private boolean verifieMemeNombreOccurenceEntreeSortie(String entree, String sortie) {
		boolean verificationOk = true;
		for (int i = 1; i < EmballageServiceImpl.TAILLE_COLIS_MAX; i++) {
			verificationOk = verificationOk && StringUtils.countMatches(entree,
					Integer.valueOf(i).toString()) == StringUtils.countMatches(sortie, Integer.valueOf(i).toString());
		}
		return verificationOk;
	}

	// Vérifie que la somme des tailles des colis du carton ne dépasse pas la taille
	// maximum
	private boolean verifieTailleMaxCartons(String cartons) {
		boolean verificationOk = true;
		String[] tabCartons = cartons.split("/");
		for (int i = 0; i < tabCartons.length; i++) {
			int somme = 0;
			for (int j = 0; j < tabCartons[i].length(); j++) {
				somme += Integer.parseInt(tabCartons[i].substring(j, j + 1));
			}
			verificationOk = verificationOk && somme <= EmballageServiceImpl.TAILLE_CARTON_MAX;
		}
		return verificationOk;
	}

	// Retourne le nombre de cartons
	private Integer nbCartons(String cartons) {
		return StringUtils.countMatches(cartons, "/") + 1;
	}

}
