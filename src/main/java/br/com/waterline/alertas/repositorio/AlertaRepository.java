package br.com.waterline.alertas.repositorio;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.waterline.alertas.dominio.Alerta;
import br.com.waterline.alertas.dominio.Coordenada.AreaDeBusca;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

	Optional<Alerta> findByAlertaOrigemId(int alertaOrigemId);

	@Query("""
			select a from Alerta a
			where a.expiraEm > :agora
			  and a.localizacao.latitude between :latMin and :latMax
			  and a.localizacao.longitude between :lngMin and :lngMax
			""")
	List<Alerta> buscarAtivosNaArea(@Param("agora") Instant agora, @Param("latMin") double latMin,
			@Param("latMax") double latMax, @Param("lngMin") double lngMin, @Param("lngMax") double lngMax);

	default List<Alerta> buscarAtivosNaArea(Instant agora, AreaDeBusca area) {
		return buscarAtivosNaArea(agora, area.latMin(), area.latMax(), area.lngMin(), area.lngMax());
	}
}
