
CREATE TYPE habitacion_tipo    AS ENUM ('simple','doble','matrimonial','triple','cuadruple');
CREATE TYPE habitacion_estado  AS ENUM ('libre','pendiente','ocupada','mantenimiento');
CREATE TYPE reserva_estado     AS ENUM ('pendiente','confirmada','cancelada');
CREATE TYPE pago_estado        AS ENUM ('PENDIENTE','MITAD','COMPLETO');
CREATE TYPE huesped_tipo       AS ENUM ('titular','acompanante');
CREATE TYPE usuario_rol        AS ENUM ('ROLE_ADMIN','ROLE_CLIENT');
CREATE TYPE verificacion_tipo  AS ENUM ('EMAIL', 'WHATSAPP');

CREATE CAST (character varying AS verificacion_tipo) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS habitacion_tipo) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS habitacion_estado) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS reserva_estado) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS pago_estado) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS huesped_tipo) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS usuario_rol) WITH INOUT AS IMPLICIT;

-- ── HABITACIONES ─────────────────────────────────────────────
CREATE TABLE habitaciones (
                              id          BIGSERIAL PRIMARY KEY,
                              numero      VARCHAR(10)      NOT NULL UNIQUE,
                              nombre      VARCHAR(100)     NOT NULL,
                              tipo        habitacion_tipo  NOT NULL,
                              capacidad   INT              NOT NULL,
                              precio_base DECIMAL(10,2)    NOT NULL,
                              size_m2     INT,
                              camas       VARCHAR(100),
                              estado      habitacion_estado NOT NULL DEFAULT 'libre',
                              amenidades  JSONB            NOT NULL DEFAULT '{}',
                              imagenes    TEXT[]           DEFAULT '{}',
                              version     INT              NOT NULL DEFAULT 0,
                              created_at  TIMESTAMP        NOT NULL DEFAULT NOW()
);

-- ── EXTRAS ───────────────────────────────────────────────────
CREATE TABLE extras (
                        id           BIGSERIAL PRIMARY KEY,
                        codigo       VARCHAR(30)   NOT NULL UNIQUE,
                        nombre       VARCHAR(100)  NOT NULL,
                        icono        VARCHAR(50),
                        precio_noche DECIMAL(10,2) NOT NULL
);

-- ── USUARIOS ─────────────────────────────────────────────────
CREATE TABLE usuarios (
                          id        BIGSERIAL PRIMARY KEY,
                          email     VARCHAR(150) NOT NULL UNIQUE,
                          password  VARCHAR(255) NOT NULL,
                          nombre    VARCHAR(100) NOT NULL,
                          rol       usuario_rol  NOT NULL DEFAULT 'ROLE_CLIENT',
                          activo    BOOLEAN      NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── RESERVAS ─────────────────────────────────────────────────
CREATE TABLE reservas (
                          id             BIGSERIAL PRIMARY KEY,
                          codigo         VARCHAR(20)    NOT NULL UNIQUE,
                          habitacion_id  BIGINT         NOT NULL REFERENCES habitaciones(id),
                          check_in       DATE           NOT NULL,
                          check_out      DATE           NOT NULL,
                          noches         INT            NOT NULL,
                          adultos        INT            NOT NULL DEFAULT 1,
                          ninos          INT            NOT NULL DEFAULT 0,
                          estado         reserva_estado NOT NULL DEFAULT 'pendiente',
                          pago_estado    pago_estado    NOT NULL DEFAULT 'PENDIENTE',
                          subtotal       DECIMAL(10,2)  NOT NULL,
                          impuestos      DECIMAL(10,2)  NOT NULL,
                          total          DECIMAL(10,2)  NOT NULL,
                          origen         VARCHAR(10)    DEFAULT 'WEB',
                          created_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── HUÉSPEDES ────────────────────────────────────────────────
CREATE TABLE huespedes (
                           id               BIGSERIAL PRIMARY KEY,
                           reserva_id       BIGINT       NOT NULL REFERENCES reservas(id) ON DELETE CASCADE,
                           tipo             huesped_tipo NOT NULL DEFAULT 'titular',
                           nombre           VARCHAR(100) NOT NULL,
                           apellido         VARCHAR(100) NOT NULL,
                           documento_tipo   VARCHAR(20),
                           documento        VARCHAR(50),
                           edad             INT,
                           sexo             VARCHAR(20),
                           nacionalidad     VARCHAR(80),
                           email            VARCHAR(150),
                           codigo_pais      VARCHAR(10),
                           telefono         VARCHAR(30),
                           peticion_especial TEXT
);

-- ── RESERVA_EXTRAS ───────────────────────────────────────────
CREATE TABLE reserva_extras (
                                reserva_id BIGINT NOT NULL REFERENCES reservas(id) ON DELETE CASCADE,
                                extra_id   BIGINT NOT NULL REFERENCES extras(id),
                                PRIMARY KEY (reserva_id, extra_id)
);

-- ── MENSAJES DE CONTACTO ─────────────────────────────────────
CREATE TABLE mensajes_contacto (
                                   id         BIGSERIAL PRIMARY KEY,
                                   nombre     VARCHAR(150) NOT NULL,
                                   email      VARCHAR(150) NOT NULL,
                                   telefono   VARCHAR(30),
                                   asunto     VARCHAR(200) NOT NULL,
                                   mensaje    TEXT         NOT NULL,
                                   leido      BOOLEAN      NOT NULL DEFAULT FALSE,
                                   respondido BOOLEAN      NOT NULL DEFAULT FALSE,
                                   created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── SISTEMA DE VERIFICACIÓN ──────────────────────────────────
CREATE TABLE verificacion_contactos (
                                        id          BIGSERIAL PRIMARY KEY,
                                        tipo        verificacion_tipo NOT NULL,
                                        valor       VARCHAR(150)      NOT NULL,
                                        codigo_pais VARCHAR(10),
                                        created_at  TIMESTAMP         NOT NULL DEFAULT NOW(),
                                        UNIQUE(tipo, valor)
);

CREATE TABLE codigos_verificacion (
                                      id          BIGSERIAL PRIMARY KEY,
                                      contacto_id BIGINT    NOT NULL REFERENCES verificacion_contactos(id),
                                      codigo      VARCHAR(6) NOT NULL,
                                      usado       BOOLEAN   NOT NULL DEFAULT FALSE,
                                      expira_en   TIMESTAMP NOT NULL,
                                      created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── TABLA IDEMPOTENCIA (evita duplicados) ────────────────────
CREATE TABLE idempotency_log (
                                 id              BIGSERIAL PRIMARY KEY,
                                 idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                                 response_data   JSONB        NOT NULL,
                                 created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 expires_at      TIMESTAMP    NOT NULL DEFAULT (NOW() + INTERVAL '1 day')
);

-- ── ÍNDICES ──────────────────────────────────────────────────
CREATE INDEX idx_mensajes_contacto_email ON mensajes_contacto(email);
CREATE INDEX idx_mensajes_contacto_leido ON mensajes_contacto(leido);
CREATE INDEX idx_verificacion_contactos_valor ON verificacion_contactos(valor);
CREATE INDEX idx_codigos_contacto ON codigos_verificacion(contacto_id);
CREATE INDEX idx_codigos_codigo ON codigos_verificacion(codigo);
CREATE INDEX idx_idempotency_key ON idempotency_log(idempotency_key);
CREATE INDEX idx_expires_at ON idempotency_log(expires_at);
CREATE INDEX idx_reservas_estado    ON reservas(estado);
CREATE INDEX idx_reservas_check_in  ON reservas(check_in);
CREATE INDEX idx_habitaciones_estado ON habitaciones(estado);
CREATE INDEX idx_habitaciones_capacidad ON habitaciones(capacidad);
CREATE INDEX idx_usuarios_activo    ON usuarios(activo);

-- ── COCHERA (HU26) ──────────────────────────────────────────
CREATE TYPE vehiculo_tipo   AS ENUM ('AUTO','MOTO','CAMIONETA');
CREATE TYPE espacio_estado  AS ENUM ('LIBRE','OCUPADO');
CREATE CAST (character varying AS vehiculo_tipo)  WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS espacio_estado) WITH INOUT AS IMPLICIT;

CREATE TABLE vehiculos (
                           id         BIGSERIAL PRIMARY KEY,
                           placa      VARCHAR(20)   NOT NULL UNIQUE,
                           marca      VARCHAR(50)   NOT NULL,
                           modelo     VARCHAR(50)   NOT NULL,
                           color      VARCHAR(30),
                           tipo       vehiculo_tipo NOT NULL DEFAULT 'AUTO',
                           created_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE espacios_cochera (
                                  id         BIGSERIAL PRIMARY KEY,
                                  codigo     VARCHAR(10)    NOT NULL UNIQUE,
                                  ubicacion  VARCHAR(100),
                                  estado     espacio_estado NOT NULL DEFAULT 'LIBRE',
                                  created_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE registros_cochera (
                                   id            BIGSERIAL PRIMARY KEY,
                                   vehiculo_id   BIGINT  NOT NULL REFERENCES vehiculos(id),
                                   espacio_id    BIGINT  NOT NULL REFERENCES espacios_cochera(id),
                                   usuario_id    BIGINT  NOT NULL REFERENCES usuarios(id),
                                   reserva_id    BIGINT  REFERENCES reservas(id),
                                   fecha_ingreso TIMESTAMP NOT NULL DEFAULT NOW(),
                                   fecha_salida  TIMESTAMP,
                                   observacion   TEXT,
                                   created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_registros_cochera_vehiculo ON registros_cochera(vehiculo_id);
CREATE INDEX idx_registros_cochera_espacio  ON registros_cochera(espacio_id);
CREATE INDEX idx_registros_cochera_usuario  ON registros_cochera(usuario_id);
CREATE INDEX idx_registros_cochera_activos  ON registros_cochera(espacio_id) WHERE fecha_salida IS NULL;

-- ═══════════════════════════════════════════════════════════
-- DATOS SEMILLA
-- ═══════════════════════════════════════════════════════════

-- Extras
INSERT INTO extras (codigo, nombre, icono, precio_noche) VALUES
                                                             ('buffet', 'Buffet Andino', 'fa-utensils', 15.00),
                                                             ('cochera', 'Cochera', 'fa-car', 10.00),
                                                             ('spa', 'Spa', 'fa-spa', 20.00)
    ON CONFLICT (codigo) DO NOTHING;

-- Habitaciones
INSERT INTO habitaciones (numero, nombre, tipo, capacidad, precio_base, size_m2, camas, estado, amenidades, imagenes, version) VALUES
                                                                                                                                   ('101', 'Suite Simple Illimani', 'simple', 1, 60.00, 16, '1 cama twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":false,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/101/simple1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/101/simple1bano.jpeg'], 1),

                                                                                                                                   ('102', 'Suite Simple Sajama', 'simple', 1, 60.00, 16, '1 cama twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":false,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/102/simple2-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/102/simple2-1bano.jpeg'], 2),

                                                                                                                                   ('103', 'Suite Simple Coropuna', 'simple', 1, 60.00, 17, '1 cama twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":false,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/103/simple2-2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/103/simple2-2bano.jpeg'], 3),

                                                                                                                                   ('104', 'Suite Simple Ausangate', 'simple', 1, 60.00, 17, '1 cama twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":false,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/104/simple4.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/104/simple4bano.jpeg'], 4),

                                                                                                                                   ('201', 'Suite Doble Titicaca', 'doble', 2, 90.00, 26, '2 camas twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":false,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/201/doble1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/201/doble1bano.jpeg'], 5),

                                                                                                                                   ('202', 'Suite Matrimonial Collasuyo', 'matrimonial', 2, 90.00, 28, '1 cama queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/202/doble1-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/202/doble1-1bano.jpeg'], 6),

                                                                                                                                   ('203', 'Suite Matrimonial Tawantinsuyo', 'matrimonial', 2, 90.00, 28, '1 cama queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/203/doble1-2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/203/doble1-2bano.jpeg'], 7),

                                                                                                                                   ('204', 'Suite Matrimonial Antisuyo', 'matrimonial', 2, 90.00, 30, '1 cama king', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":false,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/204/doble2-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/204/doble2-1bano.jpeg'], 8),

                                                                                                                                   ('205', 'Suite Matrimonial Contisuyo', 'matrimonial', 2, 95.00, 30, '1 cama king', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/205/doble2-2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/205/doble2-2bano.jpeg'], 9),

                                                                                                                                   ('301', 'Suite Triple Pachamama', 'triple', 3, 140.00, 40, '1 cama queen + 1 cama twin', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/301/triple2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/301/triple2-bano.jpeg'], 10),

                                                                                                                                   ('401', 'Suite Cuádruple Wiracocha', 'cuadruple', 4, 160.00, 50, '2 camas queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/401/cuadruple1-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/401/cuadruple1-2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/401/cuadruple1bano.jpeg'], 11),

                                                                                                                                   ('402', 'Suite Cuádruple Inti', 'cuadruple', 4, 160.00, 50, '2 camas queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":false}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/402/cuadruple3-2.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/402/cuadruple3-2bano.jpeg'], 12),

                                                                                                                                   ('403', 'Suite Cuádruple Mama Quilla', 'cuadruple', 4, 165.00, 52, '2 camas queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":true}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/403/cuadruple3-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/403/cuadruple3-1bano.jpeg'], 13),

                                                                                                                                   ('404', 'Suite Cuádruple Supay', 'cuadruple', 4, 165.00, 52, '2 camas queen', 'libre',
                                                                                                                                    '{"internet":true,"cable":true,"banioPrivado":true,"buffetAndino":true,"cochera":true,"spa":true}',
                                                                                                                                    ARRAY['https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/404/cuadruple1-1.jpeg', 'https://czfxbrtdiiackpacflfm.supabase.co/storage/v1/object/public/rooms/404/cuadruple1-1bano.jpeg'], 14)
    ON CONFLICT (numero) DO NOTHING;

-- Usuario administrador (contraseña: admin123, debes generar el BCrypt real)
INSERT INTO usuarios (email, password, nombre, rol, activo) VALUES (
                                                                       'admin@pachasuite.com',
                                                                       'PLACEHOLDER',
                                                                       'Administrador Pacha',
                                                                       'ROLE_ADMIN',
                                                                       TRUE
                                                                   ) ON CONFLICT (email) DO NOTHING;