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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CocheraService {

    private final VehiculoRepository vehiculoRepo;
    private final EspacioCocheraRepository espacioRepo;
    private final RegistroCocheraRepository registroRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReservaRepository reservaRepo;

    // ── Vehículos ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VehiculoDTO> listarVehiculos() {
        return vehiculoRepo.findAll()
                .stream()
                .map(VehiculoDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculoDTO obtenerVehiculo(Long id) {
        return vehiculoRepo.findById(id)
                .map(VehiculoDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));
    }

    // ── Espacios ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EspacioCocheraDTO> listarEspacios() {
        return espacioRepo.findAll()
                .stream()
                .map(EspacioCocheraDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EspacioCocheraDTO obtenerEspacio(Long id) {
        return espacioRepo.findById(id)
                .map(EspacioCocheraDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio de cochera", "id", id));
    }

    @Transactional
    public EspacioCocheraDTO crearEspacio(EspacioCocheraRequest request) {
        if (espacioRepo.existsByCodigo(request.getCodigo())) {
            throw new BadRequestException("Ya existe un espacio con el código: " + request.getCodigo());
        }
        EspacioCochera espacio = EspacioCochera.builder()
                .codigo(request.getCodigo().toUpperCase())
                .ubicacion(request.getUbicacion())
                .build();
        EspacioCochera saved = espacioRepo.save(espacio);
        log.info("Espacio de cochera creado: {}", saved.getCodigo());
        return EspacioCocheraDTO.from(saved);
    }

    @Transactional
    public EspacioCocheraDTO actualizarEspacio(Long id, EspacioCocheraRequest request) {
        EspacioCochera espacio = espacioRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio de cochera", "id", id));
        if (request.getCodigo() != null) {
            espacio.setCodigo(request.getCodigo().toUpperCase());
        }
        if (request.getUbicacion() != null) {
            espacio.setUbicacion(request.getUbicacion());
        }
        EspacioCochera saved = espacioRepo.save(espacio);
        log.info("Espacio de cochera actualizado: {}", saved.getId());
        return EspacioCocheraDTO.from(saved);
    }

    // ── Registros (IN/OUT) ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RegistroCocheraDTO> listarRegistros() {
        return registroRepo.findAllOrderByFechaIngresoDesc()
                .stream()
                .map(RegistroCocheraDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroCocheraDTO> listarActivos() {
        return registroRepo.findActivos()
                .stream()
                .map(RegistroCocheraDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegistroCocheraDTO obtenerRegistro(Long id) {
        return registroRepo.findById(id)
                .map(RegistroCocheraDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de cochera", "id", id));
    }

    @Transactional
    public RegistroCocheraDTO registrarIngreso(IngresoCocheraRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepo.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        Vehiculo vehiculo = vehiculoRepo.findByPlaca(request.getPlaca().toUpperCase())
                .orElseGet(() -> {
                    Vehiculo nuevo = Vehiculo.builder()
                            .placa(request.getPlaca().toUpperCase())
                            .marca(request.getMarca())
                            .modelo(request.getModelo())
                            .color(request.getColor())
                            .tipo(request.getTipo() != null
                                    ? Vehiculo.VehiculoTipo.valueOf(request.getTipo())
                                    : Vehiculo.VehiculoTipo.AUTO)
                            .build();
                    return vehiculoRepo.save(nuevo);
                });

        EspacioCochera espacio = espacioRepo.findById(request.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio de cochera", "id", request.getEspacioId()));

        if (espacio.getEstado() == EspacioCochera.EspacioEstado.OCUPADO) {
            throw new BadRequestException("El espacio " + espacio.getCodigo() + " ya está ocupado");
        }

        espacio.setEstado(EspacioCochera.EspacioEstado.OCUPADO);
        espacioRepo.save(espacio);

        RegistroCochera.RegistroCocheraBuilder builder = RegistroCochera.builder()
                .vehiculo(vehiculo)
                .espacio(espacio)
                .usuario(usuario)
                .fechaIngreso(LocalDateTime.now())
                .observacion(request.getObservacion());

        if (request.getReservaId() != null) {
            builder.reserva(reservaRepo.getReferenceById(request.getReservaId()));
        }

        RegistroCochera saved = registroRepo.save(builder.build());
        log.info("Ingreso de vehículo {} al espacio {}", vehiculo.getPlaca(), espacio.getCodigo());
        return RegistroCocheraDTO.from(saved);
    }

    @Transactional
    public RegistroCocheraDTO registrarSalida(Long id, SalidaCocheraRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepo.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        RegistroCochera registro = registroRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de cochera", "id", id));

        if (registro.getFechaSalida() != null) {
            throw new BadRequestException("El vehículo ya registró su salida");
        }

        registro.setFechaSalida(LocalDateTime.now());

        if (request.getObservacion() != null) {
            registro.setObservacion(registro.getObservacion() != null
                    ? registro.getObservacion() + " | Salida: " + request.getObservacion()
                    : "Salida: " + request.getObservacion());
        }

        EspacioCochera espacio = registro.getEspacio();
        espacio.setEstado(EspacioCochera.EspacioEstado.LIBRE);
        espacioRepo.save(espacio);

        RegistroCochera saved = registroRepo.save(registro);
        log.info("Salida de vehículo {} del espacio {} (registrado por {})",
                registro.getVehiculo().getPlaca(), espacio.getCodigo(), usuario.getNombre());
        return RegistroCocheraDTO.from(saved);
    }
}