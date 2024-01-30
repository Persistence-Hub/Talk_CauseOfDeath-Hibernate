package com.thorben.janssen.causeOfDeathHibernate.model;

import java.util.Arrays;
import java.util.Objects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
// @Cacheable
public class Manuscript {

	@Id
	private Long id;
	
	private byte[] file;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id")
	@MapsId
	private Book book;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Manuscript other = (Manuscript) obj;

		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Manuscript [id=" + id + ", file=" + Arrays.toString(file) + "]";
	}
}
