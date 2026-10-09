package dgtic.core.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "producto")
@Table(name = "producto")
public class ProductoBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    // Lado "N" de la relacion 1:N inventario -> producto.
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventario", nullable = false)
    private InventarioBd inventario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "codigo_barras", length = 13)
    private String codigoBarras;

    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    @Column(name = "activo")
    private Boolean activo;

    // Dias que faltan para la caducidad (negativo si ya vencio); null si no tiene fecha.
    public Long getDiasParaCaducar() {
        return fechaCaducidad == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), fechaCaducidad);
    }

    // Semaforo de caducidad: SIN_FECHA, VENCIDO, POR_VENCER (3 dias o menos) o VIGENTE.
    public String getEstadoCaducidad() {
        Long dias = getDiasParaCaducar();
        if (dias == null) return "SIN_FECHA";
        if (dias < 0) return "VENCIDO";
        return dias <= 3 ? "POR_VENCER" : "VIGENTE";
    }
}
