package dgtic.core.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Una calificacion por usuario y receta (restriccion unica).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "receta_calificacion")
@Table(name = "receta_calificacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_calificacion_receta_usuario",
                columnNames = {"id_receta", "id_usuario"}))
public class RecetaCalificacionBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCalificacion;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private RecetaBd receta;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioBd usuario;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    @Column(name = "fecha")
    private LocalDateTime fecha;
}
