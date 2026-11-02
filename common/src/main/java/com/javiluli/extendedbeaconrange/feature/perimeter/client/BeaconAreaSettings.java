package com.javiluli.extendedbeaconrange.feature.perimeter.client;

/**
 * Valores de configuracion del overlay visual del beacon.
 *
 * <p>
 * Centralizar estas constantes evita que el renderer, el escaneo de chunks y el generador de color dependan de numeros magicos repartidos
 * por varias clases. Los valores visuales activos corresponden al estilo de muro completo bajo el beacon, fade superior y esquinas opacas.
 * </p>
 */
public final class BeaconAreaSettings {
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
	/** Seccion cuadrada del poste: 1/16 de bloque (un pixel) de grosor en X y Z. */
	public static final float FULL_WALL_CORNER_WIDTH = 1.0f / 16.0f;
	/** Offset interno pequeno para evitar z-fighting sin invadir bloques fuera del rango. */
	public static final float FULL_WALL_BOX_OFFSET = 0.005f;

	private BeaconAreaSettings() {
	}
}
