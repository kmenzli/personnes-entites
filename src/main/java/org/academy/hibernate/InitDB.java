package org.academy.hibernate;

import org.academy.hibernate.entities.Personne;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class InitDB {
	// constantes
	private final static String TABLE_NAME = "jpa01_personne";


	public static void main(String[] args) throws ParseException {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa");

		EntityManager em = emf.createEntityManager();

		EntityTransaction tx = em.getTransaction();
		tx.begin();
		System.out.print("####### avant DAO drop #####");
		em.createNativeQuery("delete from " + TABLE_NAME).executeUpdate();

		System.out.print("####### after DAO drop #####");

		Personne p1 = new Personne("Martin", "Paul", new SimpleDateFormat("dd/MM/yy").parse("31/01/2000"), true, 2);
		Personne p2 = new Personne("Durant", "Sylvie", new SimpleDateFormat("dd/MM/yy").parse("05/07/2001"), false, 0);

		em.persist(p1);
		em.persist(p2);

		System.out.println("[personnes]");
		for (Object p : em.createQuery("select p from Personne p order by p.nom asc").getResultList()) {
			System.out.println(p);
		}

		tx.commit();

		em.close();

		emf.close();

		System.out.println("terminé ...");

	}
}
