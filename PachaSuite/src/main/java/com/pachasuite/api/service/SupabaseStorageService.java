package com.pachasuite.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private RestTemplate buildClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();
        var factory = new HttpComponentsClientHttpRequestFactory(
                HttpClients.custom().setDefaultRequestConfig(config).build());
        return new RestTemplate(factory);
    }

    public String subirImagen(MultipartFile file, String habitacionNumero) throws IOException {
        // nombre único para evitar colisiones
        String extension = obtenerExtension(file.getOriginalFilename());
        String fileName  = habitacionNumero + "/" + UUID.randomUUID() + "." + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("x-upsert", "true");
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "image/jpeg"));

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        RestTemplate rt = buildClient();
        rt.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        // retornar URL pública
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
        log.info("Imagen subida a Supabase: {}", publicUrl);
        return publicUrl;
    }

    public void eliminarImagen(String url) {
        // extraer el path desde la URL pública
        String path = url.replace(
                supabaseUrl + "/storage/v1/object/public/" + bucket + "/", "");
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);

        RestTemplate rt = buildClient();
        rt.exchange(deleteUrl, HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        log.info("Imagen eliminada de Supabase: {}", path);
    }

    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}