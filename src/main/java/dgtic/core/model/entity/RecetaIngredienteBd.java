package dgtic.core.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "receta_ingrediente")
@Table(name = "receta_ingrediente")
public class RecetaIngredienteBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idIngrediente;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private RecetaBd receta;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "unidad", length = 20)
    private String unidad;

    // true = ingrediente principal; false = complementario.
    @Column(name = "principal", nullable = false)
    private Boolean principal;
}
