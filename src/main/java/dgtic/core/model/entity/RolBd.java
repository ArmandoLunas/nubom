package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "rol")
@Table(name = "rol")
// Defensa adicional para cuando esta entidad se carga como referencia LAZY
// (lado "1" de usuario_hogar.rol): ignora los campos internos del proxy de
// Hibernate si alguna vez se serializara sin pasar por un DTO.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RolBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(name = "nombre")
    private String nombre; // PROPIETARIO o FAMILIAR

    @Column(name = "descripcion")
    private String descripcion;
}
