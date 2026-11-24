package cl.duoc.msventas.service;

import cl.duoc.msventas.model.Venta;
import cl.duoc.msventas.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    public static final String ESTADO_BORRADOR = "Borrador";

    private final VentaRepository repository;

    public VentaService(VentaRepository repository) {
        this.repository = repository;
    }

    public List<Venta> listar() {
        return repository.findAll();
    }

    public Optional<Venta> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Venta crear(Venta venta) {
        if (repository.existsByCodigoAutorizacionIgnoreCase(venta.getCodigoAutorizacion())) {
            throw new IllegalStateException("El codigo de autorizacion ya existe");
        }
        venta.setId(null);
        return repository.save(venta);
    }

    public Optional<Venta> actualizar(Long id, Venta datos) {
        return repository.findById(id).map(existente -> {
            if (repository.existsByCodigoAutorizacionIgnoreCaseAndIdNot(datos.getCodigoAutorizacion(), id)) {
                throw new IllegalStateException("El codigo de autorizacion ya existe");
            }
            existente.setTipoServicio(datos.getTipoServicio());
            existente.setFechaOperacion(datos.getFechaOperacion());
            existente.setCliente(datos.getCliente());
            existente.setMonto(datos.getMonto());
            existente.setCodigoAutorizacion(datos.getCodigoAutorizacion());
            existente.setEstado(datos.getEstado());
            existente.setObservaciones(datos.getObservaciones());
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        Optional<Venta> venta = repository.findById(id);
        if (venta.isEmpty()) {
            return false;
        }
        if (!ESTADO_BORRADOR.equals(venta.get().getEstado())) {
            throw new IllegalStateException("Solo se pueden eliminar ventas en estado Borrador");
        }
        repository.deleteById(id);
        return true;
    }
}
