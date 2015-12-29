package org.academy.hibernate;

import org.academy.hibernate.entities.Personne;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@SuppressWarnings("unchecked")
public class Main {


	private final static String TABLE_NAME = "jpa01_personne";


	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa");

	private static EntityManager em = null;


	private static Personne p1, p2, newp1;

	public static void main(String[] args) throws Exception {
		// nettoyage base
		log("clean");
		clean();

		// dump
		dump();

		// test1
		log("test1");
		test1();

		// test2
		log("test2");
		test2();

		// test3
		log("test3");
		test3();

		// test4
		log("test4");
		test4();

		// test5
		log("test5");
		test5();

		// test6
		log("test6");
		test6();

		// test7
		log("test7");
		test7();

		// test8
		log("test8");
		test8();

		// test9
		log("test9");
		test9();

		// test10
		log("test10");
		test10();

		// test11
		log("test11");
		test11();

//		// test12
//		log("test12");
//		test12();

		// fin contexte de persistance
		if (em.isOpen())
			em.close();

		// fermeture EntityManagerFactory
		emf.close();
	}

	// r�cup�rer l'EntityManager courant
	private static EntityManager getEntityManager() {
		if (em == null || !em.isOpen()) {
			em = emf.createEntityManager();
		}
		return em;
	}

	// r�cup�rer un EntityManager neuf
	private static EntityManager getNewEntityManager() {
		if (em != null && em.isOpen()) {
			em.close();
		}
		em = emf.createEntityManager();
		return em;
	}

	// affichage contenu table
	private static void dump() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// affichage personnes
		System.out.println("[personnes]");
		for (Object p : em.createQuery("select p from Personne p order by p.nom asc").getResultList()) {
			System.out.println(p);
		}
		// fin transaction
		tx.commit();
	}

	// raz BD
	private static void clean() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// supprimer les �l�ments de la table PERSONNES
		em.createNativeQuery("delete from " + TABLE_NAME).executeUpdate();
		// fin transaction
		tx.commit();
	}

	// logs
	private static void log(String message) {
		System.out.println("main : ----------- " + message);
	}

	// cr�ation d'objets
	public static void test1() throws ParseException {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// cr�ation personnes
		p1 = new Personne("Martin", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
		p2 = new Personne("Durant", "Sylvie", new SimpleDateFormat("dd/MM/yy").parse("05/07/2001"), false, 0);
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// persistance des personnes
		em.persist(p1);
		em.persist(p2);
		// fin transaction
		tx.commit();
		// on affiche la table
		dump();
	}

	// modifier un objet du contexte
	public static void test2() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on incr�mente le nbre d'enfants de p1
		p1.setNbenfants(p1.getNbenfants() + 1);
		// on modifie son �tat marital
		p1.setMarie(false);
		// l'objet p1 est automatiquement sauvegard� (dirty checking)
		// lors de la prochaine synchronisation (commit ou select)
		// fin transaction
		tx.commit();
		// on affiche la nouvelle table
		dump();
	}

	// demander des objets
	public static void test3() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on demande la personne p1
		Personne p1b = em.find(Personne.class, p1.getId());
		// parce que p1 est d�j� dans le contexte de persistance, il n'y a pas eu d'acc�s � la base
		// p1b et p1 sont les m�mes r�f�rences
		System.out.format("p1==p1b ? %s%n", p1 == p1b);
		// demander un objet qui n'existe pas rend 1 pointeur null
		Personne px = em.find(Personne.class, -4);
		System.out.format("px==null ? %s%n", px == null);
		// fin transaction
		tx.commit();
	}

	// supprimer un objet appartenant au contexte de persistance
	public static void test4() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on supprime l'objet attach� p2
		em.remove(p2);
		// fin transaction
		tx.commit();
		// on affiche la nouvelle table
		dump();
	}

	// d�tacher, r�attacher et modifier
	public static void test5() {
		// nouveau contexte de persistance
		EntityManager em = getNewEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// p1 d�tach�
		Personne oldp1 = p1;
		// on r�attache p1 au nouveau contexte
		p1 = em.find(Personne.class, p1.getId());
		// v�rification
		System.out.format("p1==oldp1 ? %s%n", p1 == oldp1);
		// fin transaction
		tx.commit();
		// on incr�mente le nbre d'enfants de p1
		p1.setNbenfants(p1.getNbenfants() + 1);
		// on affiche la nouvelle table
		dump();
	}

	// supprimer un objet n'appartenant pas au contexte de persistance
	public static void test6() {
		// nouveau contexte de persistance
		EntityManager em = getNewEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on supprime p1 qui n'appartient pas au nouveau contexte
		try {
			em.remove(p1);
			// fin transaction
			tx.commit();
		} catch (RuntimeException e1) {
			System.out.format("Erreur � la suppression de p1 : [%s,%s]%n", e1.getClass().getName(), e1.getMessage());
			// on fait un rollback de la transaction
			try {
				if (tx.isActive())
					tx.rollback();
			} catch (RuntimeException e2) {
				System.out.format("Erreur au rollback [%s,%s]%n", e2.getClass().getName(), e2.getMessage());
			}
		}
		// on affiche la nouvelle table
		dump();
	}

	// modifier un objet n'appartenant pas au contexte de persistance
	public static void test7() {
		// nouveau contexte de persistance
		EntityManager em = getNewEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on incr�mente le nbre d'enfants de p1 qui n'appartient pas au nouveau contexte
		p1.setNbenfants(p1.getNbenfants() + 1);
		// fin transaction
		tx.commit();
		// on affiche la nouvelle table - elle n'a pas du changer
		dump();
	}

	// r�attacher un objet au contexte de persistance
	public static void test8() {
		// nouveau contexte de persistance
		EntityManager em = getNewEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on r�attache l'objet au contexte
		newp1 = em.merge(p1);
		// c'est newp1 qui fait d�sormais partie du contexte, pas p1
		// fin transaction
		tx.commit();
		// on affiche la nouvelle table - le nbre d'enfants de p1 a du changer
		dump();
	}

	// une requ�te select provoque une synchronisation
	// de la base avec le contexte de persistance
	public static void test9() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// on incr�mente le nbre d'enfants de newp1
		newp1.setNbenfants(newp1.getNbenfants() + 1);
		// affichage personnes - le nbre d'enfants de newp1 a du changer
		System.out.println("[personnes]");
		for (Object p : em.createQuery("select p from Personne p order by p.nom asc").getResultList()) {
			System.out.println(p);
		}
		// fin transaction
		tx.commit();
	}

	// contr�le de version (optimistic locking)
	public static void test10() {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		// incr�menter la version de newp1 directement dans la base (native query)
		em.createNativeQuery(String.format("update %s set VERSION=VERSION+1 WHERE ID=%d", TABLE_NAME, newp1.getId())).executeUpdate();
		// fin transaction
		tx.commit();
		// d�but nouvelle transaction
		tx = em.getTransaction();
		tx.begin();
		// on incr�mente le nbre d'enfants de newp1
		newp1.setNbenfants(newp1.getNbenfants() + 1);
		// fin transaction - elle doit �chouer car newp1 n'a plus la bonne version
		try {
			tx.commit();
		} catch (RuntimeException e1) {
			System.out.format("Erreur lors de la mise � jour de newp1 [%s,%s,%s,%s]%n", e1.getClass().getName(), e1.getMessage(), e1.getCause().getClass()
					.getName(), e1.getCause().getMessage());
			// on fait un rollback de la transaction
			try {
				if (tx.isActive())
					tx.rollback();
			} catch (RuntimeException e2) {
				System.out.format("Erreur au rollback [%s,%s]%n", e2.getClass().getName(), e2.getMessage());
			}
		}
		// on ferme le contexte qui n'est plus � jour
		em.close();
		// dump de la table - la version de p1 a du changer
		dump();
	}

	// rollback d'une transaction
	public static void test11() throws ParseException {
		// contexte de persistance
		EntityManager em = getEntityManager();
		// d�but transaction
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			// on r�attache p1
			p1 = em.find(Personne.class, p1.getId());
			// on incr�mente le nbre d'enfants de p1
			p1.setNbenfants(p1.getNbenfants() + 1);
			// affichage personnes - le nbre d'enfants de p1 a du changer
			System.out.println("[personnes]");
			for (Object p : em.createQuery("select p from Personne p order by p.nom asc").getResultList()) {
				System.out.println(p);
			}
			// cr�ation de 2 personnes de nom identique, ce qui est interdit par la DDL
			Personne p3 = new Personne("X", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
			Personne p4 = new Personne("X", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
			// persistance des personnes
			em.persist(p3);
			em.persist(p4);
			// fin transaction
			tx.commit();
		} catch (RuntimeException e1) {
			// on a eu un pb
			System.out.format("Erreur dans transaction [%s,%s,%s,%s,%s,%s]%n", e1.getClass().getName(), e1.getMessage(),
					e1.getCause().getClass().getName(), e1.getCause().getMessage(), e1.getCause().getCause().getClass().getName(), e1.getCause().getCause()
							.getMessage());
			try {
				if (tx.isActive())
					tx.rollback();
			} catch (RuntimeException e2) {
				System.out.format("Erreur au rollback [%s]%n", e2.getMessage());
			}
			// on abandonne le contexte courant
			em.clear();
		}
		// dump - la table n'a pas du changer � cause du rollback
		dump();
	}

	// on refait la m�me chose mais sans les transactions
	// on obtient le m�me r�sultat qu'auparavant avec les SGBD : FIREBIRD, ORACLE XE, POSTGRES, MYSQL5
	// avec SQLSERVER on a une table vide. La connexion est laiss�e dans un �tat qui emp�che la r�ex�cution
	// du programme. Il faut alors relancer le serveur.
	// idem avec le SGBD Derby
	// HSQL ins�re la 1�re personne - il n'y a pas de rollback
	public static void test12() throws ParseException {
		// on r�attache p1
		p1 = em.find(Personne.class, p1.getId());
		// on incr�mente le nbre d'enfants de p1
		p1.setNbenfants(p1.getNbenfants() + 1);
		// affichage personnes - le nbre d'enfants de p1 a du changer
		System.out.println("[personnes]");
		for (Object p : em.createQuery("select p from Personne p order by p.nom asc").getResultList()) {
			System.out.println(p);
		}
		// cr�ation de 2 personnes de nom identique, ce qui est interdit par la DDL
		Personne p3 = new Personne("X", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
		Personne p4 = new Personne("X", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
		// persistance des personnes
		em.persist(p3);
		em.persist(p4);
		// dump qui va provoquer la synchro du contexte em avec la BD
		try {
			dump();
		} catch (RuntimeException e3) {
			System.out.format("Erreur dans dump [%s,%s,%s,%s]%n", e3.getClass().getName(), e3.getMessage(), e3.getCause().getClass().getName(), e3
					.getCause().getMessage());
		}
		// on ferme le contexte actuel
		em.close();
		// dump
		dump();
	}

}