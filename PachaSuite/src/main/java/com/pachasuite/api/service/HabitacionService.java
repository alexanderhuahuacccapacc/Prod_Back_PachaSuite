package com.pachasuite.api.service;

import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.dto.HabitacionUpdateDTO;
import com.pachasuite.api.entities.Habitacion;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.exception.ResourceNotFoundException;
import com.pachasuite.api.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitacionService {

    private static final List<String> ESTADOS_VALIDOS =
            Arrays.asList("libre", "pendiente", "ocupada", "mantenimiento");

    private final HabitacionRepository habitacionRepo;


    @Transactional(readOnly = true)
    public List<HabitacionDTO> buscarDisponibles(LocalDate checkIn, LocalDate checkOut, int adultos, int ninos) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("La fecha de check-out debe ser posterior al check-in.");
        }
        int capacidadTotal = adultos + ninos;
        int capacidadMax = ninos > 0 ? capacidadTotal + 1 : capacidadTotal;
        return habitacionRepo.findDisponibles(checkIn, checkOut, capacidadTotal, capacidadMax)
                .stream()
                .map(HabitacionDTO::from)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<HabitacionDTO> findAll() {
        return habitacionRepo.findAll()
                .stream()
                .map(HabitacionDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HabitacionDTO findById(Long id) {
        return habitacionRepo.findById(id)
                .map(HabitacionDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));
    }

    @Transactional
    public HabitacionDTO update(Long id, HabitacionUpdateDTO dto) {
        Habitacion h = habitacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        if (dto.getNombre() != null)     h.setNombre(dto.getNombre());
        if (dto.getPrecioBase() != null) h.setPrecioBase(dto.getPrecioBase());

        if (dto.getEstado() != null) {
            if (!ESTADOS_VALIDOS.contains(dto.getEstado())) {
                throw new BadRequestException("Estado inválido: " + dto.getEstado());
            }
            h.setEstado(Habitacion.HabitacionEstado.valueOf(dto.getEstado()));
        }

        if (dto.getAmenidades() != null) {
            h.setAmenidades(dto.getAmenidades());
        }

        Habitacion saved = habitacionRepo.save(h);
        log.info("Habitación {} actualizada", id);
        return HabitacionDTO.from(saved);
    }

    @Transactional
    public HabitacionDTO updateAmenidades(Long id, Map<String, Boolean> amenidades) {
        Habitacion h = habitacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        h.setAmenidades(amenidades);
        Habitacion saved = habitacionRepo.save(h);
        log.info("Amenidades de habitación {} actualizadas: {}", id, amenidades);
        return HabitacionDTO.from(saved);
    }


    @Transactional
    public void cambiarEstado(Long id, Habitacion.HabitacionEstado nuevoEstado) {
        Habitacion h = habitacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));
        h.setEstado(nuevoEstado);
        habitacionRepo.save(h);
    }
    @Transactional
    public String[] agregarImagen(Long id, String url) {
        Habitacion h = habitacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));
        String[] actuales = h.getImagenes() != null ? h.getImagenes() : new String[0];
        String[] nuevas   = Arrays.copyOf(actuales, actuales.length + 1);
        nuevas[actuales.length] = url;
        h.setImagenes(nuevas);
        habitacionRepo.save(h);
        return nuevas;
    }

    @Transactional
    public String[] eliminarImagen(Long id, String url) {
        Habitacion h = habitacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));
        String[] filtradas = Arrays.stream(h.getImagenes())
                .filter(img -> !img.equals(url))
                .toArray(String[]::new);
        h.setImagenes(filtradas);
        habitacionRepo.save(h);
        return filtradas;
    }
}