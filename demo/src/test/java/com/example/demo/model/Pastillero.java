package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Estado de un pastillero de SeguimientoYa (RF8): batería, cobertura y
 * última comunicación con el sistema.
 */
public record Pastillero(
		String identificador,
		int nivelBateria,
		int umbralBateria,
		boolean cobertura,
		LocalDateTime ultimaComunicacion) {

	/** La batería está baja cuando el nivel cae por debajo del umbral configurado (RF10). */
	public boolean isBateriaBaja() {
		return nivelBateria < umbralBateria;
	}
}
