/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.rendering.spi;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hibernate.models.spi.AnnotationDescriptor;
import org.hibernate.models.spi.AnnotationTarget;
import org.hibernate.models.spi.AttributeDescriptor;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.FieldDetails;
import org.hibernate.models.spi.MethodDetails;
import org.hibernate.models.spi.RecordComponentDetails;
import org.hibernate.models.spi.ModelsContext;

import static org.hibernate.models.internal.AnnotationHelper.extractValue;

/**
 * Base support for Renderer implementations.  Handles rendering of annotations.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractRenderer implements Renderer {
	protected abstract RenderingTarget getRenderingTarget();

	@Override
	public void renderClass(ClassDetails classDetails, ModelsContext context) {
		renderDirectAnnotations( classDetails, context );
		renderClassDetails( classDetails, context );
	}

	private void renderDirectAnnotations(AnnotationTarget annotationTarget, ModelsContext context) {
		annotationTarget.forEachDirectAnnotationUsage( (usage) -> renderAnnotation( usage, context ) );
	}

	protected abstract void renderClassDetails(ClassDetails classDetails, ModelsContext context);

	@Override
	public void renderField(FieldDetails fieldDetails, ModelsContext context) {
		renderDirectAnnotations( fieldDetails, context );
		renderFieldDetails( fieldDetails, context );
	}

	protected abstract void renderFieldDetails(FieldDetails fieldDetails, ModelsContext context);

	@Override
	public void renderMethod(MethodDetails methodDetails, ModelsContext context) {
		renderDirectAnnotations( methodDetails, context );
		renderMethodDetails( methodDetails, context );
	}

	protected abstract void renderMethodDetails(MethodDetails methodDetails, ModelsContext context);

	@Override
	public void renderRecordComponent(RecordComponentDetails recordComponentDetails, ModelsContext context) {
		renderDirectAnnotations( recordComponentDetails, context );
		renderRecordComponentDetails( recordComponentDetails, context );
	}

	protected abstract void renderRecordComponentDetails(RecordComponentDetails recordComponentDetails, ModelsContext context);


	@Override
	public <A extends Annotation> void renderAnnotation(A annotation, ModelsContext context) {
		//noinspection unchecked
		final AnnotationDescriptor<A> descriptor = (AnnotationDescriptor<A>) context.getAnnotationDescriptorRegistry().getDescriptor( annotation.annotationType() );
		final List<AttributeDescriptor<?>> attributes = descriptor.getAttributes();

		if ( attributes.isEmpty() ) {
			getRenderingTarget().addLine( "@%s", descriptor.getAnnotationType().getName() );
		}
		else {
			getRenderingTarget().addLine( "@%s(", descriptor.getAnnotationType().getName() );
			getRenderingTarget().indent( 2 );

			attributes.forEach( (attribute) -> attribute.getTypeDescriptor().render(
					attribute.getName(),
					extractValue( annotation, attribute ),
					getRenderingTarget(),
					this,
					context
			) );

			getRenderingTarget().unindent( 2 );
			getRenderingTarget().addLine( ")" );
		}
	}

	@Override
	public <A extends Annotation> void renderNestedAnnotation(String name, A annotation, ModelsContext context) {
		//noinspection unchecked
		final AnnotationDescriptor<A> descriptor = (AnnotationDescriptor<A>) context.getAnnotationDescriptorRegistry().getDescriptor( annotation.annotationType() );
		final List<AttributeDescriptor<?>> attributes = descriptor.getAttributes();

		getRenderingTarget().addLine( "%s = @%s(", name, descriptor.getAnnotationType().getName() );
		getRenderingTarget().indent( 2 );

		attributes.forEach( (attribute) -> attribute.getTypeDescriptor().render(
				attribute.getName(),
				extractValue( annotation, attribute ),
				getRenderingTarget(),
				this,
				context
		) );

		getRenderingTarget().unindent( 2 );
		getRenderingTarget().addLine( ")" );
	}

	@Override
	public <A extends Annotation> void renderNestedAnnotation(A annotation, ModelsContext context) {
		//noinspection unchecked
		final AnnotationDescriptor<A> descriptor = (AnnotationDescriptor<A>) context.getAnnotationDescriptorRegistry().getDescriptor( annotation.annotationType() );
		final List<AttributeDescriptor<?>> attributes = descriptor.getAttributes();

		getRenderingTarget().addLine( "@%s(", descriptor.getAnnotationType().getName() );
		getRenderingTarget().indent( 2 );

		attributes.forEach( (attribute) -> attribute.getTypeDescriptor().render(
				attribute.getName(),
				extractValue( annotation, attribute ),
				getRenderingTarget(),
				this,
				context
		) );

		getRenderingTarget().unindent( 2 );
		getRenderingTarget().addLine( ")" );

	}
}
