package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "inventario")
@Table(name = "inventario")
// Defensa adicional para cuando esta entidad se carga como referencia LAZY
// (lado "1" de producto.inventario).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventarioBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInventario;

    // Lado "N" de la relacion 1:N hogar -> inventario. Pareja de
    // HogarBd.inventarios (@JsonManagedReference): @JsonBackReference oculta
    // este campo al serializar (evita el ciclo hogar->inventario->hogar->...).
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hogar", nullable = false)
    private HogarBd hogar;

    // Lado "N" de la relacion 1:N inventario_tipo -> inventario.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", nullable = false)
    private InventarioTipoBd tipo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima; // capacidad en NUMERO DE OBJETOS (no litros/kg)

    // Estilo visual con el que se dibuja el inventario: clasico, moderno o retro.
    @Builder.Default
    @Column(name = "estilo", length = 20)
    private String estilo = "clasico";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Lado "1" de la relacion 1:N inventario -> producto.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoBd> productos = new ArrayList<>();
}
