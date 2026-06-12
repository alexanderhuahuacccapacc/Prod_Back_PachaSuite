package com.pachasuite.api.service;

import com.pachasuite.api.dto.MensajeContactoRequest;
import com.pachasuite.api.dto.MensajeContactoResponse;
import com.pachasuite.api.entities.MensajeContacto;
import com.pachasuite.api.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensajeContactoService {

    private final MensajeContactoRepository repository;
    private final EmailService emailService;

    @Value("${app.mail.destinatario-contacto:reservas@pachasuite.com}")
    private String destinatarioAdmin;

    public MensajeContactoResponse guardar(MensajeContactoRequest request) {
        MensajeContacto entidad = MensajeContacto.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .asunto(request.getAsunto())
                .mensaje(request.getMensaje())
                .build();

        MensajeContacto guardado = repository.save(entidad);

        try {
            emailService.enviarNotificacionContacto(
                    destinatarioAdmin,
                    request.getNombre(),
                    request.getEmail(),
                    request.getTelefono(),
                    request.getAsunto(),
                    request.getMensaje()
            );
        } catch (Exception ex) {
            log.warn("Mensaje guardado pero no se pudo enviar email de notificación: {}", ex.getMessage());
        }

        return toResponse(guardado);
    }

    public List<MensajeContactoResponse> listarTodos() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MensajeContactoResponse> listarNoLeidos() {
        return repository.findByLeidoFalseOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public MensajeContactoResponse marcarLeido(Long id) {
        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado: " + id));
        msg.setLeido(true);
        return toResponse(repository.save(msg));
    }

    public long contarNoLeidos() {
        return repository.countByLeidoFalse();
    }

    public void marcarTodosLeidos() {
        List<MensajeContacto> pendientes = repository.findByLeidoFalseOrderByCreatedAtDesc();
        pendientes.forEach(m -> m.setLeido(true));
        repository.saveAll(pendientes);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Mensaje no encontrado: " + id);
        repository.deleteById(id);
    }

    private MensajeContactoResponse toResponse(MensajeContacto e) {
        return MensajeContactoResponse.builder()
                .id(e.getId()).nombre(e.getNombre()).email(e.getEmail())
                .telefono(e.getTelefono()).asunto(e.getAsunto())
                .mensaje(e.getMensaje()).leido(e.getLeido())
                .respondido(e.getRespondido())
                .createdAt(e.getCreatedAt()).build();
    }

    public void responder(Long id, String cuerpo) {
        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado: " + id));
        emailService.enviarRespuestaContacto(msg.getEmail(), msg.getNombre(), msg.getAsunto(), cuerpo);
        msg.setLeido(true);
        msg.setRespondido(true);
        repository.save(msg);
    }
    public List<MensajeContactoResponse> listarRespondidos() {
        return repository.findByRespondidoTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }
}