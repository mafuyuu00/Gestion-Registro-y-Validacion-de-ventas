package cl.duoc.msventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El tipo de convenio o servicio es obligatorio")
    private String tipoServicio;

    @NotBlank(message = "La fecha de operacion es obligatoria")
    private String fechaOperacion;

    @NotBlank(message = "El cliente ficticio es obligatorio")
    private String cliente;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que 0")
    private Double monto;

    @NotBlank(message = "El codigo de autorizacion es obligatorio")
    @Column(unique = true)
    private String codigoAutorizacion;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(String fechaOperacion) { this.fechaOperacion = fechaOperacion; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getCodigoAutorizacion() { return codigoAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
