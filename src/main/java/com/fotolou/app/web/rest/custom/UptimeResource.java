package com.fotolou.app.web.rest.custom;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour le monitoring de disponibilité (Uptime, Health & Keep-Alive).
 * Utilisé pour :
 * 1. Empêcher la mise en veille sur Render Free Tier (UptimeRobot ping toutes les 5-10 min).
 * 2. Serveur de Health Check natif de Render (Auto-Reboot en cas de blocage applicatif).
 */
@Tag(name = "10. Surveillance & Uptime", description = "Monitoring de disponibilité, santé et keep-alive pour Render / UptimeRobot")
@RestController
@RequestMapping("/api")
public class UptimeResource {

    private static final long START_TIME_MILLIS = System.currentTimeMillis();

    public record JvmMemory(long totalMemoryMb, long freeMemoryMb, long maxMemoryMb, long usedMemoryMb) {}

    public record UptimeResponse(
        String status,
        String service,
        long uptimeSeconds,
        String uptimeFormatted,
        Instant startTime,
        Instant timestamp,
        JvmMemory memory,
        String message
    ) {}

    /**
     * GET /api/uptime ou /api/health : Statut de disponibilité complet du backend.
     * Idéal pour UptimeRobot, Render Health Check ou tableau de bord.
     */
    @Operation(summary = "Statut et temps de fonctionnement du backend (Keep-Alive Render / UptimeRobot)")
    @GetMapping(value = { "/uptime", "/health" })
    public ResponseEntity<UptimeResponse> getUptime() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        long uptimeSec = uptimeMillis / 1000;

        long days = uptimeSec / 86400;
        long hours = (uptimeSec % 86400) / 3600;
        long minutes = (uptimeSec % 3600) / 60;
        long seconds = uptimeSec % 60;

        String formatted = String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);

        UptimeResponse response = new UptimeResponse(
            "UP",
            "fotolou-backend",
            uptimeSec,
            formatted,
            Instant.ofEpochMilli(START_TIME_MILLIS),
            Instant.now(),
            new JvmMemory(totalMemory, freeMemory, maxMemory, usedMemory),
            "Fotolou Backend is running smoothly."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/uptime/ping ou /api/health/ping : Ping ultra-léger (Keep-Alive).
     */
    @Operation(summary = "Ping ultra-léger (Keep-Alive)")
    @GetMapping(value = { "/uptime/ping", "/health/ping" })
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "pong", "service", "fotolou-backend"));
    }
}
