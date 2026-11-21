package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Alerta del sistema SeguimientoYa (RF4 y RF10).
 * Representa tanto una alerta de toma olvidada como una de batería baja,
 * diferenciadas por el atributo tipo (apartado 8.1 del documento).
 */
public class Alerta {

    public static final String TOMA_OLVIDADA = "TOMA_OLVIDADA";
    public static final String BATERIA_BAJA = "BATERIA_BAJA";

    private final Long id;
    private final String tipo;
    private final Long pacienteId;
    private final String paciente;
    private final String detalle;
    private final LocalDateTime fechaHora;
    private boolean activa;

    public Alerta(Long id, String tipo, Long pacienteId, String paciente,
                  String detalle, LocalDateTime fechaHora) {
        this.id = id;
        this.tipo = tipo;
        this.pacienteId = pacienteId;
        this.paciente = paciente;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
        this.activa = true;
    }

    /** Cierra la alerta (operación desactivar() del diagrama de clases). */
    public void desactivar() {
        this.activa = false;
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public Long getPacienteId() { return pacienteId; }
    public String getPaciente() { return paciente; }
    public String getDetalle() { return detalle; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public boolean isActiva() { return activa; }
}
