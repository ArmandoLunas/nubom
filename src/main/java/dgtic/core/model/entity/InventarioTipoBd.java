package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "inventario_tipo")
@Table(name = "inventario_tipo")
// Defensa adicional para cuando esta entidad se carga como referencia LAZY
// (lado "1" de inventario.tipo).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventarioTipoBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipo;

    @Column(name = "nombre")
    private String nombre; // Refrigerador, Alacena

    @Column(name = "descripcion")
    private String descripcion;

    // Clase de icono (bootstrap-icons) para representar el tipo visualmente
    @Column(name = "icono")
    private String icono;

    // Lado "1" de la relacion 1:N inventario_tipo -> inventario.
    // No se usa cascade aqui a proposito: borrar un tipo de inventario en uso
    // debe rechazarse (conflicto de integridad), no arrastrar inventarios.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "tipo")
    private List<InventarioBd> inventarios = new ArrayList<>();
}
