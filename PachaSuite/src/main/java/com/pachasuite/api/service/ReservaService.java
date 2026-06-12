package com.pachasuite.api.service;

import com.pachasuite.api.dto.*;
import com.pachasuite.api.entities.*;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.exception.ResourceNotFoundException;
import com.pachasuite.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaService {

    private static final BigDecimal TAX_RATE   = new BigDecimal("0.18");
    private static final int        MAX_RETRIES = 10;

    private final ReservaRepository    reservaRepo;
    private final HabitacionRepository habitacionRepo;
    private final ExtraRepository      extraRepo;
    private final CodigoService        codigoService;
    private final SecureRandom         secureRandom = new SecureRandom();

    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO req) {
        validarFechas(req.getCheckIn(), req.getCheckOut());

        String email = req.getEmailTitular();
        log.debug("🔍 Validando código para email: {}", email);

        CodigoVerificacion cv = codigoService.validar(email, req.getCodigoVerificacion());

        Habitacion hab = habitacionRepo.findByIdWithLock(req.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Habitacion", "id", req.getHabitacionId()));

        if (hab.getEstado() != Habitacion.HabitacionEstado.libre) {
            throw new BadRequestException(
                    "La habitación " + hab.getNumero() + " no está disponible. Estado: " + hab.getEstado());
        }

        validarSolape(hab.getId(), req.getCheckIn(), req.getCheckOut(), null);
        int            noches = calcularNoches(req.getCheckIn(), req.getCheckOut());
        Set<Extra>     extras = resolverExtras(req.getExtrasCodigos());
        BigDecimal[]   tot    = calcularTotales(hab.getPrecioBase(), sumarPrecioExtras(extras), noches);
        String         codigo = generarCodigoUnico(req.getCheckIn().getYear());

        Reserva r = Reserva.builder()
                .codigo(codigo).habitacion(hab)
                .checkIn(req.getCheckIn()).checkOut(req.getCheckOut())
                .noches(noches).adultos(req.getAdultos()).ninos(req.getNinos())
                .estado(Reserva.ReservaEstado.pendiente)
                .pagoEstado(Reserva.PagoEstado.PENDIENTE)
                .subtotal(tot[0]).impuestos(tot[1]).total(tot[2])
                .origen("WEB").extras(extras).build();

        req.getHuespedes().stream()
                .map(d -> buildHuesped(d, r))
                .forEach(r.getHuespedes()::add);

        Reserva saved = reservaRepo.save(r);
        hab.setEstado(Habitacion.HabitacionEstado.pendiente);
        habitacionRepo.save(hab);
        codigoService.marcarUsado(cv);

        log.info("Reserva WEB: {} | Hab {} | ${} | verificado por email: {}",
                codigo, hab.getNumero(), tot[2], email);
        return ReservaResponseDTO.from(saved);
    }

    @Transactional
    public ReservaResponseDTO crearReservaAdmin(AdminReservaRequestDTO req) {
        validarFechas(req.getCheckIn(), req.getCheckOut());

        Habitacion hab = habitacionRepo.findByIdWithLock(req.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Habitacion", "id", req.getHabitacionId()));

        validarSolape(hab.getId(), req.getCheckIn(), req.getCheckOut(), null);
        int          noches = calcularNoches(req.getCheckIn(), req.getCheckOut());
        Set<Extra>   extras = resolverExtras(req.getExtrasCodigos());
        BigDecimal[] tot    = calcularTotales(hab.getPrecioBase(), sumarPrecioExtras(extras), noches);
        String       codigo = generarCodigoUnico(req.getCheckIn().getYear());

        Reserva r = Reserva.builder()
                .codigo(codigo).habitacion(hab)
                .checkIn(req.getCheckIn()).checkOut(req.getCheckOut())
                .noches(noches).adultos(req.getAdultos()).ninos(req.getNinos())
                .estado(Reserva.ReservaEstado.confirmada)
                .pagoEstado(parsePagoEstado(req.getPagoEstado()))
                .subtotal(tot[0]).impuestos(tot[1]).total(tot[2])
                .origen("ADMIN").extras(extras).build();

        req.getHuespedes().stream()
                .map(d -> buildHuesped(d, r))
                .forEach(r.getHuespedes()::add);

        Reserva saved = reservaRepo.save(r);
        hab.setEstado(Habitacion.HabitacionEstado.ocupada);
        habitacionRepo.save(hab);
        log.info("Reserva ADMIN: {} | Hab {} | ${}", codigo, hab.getNumero(), tot[2]);
        return ReservaResponseDTO.from(saved);
    }

    @Transactional
    public ReservaResponseDTO editarReserva(Long id, EditarReservaRequestDTO req) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (reserva.getEstado() != Reserva.ReservaEstado.pendiente) {
            throw new BadRequestException(
                    "Solo se pueden editar reservas en estado 'pendiente'. Estado actual: "
                            + reserva.getEstado());
        }

        validarFechas(req.getCheckIn(), req.getCheckOut());
        validarSolape(reserva.getHabitacion().getId(), req.getCheckIn(), req.getCheckOut(), id);

        int          noches = calcularNoches(req.getCheckIn(), req.getCheckOut());
        Set<Extra>   extras = resolverExtras(req.getExtrasCodigos());
        BigDecimal[] tot    = calcularTotales(
                reserva.getHabitacion().getPrecioBase(), sumarPrecioExtras(extras), noches);

        reserva.setCheckIn(req.getCheckIn());
        reserva.setCheckOut(req.getCheckOut());
        reserva.setNoches(noches);
        reserva.setAdultos(req.getAdultos());
        reserva.setNinos(req.getNinos());
        reserva.setSubtotal(tot[0]);
        reserva.setImpuestos(tot[1]);
        reserva.setTotal(tot[2]);
        reserva.getExtras().clear();
        reserva.getExtras().addAll(extras);
        if (req.getPagoEstado() != null) {
            reserva.setPagoEstado(parsePagoEstado(req.getPagoEstado()));
        }

        Reserva saved = reservaRepo.save(reserva);
        log.info("✏️ Reserva {} editada | {} → {} | ${}",
                reserva.getCodigo(), req.getCheckIn(), req.getCheckOut(), tot[2]);
        return ReservaResponseDTO.from(saved);
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Long id) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (reserva.getEstado() == Reserva.ReservaEstado.cancelada) {
            throw new BadRequestException("La reserva ya está cancelada.");
        }

        Reserva.ReservaEstado estadoAnterior = reserva.getEstado();
        reserva.setEstado(Reserva.ReservaEstado.cancelada);
        reservaRepo.save(reserva);

        Habitacion hab = reserva.getHabitacion();
        boolean hayOtrasActivas = reservaRepo.existeOtraReservaActiva(hab.getId(), id);
        if (!hayOtrasActivas) {
            hab.setEstado(Habitacion.HabitacionEstado.libre);
            habitacionRepo.save(hab);
            log.info("Habitación {} liberada tras cancelar reserva {}",
                    hab.getNumero(), reserva.getCodigo());
        }

        log.info("Reserva {} CANCELADA (estado anterior: {})",
                reserva.getCodigo(), estadoAnterior);
        return ReservaResponseDTO.from(reserva);
    }

    @Transactional
    public ReservaResponseDTO confirmar(Long id) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (reserva.getEstado() != Reserva.ReservaEstado.pendiente) {
            throw new BadRequestException(
                    "Solo se pueden confirmar reservas en estado 'pendiente'");
        }

        reserva.setEstado(Reserva.ReservaEstado.confirmada);
        reservaRepo.save(reserva);

        Habitacion h = habitacionRepo.findByIdWithLock(reserva.getHabitacion().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Habitacion", "id", reserva.getHabitacion().getId()));
        h.setEstado(Habitacion.HabitacionEstado.ocupada);
        habitacionRepo.save(h);

        log.info(" Reserva {} CONFIRMADA | Hab {} → ocupada",
                reserva.getCodigo(), h.getNumero());
        return ReservaResponseDTO.from(reserva);
    }

    @Transactional(readOnly = true)
    public ReservaResponseDTO findByCodigo(String codigo) {
        return ReservaResponseDTO.from(
                reservaRepo.findByCodigoWithDetails(codigo)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Reserva", "codigo", codigo)));
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponseDTO> findAll(Pageable pageable) {
        return reservaRepo.findAllWithDetails(pageable)
                .map(ReservaResponseDTO::from);
    }

    private void validarFechas(LocalDate ci, LocalDate co) {
        if (ci == null || co == null)
            throw new BadRequestException("Las fechas son obligatorias.");
        if (!co.isAfter(ci))
            throw new BadRequestException("El check-out debe ser posterior al check-in.");
        if (ci.isBefore(LocalDate.now()))
            throw new BadRequestException("El check-in no puede ser en el pasado.");
    }

    private void validarSolape(Long habId, LocalDate ci, LocalDate co, Long excludeId) {
        if (habitacionRepo.existeSolape(habId, ci, co, excludeId))
            throw new BadRequestException(
                    "La habitación ya tiene una reserva para las fechas seleccionadas.");
    }

    private int calcularNoches(LocalDate ci, LocalDate co) {
        int n = (int) ChronoUnit.DAYS.between(ci, co);
        if (n < 1) throw new BadRequestException("La reserva debe ser de al menos 1 noche.");
        return n;
    }

    private Set<Extra> resolverExtras(List<String> codigos) {
        if (codigos == null || codigos.isEmpty()) return new HashSet<>();
        List<Extra> encontrados = extraRepo.findByCodigoIn(codigos);
        if (encontrados.size() != codigos.size())
            throw new BadRequestException("Uno o más extras no existen.");
        return new HashSet<>(encontrados);
    }

    private BigDecimal sumarPrecioExtras(Set<Extra> extras) {
        return extras.stream()
                .map(Extra::getPrecioNoche)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal[] calcularTotales(BigDecimal base, BigDecimal extra, int noches) {
        BigDecimal sub = base.add(extra).multiply(BigDecimal.valueOf(noches));
        BigDecimal imp = sub.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal[]{sub, imp, sub.add(imp).setScale(2, RoundingMode.HALF_UP)};
    }

    private Reserva.PagoEstado parsePagoEstado(String val) {
        if (val == null) return Reserva.PagoEstado.PENDIENTE;
        try { return Reserva.PagoEstado.valueOf(val); }
        catch (IllegalArgumentException e) { return Reserva.PagoEstado.PENDIENTE; }
    }

    private String generarCodigoUnico(int year) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String c = "RES-" + year + "-" + String.format("%05d", secureRandom.nextInt(100_000));
            if (!reservaRepo.existsByCodigo(c)) return c;
        }
        throw new RuntimeException("No se pudo generar un código único de reserva.");
    }

    private Huesped buildHuesped(HuespedDTO dto, Reserva reserva) {
        return Huesped.builder()
                .reserva(reserva)
                .tipo((dto.getTipo() != null && dto.getTipo().equalsIgnoreCase("acompanante"))
                        ? Huesped.HuespedTipo.acompanante : Huesped.HuespedTipo.titular)
                .nombre(dto.getNombre()).apellido(dto.getApellido())
                .documentoTipo(dto.getDocumentoTipo()).documento(dto.getDocumento())
                .edad(dto.getEdad()).sexo(dto.getSexo()).nacionalidad(dto.getNacionalidad())
                .email(dto.getEmail()).codigoPais(dto.getCodigoPais())
                .telefono(dto.getTelefono()).peticionEspecial(dto.getPeticionEspecial())
                .build();
    }
}