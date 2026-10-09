package dgtic.core.model.entity;

import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OnDelete;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Aviso de caducidad dirigido a un usuario.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "notificacion")
@Table(name = "notificacion")
public class NotificacionBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificacion;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioBd usuario;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductoBd producto;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "leida", nullable = false)
    private Boolean leida;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
