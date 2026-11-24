package cl.duoc.msventas.repository;

import cl.duoc.msventas.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByCodigoAutorizacionIgnoreCase(String codigoAutorizacion);

    boolean existsByCodigoAutorizacionIgnoreCaseAndIdNot(String codigoAutorizacion, Long id);
}
