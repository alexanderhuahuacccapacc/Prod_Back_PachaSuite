package com.pachasuite.api.controller;

import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.dto.HabitacionUpdateDTO;
import com.pachasuite.api.service.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/habitaciones")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Habitaciones", description = "CRUD y amenidades (solo ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminHabitacionController {

    private final HabitacionService habitacionService;
    private final SupabaseStorageService storageService;

    @GetMapping
    @Operation(summary = "Listar todas las habitaciones")
    public ResponseEntity<List<HabitacionDTO>> findAll() {
        return ResponseEntity.ok(habitacionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener habitación por ID")
    public ResponseEntity<HabitacionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar habitación (nombre, precio, estado, amenidades)")
    public ResponseEntity<HabitacionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody HabitacionUpdateDTO dto) {
        return ResponseEntity.ok(habitacionService.update(id, dto));
    }

    @PutMapping("/{id}/amenidades")
    @Operation(summary = "Actualizar matriz de amenidades con toggles",
            description = "Ejemplo: {\"internet\":true,\"buffetAndino\":false,\"cochera\":true,...}")
    public ResponseEntity<HabitacionDTO> updateAmenidades(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> amenidades) {
        return ResponseEntity.ok(habitacionService.updateAmenidades(id, amenidades));
    }


    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Subir imagen a una habitación")
    public ResponseEntity<Map<String, Object>> subir(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws Exception {

        // validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Solo se permiten imágenes"));
        }

        // validar tamaño máximo 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La imagen no puede superar 5MB"));
        }

        String numero = habitacionService.findById(id).getNumero();
        String url    = storageService.subirImagen(file, numero);
        String[] nuevas = habitacionService.agregarImagen(id, url);

        return ResponseEntity.ok(Map.of(
                "url",      url,
                "imagenes", nuevas,
                "total",    nuevas.length
        ));
    }

    @DeleteMapping
    @Operation(summary = "Eliminar imagen de una habitación")
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable Long id,
            @RequestParam("url") String url) throws Exception {

        storageService.eliminarImagen(url);
        String[] restantes = habitacionService.eliminarImagen(id, url);

        return ResponseEntity.ok(Map.of(
                "imagenes", restantes,
                "total",    restantes.length
        ));
    }
}
