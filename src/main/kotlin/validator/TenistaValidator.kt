package dev.joseluisgs.validator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.joseluisgs.error.TenistaError
import dev.joseluisgs.models.Tenista
import java.time.LocalDateTime


fun Tenista.validate(): Result<Tenista, TenistaError.ValidationError> {
    return when {
        nombre.isBlank() -> Err(TenistaError.ValidationError("El nombre no puede estar vacío"))
        pais.isBlank() -> Err(TenistaError.ValidationError("El país no puede estar vacío"))
        altura <= 0 -> Err(TenistaError.ValidationError("La altura no puede ser menor o igual a 0"))
        peso <= 0 -> Err(TenistaError.ValidationError("El peso no puede ser menor o igual a 0"))
        puntos < 0 -> Err(TenistaError.ValidationError("Los puntos no pueden ser negativos"))
        fechaNacimiento.isAfter(
            LocalDateTime.now().toLocalDate()
        ) -> Err(TenistaError.ValidationError("La fecha de nacimiento no puede ser posterior a la actual"))

        else -> Ok(this)
    }
}