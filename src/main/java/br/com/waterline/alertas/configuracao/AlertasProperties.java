package br.com.waterline.alertas.configuracao;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties("alertas")
public record AlertasProperties(
		@DefaultValue("1000") @Min(50) @Max(5000) int raioConsultaMetros,
		@DefaultValue("2h") @NotNull Duration validade) {
}
