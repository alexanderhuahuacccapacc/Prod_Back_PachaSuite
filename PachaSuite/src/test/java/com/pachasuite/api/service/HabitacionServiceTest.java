package com.pachasuite.api.service;

import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.dto.HabitacionUpdateDTO;
import com.pachasuite.api.entities.Habitacion;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.exception.ResourceNotFoundException;
import com.pachasuite.api.repository.HabitacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HabitacionService – Tests unitarios")
class HabitacionServiceTest {

    @Mock  HabitacionRepository habitacionRepo;
    @InjectMocks HabitacionService habitacionService;

    private Habitacion habitacionLibre;

    @BeforeEach
    void setUp() {
        Map<String, Boolean> amenidades = new HashMap<>();
        amenidades.put("internet", true);
        amenidades.put("cable", true);
        amenidades.put("banioPrivado", true);
        amenidades.put("buffetAndino", false);
        amenidades.put("cochera", false);
        amenidades.put("spa", false);

        habitacionLibre = Habitacion.builder()
            .id(1L)
            .numero("101")
            .nombre("Suite Simple Andes")
            .tipo(Habitacion.HabitacionTipo.simple)
            .capacidad(1)
            .precioBase(new BigDecimal("60.00"))
            .sizeM2(18)
            .camas("1 cama twin")
            .estado(Habitacion.HabitacionEstado.libre)
            .amenidades(amenidades)
            .imagenes(new String[]{})
            .build();
    }

    // ── buscarDisponibles ─────────────────────────────────────

    @Test
    @DisplayName("buscarDisponibles: retorna lista cuando hay habitaciones disponibles")
    void buscarDisponibles_conHabitacionesDisponibles_retornaLista() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        when(habitacionRepo.findDisponibles(checkIn, checkOut, 1,1))
            .thenReturn(List.of(habitacionLibre));

        List<HabitacionDTO> resultado = habitacionService.buscarDisponibles(checkIn, checkOut, 1, 0);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumero()).isEqualTo("101");
        assertThat(resultado.get(0).getEstado()).isEqualTo("libre");
        verify(habitacionRepo, times(1)).findDisponibles(checkIn, checkOut, 1,1);
    }

    @Test
    @DisplayName("buscarDisponibles: retorna lista vacía si no hay disponibles")
    void buscarDisponibles_sinDisponibles_retornaListaVacia() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        when(habitacionRepo.findDisponibles(any(), any(), anyInt(), anyInt()))
            .thenReturn(Collections.emptyList());

        List<HabitacionDTO> resultado = habitacionService.buscarDisponibles(checkIn, checkOut, 2, 0);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarDisponibles: lanza BadRequest si checkOut <= checkIn")
    void buscarDisponibles_fechasInvalidas_lanzaBadRequest() {
        LocalDate checkIn  = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(1);

        assertThatThrownBy(() ->
            habitacionService.buscarDisponibles(checkIn, checkOut, 1,0))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("check-out debe ser posterior");

        verifyNoInteractions(habitacionRepo);
    }

    // ── findById ──────────────────────────────────────────────

    @Test
    @DisplayName("findById: retorna DTO cuando la habitación existe")
    void findById_existente_retornaDTO() {
        when(habitacionRepo.findById(1L)).thenReturn(Optional.of(habitacionLibre));

        HabitacionDTO dto = habitacionService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Suite Simple Andes");
        assertThat(dto.getPrecioBase()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("findById: lanza ResourceNotFound cuando no existe")
    void findById_noExiste_lanzaNotFound() {
        when(habitacionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitacionService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ── update ────────────────────────────────────────────────

    @Test
    @DisplayName("update: actualiza nombre y precio correctamente")
    void update_camposValidos_actualizaYRetorna() {
        HabitacionUpdateDTO dto = new HabitacionUpdateDTO();
        dto.setNombre("Suite Simple Actualizada");
        dto.setPrecioBase(new BigDecimal("75.00"));

        when(habitacionRepo.findById(1L)).thenReturn(Optional.of(habitacionLibre));
        when(habitacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HabitacionDTO result = habitacionService.update(1L, dto);

        assertThat(result.getNombre()).isEqualTo("Suite Simple Actualizada");
        assertThat(result.getPrecioBase()).isEqualByComparingTo("75.00");
        verify(habitacionRepo).save(any(Habitacion.class));
    }

    @Test
    @DisplayName("update: lanza BadRequest con estado inválido")
    void update_estadoInvalido_lanzaBadRequest() {
        HabitacionUpdateDTO dto = new HabitacionUpdateDTO();
        dto.setNombre("Test");
        dto.setEstado("fantasma");

        when(habitacionRepo.findById(1L)).thenReturn(Optional.of(habitacionLibre));

        assertThatThrownBy(() -> habitacionService.update(1L, dto))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Estado inválido");
    }

    // ── updateAmenidades ──────────────────────────────────────

    @Test
    @DisplayName("updateAmenidades: persiste el mapa de toggles correctamente")
    void updateAmenidades_mapaValido_persistido() {
        Map<String, Boolean> nuevasAmenidades = new HashMap<>();
        nuevasAmenidades.put("internet", true);
        nuevasAmenidades.put("buffetAndino", true);
        nuevasAmenidades.put("cochera", false);

        when(habitacionRepo.findById(1L)).thenReturn(Optional.of(habitacionLibre));
        when(habitacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HabitacionDTO result = habitacionService.updateAmenidades(1L, nuevasAmenidades);

        assertThat(result.getAmenidades()).containsEntry("buffetAndino", true);
        assertThat(result.getAmenidades()).containsEntry("cochera", false);
    }
}
