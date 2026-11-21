package com.example.seguimientoya.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Toma pautada de un paciente y su clasificación automática (RF3).
 * El historial de tomas (fecha, hora, fármaco y estado) es lo que muestra el panel (RF5).
 */
public record Toma(
        int id,
        String paciente,
        String farmaco,
        int compartimento,
        LocalDateTime horaPautada,
        LocalDateTime horaEvento,   // null si el pastillero no ha registrado apertura
        String marcadaPor,          // null si la registró el sensor; nombre si fue manual (RF9)
        EstadoToma estado) {

    /** Margen configurable para considerar la toma "a tiempo" (minutos). */
    public static final long MARGEN_A_TIEMPO_MIN = 30;
    /** Margen configurable a partir del cual la toma se considera olvidada (minutos). */
    public static final long MARGEN_OLVIDO_MIN = 120;

    /**
     * Crea la toma y la clasifica.
     *
     * @param pastilleroComunica false si el pastillero lleva tiempo sin comunicar
     */
    public static Toma de(int id, String paciente, String farmaco, int compartimento,
                          LocalDateTime horaPautada, LocalDateTime horaEvento,
                          String marcadaPor, boolean pastilleroComunica) {
        return new Toma(id, paciente, farmaco, compartimento, horaPautada, horaEvento,
                marcadaPor, clasificar(horaPautada, horaEvento, marcadaPor, pastilleroComunica));
    }

    static EstadoToma clasificar(LocalDateTime horaPautada, LocalDateTime horaEvento,
                                 String marcadaPor, boolean pastilleroComunica) {
        if (horaEvento != null) {
            long retraso = Math.abs(Duration.between(horaPautada, horaEvento).toMinutes());
            return retraso <= MARGEN_A_TIEMPO_MIN ? EstadoToma.A_TIEMPO : EstadoToma.TARDIA;
        }
        if (marcadaPor != null) {
            return EstadoToma.A_TIEMPO; // confirmación manual del supervisor
        }
        long transcurrido = Duration.between(horaPautada, LocalDateTime.now()).toMinutes();
        if (transcurrido <= MARGEN_OLVIDO_MIN) {
            return EstadoToma.PENDIENTE;
        }
        return pastilleroComunica ? EstadoToma.OLVIDADA : EstadoToma.DESCONOCIDA;
    }
}
