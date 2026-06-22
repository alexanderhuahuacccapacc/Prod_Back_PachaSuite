package com.pachasuite.api.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    private static final int   MAX_WIDTH = 1600;
    private static final float QUALITY   = 0.80f;

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
        // 1. Optimizar antes de subir (resize + compresión)
        byte[] imagenOptimizada = optimizarImagen(file);

        // 2. Nombre único — siempre .jpg porque forzamos ese formato al optimizar
        String fileName  = habitacionNumero + "/" + UUID.randomUUID() + ".jpg";
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("x-upsert", "true");
        headers.setContentType(MediaType.IMAGE_JPEG);
        HttpEntity<byte[]> entity = new HttpEntity<>(imagenOptimizada, headers);
        RestTemplate rt = buildClient();
        rt.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
        log.info("Imagen optimizada y subida a Supabase: {} ({} KB -> {} KB)",
                publicUrl, file.getSize() / 1024, imagenOptimizada.length / 1024);
        return publicUrl;
    }

    private byte[] optimizarImagen(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(MAX_WIDTH, MAX_WIDTH)
                .outputQuality(QUALITY)
                .outputFormat("jpg")
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }
    public void eliminarImagen(String url) {
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
}