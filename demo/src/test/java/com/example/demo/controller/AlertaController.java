package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.model.Alerta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de alertas de SeguimientoYa.
 * Cubre CU10 (consultar alertas y estado del pastillero), RF4 (alerta de toma
 * olvidada) y RF10 (alerta de batería baja).
 *
 * Endpoints:
 *   GET /alertas                       -> todas las alertas
 *   GET /alertas?activa=true           -> solo las activas
 *   GET /alertas?tipo=BATERIA_BAJA     -> filtradas por tipo
 *   GET /alertas/{id}                  -> una alerta concreta
 *   GET /alertas/paciente/{pacienteId} -> alertas de un paciente
 *   PUT /alertas/{id}/desactivar       -> cierra una alerta
 */
@RestController
@RequestMapping("/alertas")
public class AlertaController {

    // Datos de ejemplo en memoria (todavía no se usa la base de datos H2)
    private final Map<Long, Alerta> alertas = new ConcurrentHashMap<>();

    public AlertaController() {
        LocalDateTime hoy = LocalDateTime.now().withSecond(0).withNano(0);
        guardar(new Alerta(1L, Alerta.TOMA_OLVIDADA, 1L, "Carmen López",
                "Toma de las 09:00 (compartimento 1) no realizada", hoy.minusHours(3)));
        guardar(new Alerta(2L, Alerta.BATERIA_BAJA, 1L, "Carmen López",
                "Batería del pastillero PST-001 al 15 % (umbral 20 %)", hoy.minusHours(1)));
        guardar(new Alerta(3L, Alerta.TOMA_OLVIDADA, 2L, "José Martín",
                "Toma de las 14:00 (compartimento 3) no realizada", hoy.minusMinutes(30)));
        Alerta cerrada = new Alerta(4L, Alerta.TOMA_OLVIDADA, 2L, "José Martín",
                "Toma de las 21:00 (compartimento 2) no realizada", hoy.minusDays(1));
        cerrada.desactivar();
        guardar(cerrada);
    }

    private void guardar(Alerta a) {
        alertas.put(a.getId(), a);
    }

    /** Lista las alertas, con filtros opcionales por estado y por tipo. */
    @GetMapping
    public List<Alerta> listar(@RequestParam(required = false) Boolean activa,
                               @RequestParam(required = false) String tipo) {
        return alertas.values().stream()
                .filter(a -> activa == null || a.isActiva() == activa)
                .filter(a -> tipo == null || a.getTipo().equalsIgnoreCase(tipo))
                .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                .toList();
    }

    /** Devuelve una alerta por su id, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<Alerta> obtener(@PathVariable Long id) {
        Alerta a = alertas.get(id);
        return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
    }

    /** Alertas de un paciente concreto (CU10, paso 4). */
    @GetMapping("/paciente/{pacienteId}")
    public List<Alerta> porPaciente(@PathVariable Long pacienteId) {
        return alertas.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                .toList();
    }

    /** Desactiva una alerta, por ejemplo tras marcar la toma manualmente (CU11). */
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Alerta> desactivar(@PathVariable Long id) {
        Alerta a = alertas.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        a.desactivar();
        return ResponseEntity.ok(a);
    }
}
