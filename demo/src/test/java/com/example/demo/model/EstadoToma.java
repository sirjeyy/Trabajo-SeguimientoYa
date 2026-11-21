package com.example.seguimientoya.model;

/**
 * Estados de una toma según la máquina de estados del apartado 8.2.
 */
public enum EstadoToma {
    PENDIENTE,    // aún no ha vencido el margen de olvido
    A_TIEMPO,     // evento dentro del margen de "a tiempo"
    TARDIA,       // evento fuera del margen de "a tiempo" pero antes del de olvido
    OLVIDADA,     // vencido el margen de olvido sin evento (genera alerta, RF4)
    DESCONOCIDA   // sin evento y el pastillero sin comunicar (decisión 4)
}
