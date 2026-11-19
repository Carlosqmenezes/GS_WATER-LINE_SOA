package br.com.waterline.alertas.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.waterline.alertas.dominio.Envio;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

	@Query("select e from Envio e left join fetch e.inscricao where e.alerta.id = :alertaId order by e.id")
	List<Envio> listarDoAlerta(@Param("alertaId") Long alertaId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update Envio e set e.inscricao = null where e.inscricao.id = :inscricaoId")
	int desvincularInscricao(@Param("inscricaoId") Long inscricaoId);
}
