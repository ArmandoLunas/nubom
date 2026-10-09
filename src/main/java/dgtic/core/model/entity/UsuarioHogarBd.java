package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Tabla intermedia de la relacion N:M usuario <-> hogar (un usuario puede
// pertenecer a varios hogares a lo largo del tiempo y un hogar tiene varios
// usuarios), con el rol como atributo propio de la asociacion. Al llevar un
// atributo (id_rol, fecha_union, activo) se modela como entidad de union con
// @ManyToOne hacia ambos lados, en vez de un simple @ManyToMany, siguiendo la
// practica recomendada de JPA/Hibernate para relaciones N:M "con carga".
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "usuario_hogar")
@Table(name = "usuario_hogar")
public class UsuarioHogarBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuarioHogar;

    // Lado "N" hacia usuario de la relacion N:M usuario <-> hogar.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioBd usuario;

    // Lado "N" hacia hogar de la relacion N:M usuario <-> hogar.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hogar", nullable = false)
    private HogarBd hogar;

    // Rol del usuario dentro de ESE hogar (atributo propio de la asociacion).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolBd rol;

    @Column(name = "fecha_union")
    private LocalDateTime fechaUnion;

    @Column(name = "activo")
    private Boolean activo;
}
