package sample.vsct.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sample.vsct.exception.TailleColisException;

public class EmballageServiceImpl implements EmballageService {
	public static final Integer TAILLE_CARTON_MAX = 10;
	public static final Integer TAILLE_COLIS_MAX = 9;
	public static final Integer TAILLE_COLIS_MIN = 1;

	final static Logger logger = Logger.getLogger(EmballageServiceImpl.class);

	@Override
	public String emballerCartons(String entreeTaillesColis, boolean affichageOptimisee) {
		logger.debug("Début de l'exécution de la méthode emballerCartons");
		Long dateDebutMethode = new Date().getTime();
		String afficherListeCartons = null;

		// Création d'une structure de données plus simple liant la taille et le nombre
		// de colis pour cette taille
		Map<Integer, Integer> mapNbColisParTaille = creerMapNbColisParTaille(entreeTaillesColis);

		// Récupère la liste de toutes les combinaisons de taille possibles, pour chaque
		// taille de cartons,
		// indépendamment des colis en entrée
		Map<Integer, List<List<Integer>>> mapTailleTotalListeCombinaisons = creerMapCombinaisonsTaille(
				TAILLE_CARTON_MAX);

		// Trie les combinaisons afin de prioriser les cartons avec les colis les plus
		// emcombrants et ainsi minimiser le nombre de cartons
		trierListeCombinaisonsTaille(mapTailleTotalListeCombinaisons);

		// Réalise les cartons à partir de la liste des combinaisons et des taille de
		// colis
		Map<String, Integer> mapCartonsEmballes = faireCartons(mapNbColisParTaille, mapTailleTotalListeCombinaisons);

		// Récupère la liste des cartons à afficher à l'utilisateur
		afficherListeCartons = afficherListeCartons(mapCartonsEmballes, affichageOptimisee);

		Long dateFinMethode = new Date().getTime();
		logger.debug(String.format("Fin de l'exécution de la méthode emballerCartons en %d %s",
				dateFinMethode - dateDebutMethode, "ms"));
		return afficherListeCartons;
	}

	/**
	 * Créer une map. clé : la taille. Valeur : le nombre de colis pour cette taille
	 * 
	 * @param entreeTaillesColis
	 *            Taille des colis entrant. Exemple : "354218465154"
	 * @return Une map avec pour clé la taille et pour valeur, le nombre de colis
	 *         ayant cette taille
	 */
	private Map<Integer, Integer> creerMapNbColisParTaille(String entreeTaillesColis) {
		verifieEntree(entreeTaillesColis);
		logger.debug("Création de la map avec le nombre de colis pour chaque taille");
		// Initialisation de la structure de données
		Map<Integer, Integer> retour = new HashMap<Integer, Integer>();
		for (int i = 1; i <= TAILLE_CARTON_MAX; i++) {
			retour.put(i, 0);
		}

		// On parcours chaque carton pour ajouter son taille à la map
		for (int i = 0; i < entreeTaillesColis.length(); i++) {
			Integer tailleCartonActuel = Character.getNumericValue(entreeTaillesColis.charAt(i));
			retour.put(tailleCartonActuel, retour.get(tailleCartonActuel) + 1);
		}

		return retour;
	}

	/**
	 * Vérifie que les données d'entrée sont au bon format
	 * 
	 * @param entreeTaillesColis
	 *            Taille des colis en entrée
	 * @throws TailleColisException
	 */
	private void verifieEntree(String entreeTaillesColis) {
		logger.debug("Vérification des tailles des colis");
		for (int i = 0; i < entreeTaillesColis.length(); i++) {
			Integer tailleColis;
			String tailleColisString = entreeTaillesColis.substring(i, i + 1);
			try {
				tailleColis = Integer.parseInt(tailleColisString);
			} catch (NumberFormatException e) {
				String messageErreur = String.format(
						"Le colis '%s' doit être enregistré avec sa taille au format nombre", tailleColisString);
				throw new TailleColisException(messageErreur);
			}
			if (tailleColis < TAILLE_COLIS_MIN || tailleColis > TAILLE_COLIS_MAX) {
				String messageErreur = String.format(
						"La taille d'un colis vaut %d alors qu'il doit être compris entre %d et %d", tailleColis,
						TAILLE_COLIS_MIN, TAILLE_COLIS_MAX);
				throw new TailleColisException(messageErreur);
			}
		}
	}

	/**
	 * Faire toutes les combinaisons de taille de colis pour une taille totale du
	 * carton donnée en paramètre
	 * 
	 * @param tailleCartonMax
	 * @return Une map avec pour clé, le taille totale du carton et pour valeur, la
	 *         liste des combinaisons de taille de colis qui ont pour total la clé
	 *         de la map. Exemple : Clé 3 (taille total cartons). Valeur : <<3>,
	 *         <2,1>, <1,1,1>> (liste des taille des colis). Clé 2. Valeur <2>,
	 *         <1,1>
	 */
	private Map<Integer, List<List<Integer>>> creerMapCombinaisonsTaille(Integer tailleCartonMax) {
		logger.debug("Calcul de la liste des combinaisons de taille de colis pour chaque taille de carton");
		// Initilisation de la map de retour avec le taille total 0 (qui aura pour clé
		// une liste contenant une liste vide)
		Map<Integer, List<List<Integer>>> mapTailleTotalListeCombinaisons = new HashMap<Integer, List<List<Integer>>>();
		List<List<Integer>> listeVide = new ArrayList<List<Integer>>();
		listeVide.add(new ArrayList<Integer>());
		mapTailleTotalListeCombinaisons.put(0, listeVide);

		// On parcours toutes les tailles totales de cartons jusqu'au tailleCartonMax.
		// Ils seront les clés de la map de retour.
		for (int tailleTotalCartonCourant = 1; tailleTotalCartonCourant <= tailleCartonMax; tailleTotalCartonCourant++) {
			List<List<Integer>> listeCombinaisonsTailleCourant = new ArrayList<List<Integer>>();
			/*
			 * On parcours tous les tailles de colis de 1 jusqu'à tailleTotalCartonCourant.
			 * Le but étant de construire la clé valeur courante à partir de la clé -valeur
			 * précédente. Par exemple. Clé 1. Valeur <1>. Clé 2. Valeur : <2> + <1, toutes
			 * les combinaisons de 1> soit <2> + <1,1>. Clé 3 : Valeur : <3>, <2, toutes les
			 * combinaisons de 1>, <1, toutes les combinaisons de 2> soit <3>, <2,1>,
			 * <1,1,1>. En sachant que nous avons retiré les doublons (<1,2> n'a pas été
			 * pris en compte)
			 */
			for (int tailleColisCourant = 1; tailleColisCourant <= tailleTotalCartonCourant; tailleColisCourant++) {
				// On clone les combinaisons pour une taille inférieure à la taille courante
				List<List<Integer>> listeCombinaisonsCourant = clonerListeCombinaisonsTaille(
						mapTailleTotalListeCombinaisons.get(tailleTotalCartonCourant - tailleColisCourant));
				ListIterator<List<Integer>> listeCombinaisonsIterator = listeCombinaisonsCourant.listIterator();
				while (listeCombinaisonsIterator.hasNext()) {
					List<Integer> listeCombinaisons = listeCombinaisonsIterator.next();
					// On ajoute la combinaison sauf si on créer un doublons. Par défaut, nous
					// récupérerons les taille dans l'ordre croissant. Par exemple <2,1> mais pas
					// <1,2>
					if (listeCombinaisons.size() == 0
							|| listeCombinaisons.get(listeCombinaisons.size() - 1) >= tailleColisCourant) {
						listeCombinaisons.add(tailleColisCourant);
					} else {
						listeCombinaisonsIterator.remove();
					}
				}

				listeCombinaisonsTailleCourant.addAll(listeCombinaisonsCourant);
			}

			mapTailleTotalListeCombinaisons.put(tailleTotalCartonCourant, listeCombinaisonsTailleCourant);
		}

		return mapTailleTotalListeCombinaisons;
	}

	/**
	 * Trie les listes de combinaisons entre elles. Les combinaisons avec le minimum
	 * de colis seront prioritaires pour faire un carton (car ils contiendront les
	 * colis les plus "emcombrants"). Par exemple, si on a les colis suivants :
	 * "2222288888". Alors il ne faut pas commencer par faire un carton <22222>,
	 * sinon nous auront 6 cartons (1 avec <22222> et 5 avec <8>) alors que nous
	 * pouvions en faire 5 (<82>). De même, si nous avons les colis suivants
	 * "81127272", alors il ne faut pas commencer par faire un cartons <811>, sinon
	 * nous aurons les cartons <72><72><2> soit 4 cartons. Alors que la version
	 * optimisé est <82><721><721>
	 * 
	 * @param mapTailleTotalListeCombinaisons
	 */
	private void trierListeCombinaisonsTaille(Map<Integer, List<List<Integer>>> mapTailleTotalListeCombinaisons) {
		logger.debug("Tri de la liste des combinaison de taille de colis");
		for (List<List<Integer>> listeCombinaisonsTaille : mapTailleTotalListeCombinaisons.values()) {
			Collections.sort(listeCombinaisonsTaille, new Comparator<List<Integer>>() {
				@Override
				public int compare(List<Integer> combinaison1, List<Integer> combinaison2) {
					return combinaison1.size() - combinaison2.size();
				}
			});
		}
	}

	/**
	 * Cloner la liste des combinaisons de taille
	 * 
	 * @param listeCombinaisonTaille
	 *            liste des combinaison de taille de colis à cloner
	 * @return liste des combinaison de taille de colis cloné
	 */
	private List<List<Integer>> clonerListeCombinaisonsTaille(List<List<Integer>> listeCombinaisonTaille) {
		List<List<Integer>> listeCombinaisonTailleClone = new ArrayList<List<Integer>>();
		// Parcours les combinaisons de taille
		for (List<Integer> combinaison : listeCombinaisonTaille) {
			List<Integer> combinaisonTailleClone = new ArrayList<Integer>();
			// Parcours les taille d'une combinaison
			for (Integer tailleColisCourant : combinaison) {
				combinaisonTailleClone.add(Integer.valueOf(tailleColisCourant.intValue()));
			}
			listeCombinaisonTailleClone.add(combinaisonTailleClone);
		}

		return listeCombinaisonTailleClone;
	}

	/**
	 * Réalise les cartons à partir des colis entrants et des listes de combinaisons
	 * de taille possibles. On privilégiera les cartons les plus lourds possibles
	 * sans dépasser la taille maximum.
	 * 
	 * @param mapTailleColis
	 *            cartons entrants sous forme de map. Clé : taille des colis. Valeur
	 *            : nombre de colis ayant ce taille
	 * @param mapCombinaisonsTaille
	 *            map des combinaisons de taille. Clé : taille totale cartons.
	 *            Valeur : Liste des combinaisons possibles de taille
	 * @return La map des taille des cartons. Clé : combinaisons de taille. Valeur :
	 *         Nombre de cartons avec cette combinaison de taille.
	 */
	private Map<String, Integer> faireCartons(Map<Integer, Integer> mapNbColisParTaille,
			Map<Integer, List<List<Integer>>> mapTailleTotalListeCombinaisons) {
		logger.debug("Emballage des colis dans les cartons");
		Map<String, Integer> cartons = new HashMap<String, Integer>();

		for (int tailleCartonCourant = TAILLE_CARTON_MAX; tailleCartonCourant > 0; tailleCartonCourant--) {
			List<List<Integer>> listeCombinaisonsTaille = mapTailleTotalListeCombinaisons.get(tailleCartonCourant);
			for (List<Integer> combinaisonCourante : listeCombinaisonsTaille) {
				// On regarde combien de cartons on peut faire avec cette combinaison de taille.
				// Le nombre de cartons correspond au nombre minimum de colis pour les tailles
				// de la combinaison
				Integer nbCartonsCombinaison = Integer.MAX_VALUE;
				for (Integer TailleCourante : combinaisonCourante) {
					Integer nbCartonTailleCourante = mapNbColisParTaille.get(TailleCourante)
							/ Collections.frequency(combinaisonCourante, TailleCourante);
					if (nbCartonTailleCourante < nbCartonsCombinaison) {
						nbCartonsCombinaison = nbCartonTailleCourante;
					}
				}
				if (nbCartonsCombinaison > 0) {
					StringBuilder stringBuilder = new StringBuilder();
					for (Integer tailleCourante : combinaisonCourante) {
						stringBuilder.append(tailleCourante.toString());
						// ON retire les colis de la liste des colis à traiter
						mapNbColisParTaille.put(tailleCourante,
								mapNbColisParTaille.get(tailleCourante) - nbCartonsCombinaison);
					}
					cartons.put(stringBuilder.toString(), nbCartonsCombinaison);
				}
			}
		}
		return cartons;
	}

	/**
	 * Construire une chaine de caractère avec tous les cartons. Par exemple : 91/55
	 * 
	 * @param cartons
	 * @param affichageOptimisee
	 * @return
	 */
	private String afficherListeCartons(Map<String, Integer> cartons, boolean affichageOptimisee) {
		logger.debug("Affichage des cartons");
		StringBuilder stringBuilder = new StringBuilder();
		// On récupère la chaine pour chaque combinaison de taille
		for (Entry<String, Integer> combinaisonTailleCourante : cartons.entrySet()) {
			stringBuilder.append(afficherCarton(combinaisonTailleCourante, affichageOptimisee));
		}
		// On retire le dernier "/" s'il existe
		if (stringBuilder.length() != 0
				&& stringBuilder.substring(stringBuilder.length() - 1, stringBuilder.length()).equals("/")) {
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		}
		logger.debug(String.format("liste des cartons emballés : %s", stringBuilder.toString()));
		return stringBuilder.toString();
	}

	/**
	 * Récupère la chaine de caractère d'une combinaison de taillen spécifique. Par
	 * exemple : 91/91/ (91(2) pour l'affichage optimisée)
	 * 
	 * @param combinaisonTailleCourante combinaison de taille. Par exemple <"91", 2>
	 * @param affichageOptimisee indique si on affiche les cartons de manière optimisée
	 * @return la chaine de caractère du carton
	 */
	private String afficherCarton(Entry<String, Integer> combinaisonTailleCourante, boolean affichageOptimisee) {
		StringBuilder stringBuilder = new StringBuilder();
		if (affichageOptimisee) {
			// Affichage optimisée (Exemple 91(2)/)
			stringBuilder.append(combinaisonTailleCourante.getKey()).append("(")
					.append(combinaisonTailleCourante.getValue()).append(")").append("/");
		} else {
			// Affichage optimisée (Exemple 91/91/)
			for (int i = 0; i < combinaisonTailleCourante.getValue(); i++) {
				stringBuilder.append(combinaisonTailleCourante.getKey()).append("/");
			}
		}
		return stringBuilder.toString();
	}

}
