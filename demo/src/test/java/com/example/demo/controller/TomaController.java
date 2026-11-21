package com.example.seguimientoya.controller;

import com.example.seguimientoya.model.EstadoToma;
import com.example.seguimientoya.model.Toma;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST del historial de tomas de SeguimientoYa (RF3, RF5).
 * Datos simulados en memoria que reproducen los escenarios del proyecto:
 * toma a tiempo, tardía, olvido, pérdida de cobertura y confirmación manual.
 *
 * GET /tomas                     -> historial completo
 * GET /tomas/{id}                -> una toma
 * GET /tomas/paciente/{nombre}   -> historial de un paciente
 * GET /tomas/estado/{estado}     -> filtra por estado (a_tiempo, tardia, olvidada...)
 * GET /tomas/resumen             -> número de tomas por estado y % de adherencia
 */
@RestController
@RequestMapping("/tomas")
public class TomaController {

    private final List<Toma> tomas;

    public TomaController() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        tomas = List.of(
                // Escenario: toma a tiempo (abre 10 min después)
                Toma.de(1, "Carmen López", "Enalapril 10 mg", 1,
                        ayer.atTime(8, 0), ayer.atTime(8, 10), null, true),
                // Escenario: toma tardía (abre 75 min después)
                Toma.de(2, "Carmen López", "Metformina 850 mg", 2,
                        ayer.atTime(14, 0), ayer.atTime(15, 15), null, true),
                // Escenario: olvido (sin evento y el pastillero comunica)
                Toma.de(3, "Carmen López", "Atorvastatina 20 mg", 3,
                        ayer.atTime(22, 0), null, null, true),
                // Confirmación manual por el cuidador (RF9)
                Toma.de(4, "José Martín", "Omeprazol 20 mg", 1,
                        ayer.atTime(9, 0), null, "Ana Martín (familiar)", true),
                // Escenario: pérdida de cobertura -> desconocida, no olvidada
                Toma.de(5, "Rosa Fernández", "Sintrom 4 mg", 1,
                        ayer.atTime(20, 0), null, null, false),
                // Toma de hoy dentro del margen -> pendiente
                Toma.de(6, "José Martín", "Levotiroxina 50 mcg", 2,
                        LocalDateTime.now().minusMinutes(15), null, null, true)
        );
    }

    @GetMapping
    public List<Toma> historial() {
        return tomas;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Toma> obtener(@PathVariable int id) {
        return tomas.stream()
                .filter(t -> t.id() == id)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{nombre}")
    public List<Toma> porPaciente(@PathVariable String nombre) {
        String buscado = nombre.toLowerCase();
        return tomas.stream()
                .filter(t -> t.paciente().toLowerCase().contains(buscado))
                .toList();
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Toma>> porEstado(@PathVariable String estado) {
        try {
            EstadoToma e = EstadoToma.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(tomas.stream().filter(t -> t.estado() == e).toList());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        Map<EstadoToma, Long> porEstado = new EnumMap<>(EstadoToma.class);
        for (EstadoToma e : EstadoToma.values()) {
            porEstado.put(e, tomas.stream().filter(t -> t.estado() == e).count());
        }
        long evaluadas = porEstado.get(EstadoToma.A_TIEMPO)
                + porEstado.get(EstadoToma.TARDIA)
                + porEstado.get(EstadoToma.OLVIDADA);
        double adherencia = evaluadas == 0 ? 0
                : 100.0 * (porEstado.get(EstadoToma.A_TIEMPO) + porEstado.get(EstadoToma.TARDIA)) / evaluadas;

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", tomas.size());
        resumen.put("porEstado", porEstado);
        resumen.put("adherenciaPorcentaje", Math.round(adherencia * 10) / 10.0);
        resumen.put("margenATiempoMin", Toma.MARGEN_A_TIEMPO_MIN);
        resumen.put("margenOlvidoMin", Toma.MARGEN_OLVIDO_MIN);
        return resumen;
    }
}
