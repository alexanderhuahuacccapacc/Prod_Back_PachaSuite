package com.pachasuite.api.service;

import com.pachasuite.api.dto.HuespedDTO;
import com.pachasuite.api.dto.ReservaRequestDTO;
import com.pachasuite.api.dto.ReservaResponseDTO;
import com.pachasuite.api.entities.*;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.repository.ExtraRepository;
import com.pachasuite.api.repository.HabitacionRepository;
import com.pachasuite.api.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService – Tests unitarios")
class ReservaServiceTest {

    @Mock ReservaRepository    reservaRepo;
    @Mock HabitacionRepository habitacionRepo;
    @Mock ExtraRepository      extraRepo;
    @Mock CodigoService        codigoService;

    @InjectMocks ReservaService reservaService;

    private Habitacion habitacionLibre;
    private CodigoVerificacion codigoValido;
    private VerificacionContacto contactoTest;

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
                .estado(Habitacion.HabitacionEstado.libre)
                .amenidades(amenidades)
                .imagenes(new String[]{})
                .build();

        contactoTest = VerificacionContacto.builder()
                .id(1L)
                .tipo(VerificacionContacto.TipoContacto.EMAIL)
                .valor("test@example.com")
                .build();

        codigoValido = CodigoVerificacion.builder()
                .id(1L)
                .contacto(contactoTest)
                .metodo(CodigoVerificacion.MetodoVerificacion.EMAIL)
                .codigo("123456")
                .usado(false)
                .expiraEn(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    // ── calcularTotal ─────────────────────────────────────────

    @Test
    @DisplayName("calcularTotal: sin extras – 2 noches a $60 = subtotal $120, impuestos $21.60, total $141.60")
    void calcularTotal_sinExtras_correcto() {
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),   // 2 noches
                Collections.emptyList()
        );

        mockDependencias(req);
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservaResponseDTO resp = reservaService.crearReserva(req);

        assertThat(resp.getSubtotal()).isEqualByComparingTo("120.00");
        assertThat(resp.getImpuestos()).isEqualByComparingTo("21.60");
        assertThat(resp.getTotal()).isEqualByComparingTo("141.60");
    }

    @Test
    @DisplayName("calcularTotal: con extras spa($20) – 3 noches a $60 = subtotal $240")
    void calcularTotal_conExtras_correcto() {
        Extra spa = Extra.builder()
                .id(1L).codigo("spa").nombre("Spa").precioNoche(new BigDecimal("20.00"))
                .build();

        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),   // 3 noches
                List.of("spa")
        );

        when(codigoService.validar(anyString(), anyString())).thenReturn(codigoValido);

        // ✅ CORREGIDO: Usar findByIdWithLock
        when(habitacionRepo.findByIdWithLock(1L)).thenReturn(Optional.of(habitacionLibre));

        when(habitacionRepo.existeSolape(any(), any(), any(), any())).thenReturn(false);
        when(extraRepo.findByCodigoIn(List.of("spa"))).thenReturn(List.of(spa));
        when(reservaRepo.existsByCodigo(anyString())).thenReturn(false);
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservaResponseDTO resp = reservaService.crearReserva(req);

        assertThat(resp.getSubtotal()).isEqualByComparingTo("240.00");
        assertThat(resp.getImpuestos()).isEqualByComparingTo("43.20");
        assertThat(resp.getTotal()).isEqualByComparingTo("283.20");
    }

    // ── verificarDisponibilidad ───────────────────────────────

    @Test
    @DisplayName("crearReserva: lanza BadRequest si habitación no está libre")
    void crearReserva_habitacionOcupada_lanzaBadRequest() {
        habitacionLibre.setEstado(Habitacion.HabitacionEstado.ocupada);
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                Collections.emptyList());

        when(codigoService.validar(anyString(), anyString())).thenReturn(codigoValido);

        // ✅ CORREGIDO: Usar findByIdWithLock
        when(habitacionRepo.findByIdWithLock(1L)).thenReturn(Optional.of(habitacionLibre));

        assertThatThrownBy(() -> reservaService.crearReserva(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("crearReserva: lanza BadRequest si hay solape de fechas")
    void crearReserva_conSolape_lanzaBadRequest() {
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                Collections.emptyList());

        when(codigoService.validar(anyString(), anyString())).thenReturn(codigoValido);

        // ✅ CORREGIDO: Usar findByIdWithLock
        when(habitacionRepo.findByIdWithLock(1L)).thenReturn(Optional.of(habitacionLibre));
        when(habitacionRepo.existeSolape(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservaService.crearReserva(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("fechas seleccionadas");
    }

    @Test
    @DisplayName("crearReserva: lanza BadRequest si checkout <= checkin")
    void crearReserva_fechasInvalidas_lanzaBadRequest() {
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(2),
                Collections.emptyList());

        assertThatThrownBy(() -> reservaService.crearReserva(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("check-out debe ser posterior");
    }

    // ── flujo completo ────────────────────────────────────────

    @Test
    @DisplayName("crearReserva: flujo completo – habitación pasa a pendiente y código se marca usado")
    void crearReserva_flujoCompleto_estadosCambian() {
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                Collections.emptyList());

        mockDependencias(req);
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        reservaService.crearReserva(req);

        verify(habitacionRepo).save(argThat(h ->
                h.getEstado() == Habitacion.HabitacionEstado.pendiente));

        verify(codigoService).marcarUsado(codigoValido);
    }

    @Test
    @DisplayName("crearReserva: el código generado empieza con RES-")
    void crearReserva_codigoFormato_correcto() {
        ReservaRequestDTO req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                Collections.emptyList());

        mockDependencias(req);
        when(reservaRepo.save(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservaResponseDTO resp = reservaService.crearReserva(req);

        assertThat(resp.getCodigo()).startsWith("RES-");
        assertThat(resp.getCodigo()).hasSize(14);
    }

    // ── helpers ───────────────────────────────────────────────

    private ReservaRequestDTO buildRequest(
            LocalDate checkIn, LocalDate checkOut, List<String> extras) {

        ReservaRequestDTO req = new ReservaRequestDTO();
        req.setCheckIn(checkIn);
        req.setCheckOut(checkOut);
        req.setAdultos(1);
        req.setNinos(0);
        req.setHabitacionId(1L);
        req.setEmailTitular("huesped@test.com");
        req.setCodigoVerificacion("123456");
        req.setExtrasCodigos(extras);

        HuespedDTO huesped = new HuespedDTO();
        huesped.setNombre("Juan");
        huesped.setApellido("Quispe");
        huesped.setTipo("titular");
        req.setHuespedes(List.of(huesped));

        return req;
    }

    private void mockDependencias(ReservaRequestDTO req) {
        lenient().when(codigoService.validar(anyString(), anyString())).thenReturn(codigoValido);
        lenient().when(habitacionRepo.findByIdWithLock(1L)).thenReturn(Optional.of(habitacionLibre));
        lenient().when(habitacionRepo.existeSolape(any(), any(), any(), any())).thenReturn(false);
        lenient().when(extraRepo.findByCodigoIn(anyList())).thenReturn(Collections.emptyList());
        lenient().when(reservaRepo.existsByCodigo(anyString())).thenReturn(false);
    }
}