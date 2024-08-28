/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.models.orm;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.hibernate.models.spi.SourceModelBuildingContext;

import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
public class PrimaryKeyJoinColumnsJpaAnnotation
		implements PrimaryKeyJoinColumns, RepeatableContainer<PrimaryKeyJoinColumn> {
	private PrimaryKeyJoinColumn[] value;
	private jakarta.persistence.ForeignKey foreignKey;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(SourceModelBuildingContext modelContext) {
		this.foreignKey = modelContext.getAnnotationDescriptorRegistry()
				.getDescriptor( jakarta.persistence.ForeignKey.class )
				.createUsage( modelContext );
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(
			PrimaryKeyJoinColumns annotation,
			SourceModelBuildingContext modelContext) {
		this.value = annotation.value();
		this.foreignKey = new ForeignKeyAnnotation( annotation.foreignKey(), modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(
			Map<String, Object> attributeValues,
			SourceModelBuildingContext modelContext) {
		this.value = (PrimaryKeyJoinColumn[]) attributeValues.get( "value" );
		this.foreignKey = (jakarta.persistence.ForeignKey) attributeValues.get( "foreignKey" );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return PrimaryKeyJoinColumns.class;
	}

	@Override
	public PrimaryKeyJoinColumn[] value() {
		return value;
	}

	public void value(PrimaryKeyJoinColumn[] value) {
		this.value = value;
	}


	@Override
	public jakarta.persistence.ForeignKey foreignKey() {
		return foreignKey;
	}

	public void foreignKey(jakarta.persistence.ForeignKey value) {
		this.foreignKey = value;
	}


}
