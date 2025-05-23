/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.internal.jdk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.models.internal.ClassDetailsSupport;
import org.hibernate.models.internal.util.CollectionHelper;
import org.hibernate.models.serial.spi.SerialClassDetails;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.ClassLoading;
import org.hibernate.models.spi.FieldDetails;
import org.hibernate.models.spi.MethodDetails;
import org.hibernate.models.spi.RecordComponentDetails;
import org.hibernate.models.spi.ModelsContext;
import org.hibernate.models.spi.TypeDetails;
import org.hibernate.models.spi.TypeVariableDetails;

import static org.hibernate.models.internal.jdk.JdkBuilders.buildMethodDetails;
import static org.hibernate.models.internal.util.CollectionHelper.arrayList;

/**
 * ClassDetails implementation based on a {@link Class} reference
 *
 * @author Steve Ebersole
 */
public class JdkClassDetails extends AbstractJdkAnnotationTarget implements ClassDetailsSupport {
	private final String name;
	private final Class<?> managedClass;

	private final ClassDetails superClass;
	private List<TypeDetails> interfaces;
	private TypeDetails genericSuperType;
	private List<TypeVariableDetails> typeParameters;

	private List<FieldDetails> fields;
	private List<MethodDetails> methods;
	private List<RecordComponentDetails> recordComponents;

	public JdkClassDetails(
			Class<?> managedClass,
			ModelsContext modelContext) {
		this( managedClass.getName(), managedClass, modelContext );
	}

	public JdkClassDetails(
			String name,
			Class<?> managedClass,
			ModelsContext modelContext) {
		super( managedClass::getAnnotations, modelContext );
		this.name = name;
		this.managedClass = managedClass;
		this.superClass = determineSuperClass( managedClass, modelContext );
	}

	private static ClassDetails determineSuperClass(Class<?> managedClass, ModelsContext modelContext) {
		final Class<?> superclass = managedClass.getSuperclass();
		if ( superclass == null ) {
			return null;
		}

		final ClassDetailsRegistry classDetailsRegistry = modelContext.getClassDetailsRegistry();
		return classDetailsRegistry.resolveClassDetails( superclass.getName() );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return managedClass.getName();
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public <X> Class<X> toJavaClass() {
		//noinspection unchecked
		return (Class<X>) managedClass;
	}

	@Override
	public <X> Class<X> toJavaClass(ClassLoading classLoading, ModelsContext modelContext) {
		if ( managedClass.isPrimitive()
				|| managedClass.isEnum()
				|| managedClass.isAnnotation() ) {
			//noinspection unchecked
			return (Class<X>) managedClass;
		}
		return classLoading.classForName( getClassName() );
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract( managedClass.getModifiers() );
	}

	@Override
	public boolean isInterface() {
		return managedClass.isInterface();
	}

	@Override
	public boolean isEnum() {
		return managedClass.isEnum();
	}

	@Override
	public boolean isRecord() {
		return managedClass.isRecord();
	}

	@Override
	public ClassDetails getSuperClass() {
		return superClass;
	}

	@Override
	public TypeDetails getGenericSuperType() {
		if ( genericSuperType == null && managedClass.getGenericSuperclass() != null ) {
			genericSuperType = new JdkTrackingTypeSwitcher( getModelContext() ).switchType( managedClass.getGenericSuperclass() );
		}
		return genericSuperType;
	}

	@Override
	public List<TypeDetails> getImplementedInterfaces() {
		if ( interfaces == null ) {
			interfaces = collectInterfaces();
		}
		return interfaces;
	}

	private List<TypeDetails> collectInterfaces() {
		final Type[] jdkInterfaces = managedClass.getGenericInterfaces();
		if ( CollectionHelper.isEmpty( jdkInterfaces ) ) {
			return Collections.emptyList();
		}

		final ArrayList<TypeDetails> result = arrayList( jdkInterfaces.length );
		final JdkTrackingTypeSwitcher typeSwitcher = new JdkTrackingTypeSwitcher( getModelContext() );
		for ( Type jdkInterface : jdkInterfaces ) {
			final TypeDetails switchedInterfaceType = typeSwitcher.switchType( jdkInterface );
			result.add( switchedInterfaceType );
		}
		return result;
	}

	@Override
	public List<TypeVariableDetails> getTypeParameters() {
		if ( typeParameters == null ) {
			typeParameters = collectTypeParameters();
		}
		return typeParameters;
	}

	private List<TypeVariableDetails> collectTypeParameters() {
		final TypeVariable<? extends Class<?>>[] jdkTypeParameters = managedClass.getTypeParameters();
		if ( CollectionHelper.isEmpty( jdkTypeParameters ) ) {
			return Collections.emptyList();
		}

		final ArrayList<TypeVariableDetails> result = arrayList( jdkTypeParameters.length );
		final JdkTrackingTypeSwitcher typeSwitcher = new JdkTrackingTypeSwitcher( getModelContext() );
		for ( TypeVariable<? extends Class<?>> jdkTypeParameter : jdkTypeParameters ) {
			result.add( (TypeVariableDetails) typeSwitcher.switchType( jdkTypeParameter ) );
		}
		return result;
	}

	@Override
	public boolean isImplementor(Class<?> checkType) {
		return checkType.isAssignableFrom( managedClass );
	}

	@Override
	public List<FieldDetails> getFields() {
		if ( fields == null ) {
			final Field[] reflectionFields = managedClass.getDeclaredFields();
			this.fields = arrayList( reflectionFields.length );
			for ( int i = 0; i < reflectionFields.length; i++ ) {
				final Field reflectionField = reflectionFields[i];
				if ( reflectionField.isSynthetic() ) {
					continue;
				}
				fields.add( new JdkFieldDetails( reflectionField, this, getModelContext() ) );
			}
		}
		return fields;
	}

	@Override
	public void addField(FieldDetails fieldDetails) {
		getFields().add( fieldDetails );
	}

	@Override
	public List<MethodDetails> getMethods() {
		if ( methods == null ) {
			final Method[] reflectionMethods = managedClass.getDeclaredMethods();
			this.methods = arrayList( reflectionMethods.length );
			for ( int i = 0; i < reflectionMethods.length; i++ ) {
				if ( reflectionMethods[i].isSynthetic() ) {
					continue;
				}
				this.methods.add( buildMethodDetails( reflectionMethods[i], this, getModelContext() ) );
			}
		}
		return methods;
	}

	@Override
	public void addMethod(MethodDetails methodDetails) {
		getMethods().add( methodDetails );
	}

	@Override
	public List<RecordComponentDetails> getRecordComponents() {
		if ( !isRecord() ) {
			return Collections.emptyList();
		}
		if ( recordComponents == null ) {
			final RecordComponent[] jdkRecordComponents = managedClass.getRecordComponents();
			recordComponents = arrayList( jdkRecordComponents.length );
			for ( int i = 0; i < jdkRecordComponents.length; i++ ) {
				recordComponents.add( new JdkRecordComponentDetails( jdkRecordComponents[i], this, getModelContext() ) );
			}
		}
		return recordComponents;
	}

	@Override
	public String toString() {
		return "JdkClassDetails(" + name + ")";
	}

	@Override
	public SerialClassDetails toStorableForm() {
		return new SerialJdkClassDetails( name, managedClass );
	}
}
