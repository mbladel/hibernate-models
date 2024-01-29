/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.internal.jandex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.models.internal.ArrayTypeDetailsImpl;
import org.hibernate.models.internal.ClassTypeDetailsImpl;
import org.hibernate.models.internal.ParameterizedTypeDetailsImpl;
import org.hibernate.models.internal.PrimitiveTypeDetailsImpl;
import org.hibernate.models.internal.TypeVariableDetailsImpl;
import org.hibernate.models.internal.TypeVariableReferenceDetailsImpl;
import org.hibernate.models.internal.VoidTypeDetailsImpl;
import org.hibernate.models.internal.util.CollectionHelper;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.SourceModelBuildingContext;
import org.hibernate.models.spi.TypeDetails;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.TypeVariableReference;
import org.jboss.jandex.VoidType;
import org.jboss.jandex.WildcardType;

import static org.hibernate.models.internal.jandex.JandexTypeSwitcher.switchType;
import static org.hibernate.models.internal.util.CollectionHelper.arrayList;

/**
 * @author Steve Ebersole
 */
public class JandexTypeSwitchStandard implements JandexTypeSwitch<TypeDetails> {
	public static final JandexTypeSwitchStandard TYPE_SWITCH_STANDARD = new JandexTypeSwitchStandard();

	@Override
	public TypeDetails caseClass(ClassType classType, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( classType.name().toString() );
		return new ClassTypeDetailsImpl( classDetails, TypeDetails.Kind.CLASS );
	}

	@Override
	public TypeDetails casePrimitive(PrimitiveType primitiveType, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( primitiveType.name().toString() );
		return new PrimitiveTypeDetailsImpl( classDetails );
	}

	@Override
	public TypeDetails caseVoid(VoidType voidType, SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				// allows for void or Void
				.resolveClassDetails( voidType.name().toString() );
		return new VoidTypeDetailsImpl( classDetails );
	}

	@Override
	public TypeDetails caseParameterizedType(
			ParameterizedType parameterizedType,
			SourceModelBuildingContext buildingContext) {
		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( parameterizedType.name().toString() );
		return new ParameterizedTypeDetailsImpl(
				classDetails,
				extractTypeParameters( parameterizedType, this, buildingContext ),
				null
		);
	}

	@Override
	public TypeDetails caseWildcardType(WildcardType wildcardType, SourceModelBuildingContext buildingContext) {
		throw new UnsupportedOperationException( "Not yet implemented - " + this );
	}

	@Override
	public TypeDetails caseTypeVariable(TypeVariable typeVariable, SourceModelBuildingContext buildingContext) {
		return new TypeVariableDetailsImpl(
				typeVariable.identifier(),
				resolveTypes( typeVariable.bounds(), this, buildingContext )
		);
	}

	@Override
	public TypeDetails caseTypeVariableReference(
			TypeVariableReference typeVariableReference,
			SourceModelBuildingContext buildingContext) {
		return new TypeVariableReferenceDetailsImpl( typeVariableReference.identifier() );
	}

	@Override
	public TypeDetails caseArrayType(ArrayType arrayType, SourceModelBuildingContext buildingContext) {
		final ClassDetails arrayClassDetails = buildingContext
				.getClassDetailsRegistry()
				.resolveClassDetails( arrayType.name().toString() );
		return new ArrayTypeDetailsImpl(
				arrayClassDetails,
				JandexTypeSwitcher.switchType( arrayType.componentType(), this, buildingContext )
		);

	}

	@Override
	public TypeDetails defaultCase(Type t, SourceModelBuildingContext buildingContext) {
		throw new UnsupportedOperationException( "Not yet implemented - " + this );
	}

	private static List<TypeDetails> extractTypeParameters(
			ParameterizedType parameterizedType,
			JandexTypeSwitch<TypeDetails> typeSwitch,
			SourceModelBuildingContext buildingContext) {
		final List<Type> typeArguments = parameterizedType.arguments();
		if ( CollectionHelper.isEmpty( typeArguments ) ) {
			return Collections.emptyList();
		}

		final ArrayList<TypeDetails> result = arrayList( typeArguments.size() );
		for ( Type typeArgument : typeArguments ) {
			final TypeDetails switchedType = switchType( typeArgument, typeSwitch, buildingContext );
			result.add( switchedType );
		}
		return result;
	}

	public static List<TypeDetails> resolveTypes(
			List<Type> types,
			JandexTypeSwitch<TypeDetails> typeSwitch,
			SourceModelBuildingContext buildingContext) {
		if ( CollectionHelper.isEmpty( types ) ) {
			return Collections.emptyList();
		}

		final ArrayList<TypeDetails> result = arrayList( types.size() );
		for ( Type actualTypeArgument : types ) {
			final TypeDetails switchedType = switchType( actualTypeArgument, typeSwitch, buildingContext );
			result.add( switchedType );
		}
		return result;
	}
}