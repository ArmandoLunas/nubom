package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Entity(name = "hogar")
@Table(name = "hogar")
// Defensa adicional para cuando esta entidad se carga como referencia LAZY
// (lado "1" de inventario.hogar, lista_compra.hogar y usuario_hogar.hogar).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HogarBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHogar;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Lado "1" de la relacion 1:N hogar -> inventario.
    // Se usa el par @JsonManagedReference (aqui) / @JsonBackReference (en
    // InventarioBd.hogar) a modo de DEMOSTRACION del mecanismo clasico de
    // Jackson para relaciones bidireccionales: si esta entidad se serializara
    // directamente (sin pasar por HogarResponseDTO), "inventarios" SI viajaria
    // en el JSON pero cada inventario dejaria de repetir "hogar" (evitando la
    // recursion infinita hogar->inventario->hogar->...).
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "hogar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventarioBd> inventarios = new ArrayList<>();

    // Lado "1" de la relacion 1:N hogar -> lista_compra. Aqui se usa
    // @JsonIgnore (en vez del par Managed/Back) como segunda forma valida de
    // resolver el mismo problema de recursividad, mas simple cuando no
    // interesa exponer la coleccion completa ni siquiera en un escenario de
    // serializacion directa.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "hogar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListaCompraBd> listaCompra = new ArrayList<>();
}
