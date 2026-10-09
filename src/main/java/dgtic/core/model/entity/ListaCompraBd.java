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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "lista_compra")
@Table(name = "lista_compra")
public class ListaCompraBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idItem;

    // Lado "N" de la relacion 1:N hogar -> lista_compra.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hogar", nullable = false)
    private HogarBd hogar;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "comprado")
    private Boolean comprado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
