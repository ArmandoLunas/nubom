package dgtic.core.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "receta")
@Table(name = "receta")
@EntityListeners(AuditingEntityListener.class)
public class RecetaBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReceta;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioBd autor;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    // Un paso por linea.
    @Column(name = "pasos", columnDefinition = "TEXT")
    private String pasos;

    @Column(name = "tiempo_minutos")
    private Integer tiempoMinutos;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReceta estado = EstadoReceta.PRIVADA;

    // Auditoria con Spring Data JPA (ver @EnableJpaAuditing en la clase principal).
    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Lado "1" de receta -> ingredientes: se guardan y eliminan junto con la receta.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("principal DESC, idIngrediente ASC")
    private List<RecetaIngredienteBd> ingredientes = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "receta", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RecetaCalificacionBd> calificaciones = new ArrayList<>();

    public List<String> getListaPasos() {
        if (pasos == null || pasos.isBlank()) {
            return List.of();
        }
        return pasos.lines().map(String::trim).filter(l -> !l.isEmpty()).toList();
    }
}
