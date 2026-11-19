package br.com.waterline.alertas.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.waterline.alertas.dominio.Canal;
import br.com.waterline.alertas.dominio.Coordenada.AreaDeBusca;
import br.com.waterline.alertas.dominio.Inscricao;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

	boolean existsByCanalAndContato(Canal canal, String contato);

	@Query("""
			select i from Inscricao i
			where i.localizacao.latitude between :latMin and :latMax
			  and i.localizacao.longitude between :lngMin and :lngMax
			""")
	List<Inscricao> buscarNaArea(@Param("latMin") double latMin, @Param("latMax") double latMax,
			@Param("lngMin") double lngMin, @Param("lngMax") double lngMax);

	default List<Inscricao> buscarNaArea(AreaDeBusca area) {
		return buscarNaArea(area.latMin(), area.latMax(), area.lngMin(), area.lngMax());
	}
}
