package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "usuario")
@Table(name = "usuario")
// Defensa adicional para cuando esta entidad se carga como referencia LAZY
// (lado "1" de usuario_hogar.usuario): si alguna vez se serializara por error
// sin pasar por un DTO, se ignoran los campos internos del proxy de Hibernate.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsuarioBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "correo", unique = true)
    private String correo;

    // Contrasena en texto plano SOLO para esta practica. Nunca debe viajar en
    // una respuesta JSON: @JsonIgnore la excluye incluso si esta entidad se
    // serializara directamente (la API REST ademas usa UsuarioResponseDTO,
    // que ni siquiera declara este campo).
    @JsonIgnore
    @Column(name = "contrasena")
    private String contrasena;

    // Rol global en la plataforma (USUARIO o ADMIN). El rol dentro de un hogar
    // (PROPIETARIO / FAMILIAR) vive en usuario_hogar, porque depende del hogar.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_sistema", nullable = false, length = 20)
    private RolSistema rolSistema = RolSistema.USUARIO;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "activo")
    private Boolean activo;
}
