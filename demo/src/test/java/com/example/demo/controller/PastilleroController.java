package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Pastillero;

/**
 * Controlador que muestra la información del pastillero (RF8, CU4).
 * De momento devuelve datos de ejemplo, ya que el hardware se simula.
 */
@RestController
public class PastilleroController {

	@GetMapping("/pastillero")
	public Pastillero obtenerPastillero() {
		return new Pastillero("PAST-001", 82, 20, true, LocalDateTime.now().minusMinutes(5));
	}
}
