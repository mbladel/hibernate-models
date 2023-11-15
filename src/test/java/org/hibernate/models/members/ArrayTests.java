package org.hibernate.models.members;

import org.hibernate.models.SourceModelTestHelper;
import org.hibernate.models.internal.SourceModelBuildingContextImpl;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.FieldDetails;

import org.junit.jupiter.api.Test;

import org.jboss.jandex.Index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.SourceModelTestHelper.buildJandexIndex;

/**
 * @author Steve Ebersole
 */
public class ArrayTests {
	@Test
	void testWithJandex() {
		verify( buildJandexIndex( SomeClass.class ) );
	}

	@Test
	void testWithoutJandex() {
		verify( null );
	}

	private void verify(Index index) {
		final SourceModelBuildingContextImpl buildingContext = SourceModelTestHelper.createBuildingContext(
				index,
				SomeClass.class
		);

		final ClassDetailsRegistry classDetailsRegistry = buildingContext.getClassDetailsRegistry();
		final ClassDetails classDetails = classDetailsRegistry.getClassDetails( SomeClass.class.getName() );
		assertThat( classDetails ).isNotNull();

		{
			final FieldDetails field = classDetails.findFieldByName( "intArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( int[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "intArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( int[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "intArray3" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( int[][][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "charArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( char[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "charArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( char[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "byteArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( byte[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "byteArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( byte[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "booleanArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( boolean[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "booleanArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( boolean[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "shortArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( short[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "shortArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( short[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "longArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( long[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "longArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( long[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "doubleArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( double[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "doubleArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( double[][].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "floatArray" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( float[].class );
		}

		{
			final FieldDetails field = classDetails.findFieldByName( "floatArray2" );
			assertThat( field.isPersistable() ).isTrue();
			assertThat( field.getType().toJavaClass() ).isEqualTo( float[][].class );
		}

		assertThat( classDetailsRegistry.resolveClassDetails( "[I" ).toJavaClass() ).isEqualTo( int[].class );
		assertThat( classDetailsRegistry.resolveClassDetails( "[[I" ).toJavaClass() ).isEqualTo( int[][].class );
		assertThat( classDetailsRegistry.resolveClassDetails( "[[[I" ).toJavaClass() ).isEqualTo( int[][][].class );

		assertThat( classDetailsRegistry.resolveClassDetails( "[C" ).toJavaClass() ).isEqualTo( char[].class );
		assertThat( classDetailsRegistry.resolveClassDetails( "[[C" ).toJavaClass() ).isEqualTo( char[][].class );

		assertThat( classDetailsRegistry.resolveClassDetails( "[Ljava.lang.String;" ).toJavaClass() ).isEqualTo( String[].class );
		assertThat( classDetailsRegistry.resolveClassDetails( "[[Ljava.lang.String;" ).toJavaClass() ).isEqualTo( String[][].class );
	}

	@SuppressWarnings("unused")
	public static class SomeClass {
		private int[] intArray;
		private int[][] intArray2;
		private int[][][] intArray3;

		private char[] charArray;
		private char[][] charArray2;

		private byte[] byteArray;
		private byte[][] byteArray2;

		private boolean[] booleanArray;
		private boolean[][] booleanArray2;

		private short[] shortArray;
		private short[][] shortArray2;

		private long[] longArray;
		private long[][] longArray2;

		private double[] doubleArray;
		private double[][] doubleArray2;

		private float[] floatArray;
		private float[][] floatArray2;
	}
}
