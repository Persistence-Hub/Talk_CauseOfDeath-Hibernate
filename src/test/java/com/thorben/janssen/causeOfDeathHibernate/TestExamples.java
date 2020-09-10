package com.thorben.janssen.causeOfDeathHibernate;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import com.thorben.janssen.causeOfDeathHibernate.dto.BookAuthorReview;
import com.thorben.janssen.causeOfDeathHibernate.model.Author;
import com.thorben.janssen.causeOfDeathHibernate.model.Book;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.annotations.QueryHints;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestExamples {

	Logger log = Logger.getLogger(this.getClass().getName());

	private EntityManagerFactory emf;

	@Before
	public void init() {
		emf = Persistence.createEntityManagerFactory("my-persistence-unit");
	}

	@After
	public void close() {
		emf.close();
	}

	@Test
	public void readOneAuthorWithBooks() {
		log.info("... readOneAuthorWithBooks ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		// Author a = em.find(Author.class, 1L);
		Author a = em.createQuery("SELECT a FROM Author a "
				+ "LEFT JOIN FETCH a.books b "
				+ "WHERE a.id = 1", Author.class)
				.getSingleResult();

		em.getTransaction().commit();
		em.close();

		log.info("Author " + a.getFirstName() + " " + a.getLastName() + " wrote "
				+ a.getBooks().stream().map(b -> b.getTitle()).collect(Collectors.joining(", ")));
	}

	@Test
	public void readAllAuthors() {
		log.info("... readAllAuthors ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		List<Author> authors = em.createQuery("SELECT a FROM Author a", Author.class).getResultList();

		em.getTransaction().commit();
		em.close();

		for (Author a : authors) {
			log.info("Author " + a.getFirstName() + " " + a.getLastName());
		}
	}

	@Test
	public void removeAuthorFromBook() {
		log.info("... removeAuthorFromBook ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Book b = em.find(Book.class, 6L);
		b.getAuthors().remove(b.getAuthors().toArray()[0]);

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void readBookWithAuthorsAndReviews() {
		log.info("... readBookWithAuthorsAndReviews ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Book b = em.createQuery(
				"SELECT b FROM Book b LEFT JOIN FETCH b.authors a LEFT JOIN FETCH b.reviews r WHERE b.id = 1",
				Book.class).getSingleResult();

		em.getTransaction().commit();
		em.close();

		log.info("Book "
				+ b.getTitle() + " was written by " + b.getAuthors().stream()
						.map(a -> a.getFirstName() + " " + a.getLastName()).collect(Collectors.joining(", "))
				+ " and got " + b.getReviews().size() + " reviews");

	}

	@Test
	public void readBookWithAuthorsAndReviewsDtos() {
		log.info("... readBookWithAuthorsAndReviewsDtos ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		BookAuthorReview b = em.createQuery(
				"SELECT new com.thorben.janssen.causeOfDeathHibernate.dto.BookAuthorReview(b.title, STRING_AGG(a.firstName || ' ' || a.lastName, ', '), count(r.id)) FROM Book b LEFT JOIN b.authors a LEFT JOIN b.reviews r WHERE b.id = 2 GROUP BY b.title",
				BookAuthorReview.class).getSingleResult();

		em.getTransaction().commit();
		em.close();

		log.info("Book " + b.getTitle() + " was written by " + b.getAuthorNames() + " and got " + b.getReviewCount()
				+ " reviews");

	}

	// @Test
	// public void removeAuthor() {
	// 	log.info("... removeAuthor ...");
	// 	EntityManager em = emf.createEntityManager();
	// 	em.getTransaction().begin();

	// 	log.info("Before removing Author 1");
	// 	logBooksOfAuthor2(em);

	// 	Author a = em.find(Author.class, 1L);
	// 	em.remove(a);

	// 	log.info("After removing Author 1");
	// 	logBooksOfAuthor2(em);

	// 	em.getTransaction().commit();
	// 	em.close();
	// }

	@Test
	public void cacheBookWithAuthorsAndReviews() {
		log.info("... cacheBookWithAuthorsAndReviews ...");

		// 1st iteration - Execute query and store result in cache
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		TypedQuery<Book> q = em.createQuery(
				"SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors a LEFT JOIN FETCH b.reviews r",
				Book.class);
		q.setHint(QueryHints.CACHEABLE, true);
		List<Book> books = q.getResultList();

		em.getTransaction().commit();
		em.close();

		log.info("2nd iteration");

		// 2nd iteration - Query result should be cached
		em = emf.createEntityManager();
		em.getTransaction().begin();

		q = em.createQuery(
				"SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors a LEFT JOIN FETCH b.reviews r",
				Book.class);
		q.setHint(QueryHints.CACHEABLE, true);
		books = q.getResultList();
		log.info("Initialize associations");
		for (Book b : books) {
			Hibernate.initialize(b.getAuthors());
			Hibernate.initialize(b.getReviews());
		}

		em.getTransaction().commit();
		em.close();

		for (Book b : books) {
			log.info("Book " + b.getTitle() 
				+ " was written by "
				+ b.getAuthors().stream().map(a -> a.getFirstName()+" "+a.getLastName()).collect(Collectors.joining(", "))
				+ " and got " + b.getReviews().size()+" reviews");
		}

	}

	@Test
	public void cacheBookWithAuthorsAndReviewsDtos() {
		log.info("... cacheBookWithAuthorsAndReviewsDtos ...");

		// 1st iteration - Execute query and store result in cache
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		TypedQuery<BookAuthorReview> q = em.createQuery(
				"SELECT new com.thorben.janssen.causeOfDeathHibernate.dto.BookAuthorReview(b.title, STRING_AGG(a.firstName || ' ' || a.lastName, ', '), count(r.id)) FROM Book b LEFT JOIN b.authors a LEFT JOIN b.reviews r GROUP BY b.title",
				BookAuthorReview.class);
		q.setHint(QueryHints.CACHEABLE, true);
		List<BookAuthorReview> books = q.getResultList();

		em.getTransaction().commit();
		em.close();

		// 2nd iteration - Query result should be cached
        em = emf.createEntityManager();
		em.getTransaction().begin();

		q = em.createQuery(
				"SELECT new com.thorben.janssen.causeOfDeathHibernate.dto.BookAuthorReview(b.title, STRING_AGG(a.firstName || ' ' || a.lastName, ', '), count(r.id)) FROM Book b LEFT JOIN b.authors a LEFT JOIN b.reviews r GROUP BY b.title",
				BookAuthorReview.class);
		q.setHint(QueryHints.CACHEABLE, true);
		books = q.getResultList();

		em.getTransaction().commit();
		em.close();

		for (BookAuthorReview b : books) {
			log.info("Book " + b.getTitle() + " was written by " + b.getAuthorNames() + " and got " + b.getReviewCount()
					+ " reviews");
		}
	}

	private void logBooks(EntityManager em) {
		log.info("##########  Books  ################");
		List<Book> books = em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
		for (Book b : books) {
			log.info(b);
		}
		em.clear();
	}

	private void logBooksOfAuthor2(EntityManager em) {
		log.info("##########  Books  ################");
		List<Book> books = em.createQuery("SELECT DISTINCT b FROM Book b JOIN b.authors a JOIN FETCH b.authors a2 WHERE a.id =2", Book.class).getResultList();
		for (Book b : books) {
			log.info(b + " was written by " + b.getAuthors().stream().map(a -> a.getId().toString()).collect(Collectors.joining(", ")));
		}
		em.clear();
	}

	//
	// DATA INCONSISTENCIES
	//

	@Test
	public void readAuthorsOfBook2() {
		log.info("... readAuthorsOfBook2 ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		// List<Author> authors = em.createQuery("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books b WHERE b.id = 2", Author.class).getResultList();
		List<Author> authors = em.createQuery("SELECT DISTINCT a FROM Author a LEFT JOIN a.books b LEFT JOIN FETCH a.books WHERE b.id = 2", Author.class).getResultList();

		em.getTransaction().commit();
		em.close();

		for (Author a : authors) {
			log.info("Author " + a.getFirstName() + " " + a.getLastName() + " wrote "
					+ a.getBooks().stream().map(b -> b.getTitle()).collect(Collectors.joining(", ")));
		}
	}

	@Test
	public void jpqlUpdate() {
		log.info("... jpqlUpdate ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		em.createQuery("SELECT a FROM Author a", Author.class).getResultList();

		em.flush();
		em.clear();

		em.createQuery("UPDATE Author a SET a.firstName = upper(a.firstName), a.lastName = upper(a.lastName)").executeUpdate();

		List<Author> authors = em.createQuery("SELECT a FROM Author a", Author.class).getResultList();

		em.getTransaction().commit();
		em.close();

		for (Author a : authors) {
			log.info("Author " + a.getFirstName() + " " + a.getLastName());
		}
	}

	@Test
	public void manageBidirectionalAssociations() {
		log.info("... manageBidirectionalAssociations ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Author a = em.find(Author.class, 1L);
		Book b = em.find(Book.class, 1L);

        // a.getBooks().remove(b);
		// b.getAuthors().remove(a);
		b.removeAuthor(a);

		log.info("Author "+a.getFirstName()+" "+a.getLastName()+" wrote "+
			a.getBooks().stream().map(book -> book.getTitle()).collect(Collectors.joining(", ")));
		
		log.info("The book "+b.getTitle()+" was written by "+
			b.getAuthors().stream().map(author -> author.getFirstName()+" "+a.getLastName()).collect(Collectors.joining(", ")));
		
		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void openSessionInView() {
		log.info("... openSessionInView ...");
		EntityManager em1 = emf.createEntityManager();
		em1.getTransaction().begin();

		Object[] r = (Object[]) em1.createQuery("SELECT a, count(b.id) FROM Author a JOIN a.books b WHERE a.id=1 GROUP BY a.id").getSingleResult();
		Author a = (Author) r[0];
		long bookCount = (long) r[1];
		
		parallelTransaction();

		em1.getTransaction().commit();
		log.info("Transaction committed");

		log.info("Author "+a.getFirstName()+" "+a.getLastName()+" wrote "+bookCount+" books.");
		log.info("These are: "+a.getBooks().stream().map(book -> book.getTitle()).collect(Collectors.joining(", ")));

		em1.close();
	}

	private void parallelTransaction() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Author a = em.find(Author.class, 1L);
		Book b = em.find(Book.class, 1L);

		a.getBooks().remove(b);
		b.getAuthors().remove(a);
		
		em.getTransaction().commit();
		em.close();
	}

	//
	// LOST DATA
	//

	@Test
	public void clearWithoutFlush() {
		log.info("... clearWithoutFlush ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		boolean loop = true;
		int firstResult = 0;
		int maxResults = 5;
		while(loop) {
			TypedQuery<Book> q = em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class);
			q.setFirstResult(firstResult);
			q.setMaxResults(maxResults);
			List<Book> books = q.getResultList();
			
			int i = 0;
			for (Book b : books) {
				b.setTitle(b.getTitle().toUpperCase());

				i++;
				if (i%5==0) {
					em.flush();
					em.clear();
				}
			}

			if (books.size() == maxResults) {
				firstResult = firstResult+maxResults;
			} else {
				loop = false;
			}
		}

		List<Book> bs = em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
		for (Book b : bs) {
			log.info(b.getTitle());
		}

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void removeAuthorsAndBooks() {
		log.info("... removeAuthorsAndBooks ...");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		log.info("Before removing Author 1");
		logBooks(em);
		// logBooksOfAuthor2(em);

		Author a = em.find(Author.class, 1L);
		em.remove(a);

		log.info("After removing Author 1");
		logBooks(em);
		// logBooksOfAuthor2(em);

		em.getTransaction().commit();
		em.close();
	}
}
