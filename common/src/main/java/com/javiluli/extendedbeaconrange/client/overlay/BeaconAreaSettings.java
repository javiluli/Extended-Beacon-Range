package com.javiluli.extendedbeaconrange.client.overlay;

/**
 * Valores de configuracion del overlay visual del beacon.
 *
 * <p>
 * Centralizar estas constantes evita que el renderer, el escaneo de chunks y el generador de color dependan de numeros magicos repartidos
 * por varias clases. Los valores visuales activos corresponden al estilo de muro completo bajo el beacon, fade superior y esquinas opacas.
 * </p>
 */
public final class BeaconAreaSettings {
	/** Desplazamiento binario usado para convertir coordenadas de bloque a coordenadas de chunk. */
	public static final int CHUNK_COORDINATE_SHIFT = 4;
	/** Chunk extra alrededor de la distancia de render para evitar cortes visuales en el borde. */
	public static final int RENDER_DISTANCE_PADDING_CHUNKS = 1;
	/** Saturacion de los colores calculados por posicion X/Z. */
	public static final float POSITION_COLOR_SATURATION = 0.88f;
	/** Brillo de los colores calculados por posicion X/Z. */
	public static final float POSITION_COLOR_VALUE = 1.0f;
	/** Numero de tonos disponibles para separar visualmente beacons cercanos. */
	public static final int POSITION_COLOR_BUCKETS = 16;
	/** Salto de paleta aplicado al hash X/Z para que posiciones cercanas cambien de color de forma notoria. */
	public static final int POSITION_COLOR_DISTINCTION_STEP = 5;
	/** Opacidad de las cuatro paredes del perimetro. */
	public static final float FULL_WALL_ALPHA = 0.25f;
	/** Altura maxima, en bloques, del muro por encima de la Y base del beacon. */
	public static final float FULL_WALL_HEIGHT = 4.0f;
	/** Altura, en bloques, a partir de la cual el muro empieza a desvanecerse hacia arriba. */
	public static final float FULL_WALL_FADE_START_HEIGHT = 0.0f;
	/** Opacidad de las esquinas verticales, usadas como marcas fuertes del limite. */
	public static final float FULL_WALL_CORNER_ALPHA = 1.0f;
	/** Anchura visual, en bloques, de cada tira vertical de esquina. */
	public static final float FULL_WALL_CORNER_WIDTH = 0.12f;
	/** Offset fijo del proyecto de referencia para evitar z-fighting con bordes exactos de bloque. */
	public static final float FULL_WALL_BOX_OFFSET = 0.005f;

	private BeaconAreaSettings() {
	}
}
