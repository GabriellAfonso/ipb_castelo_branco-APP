package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ValidateServiceWindowUseCaseTest {

    private val useCase = ValidateServiceWindowUseCase()

    private val valid = ServiceWindowDraft(
        name = "Culto de Oração",
        startTime = LocalTime.of(19, 30),
        endTime = LocalTime.of(21, 0),
    )

    @Test
    fun `a valid draft has no errors`() {
        assertTrue(useCase(valid).isEmpty())
    }

    @Test
    fun `an end before the start is refused on the end field`() {
        val errors = useCase(valid.copy(endTime = LocalTime.of(19, 0)))

        assertEquals(
            "O horário de término precisa ser depois do de início.",
            errors[ServiceWindowFields.END_TIME],
        )
    }

    @Test
    fun `an end equal to the start is refused too`() {
        val errors = useCase(valid.copy(endTime = valid.startTime))

        assertTrue(errors.containsKey(ServiceWindowFields.END_TIME))
    }

    @Test
    fun `an empty name is refused on the name field`() {
        val errors = useCase(valid.copy(name = "   "))

        assertEquals("Informe o nome do culto.", errors[ServiceWindowFields.NAME])
    }

    @Test
    fun `a name longer than the accepted length is refused`() {
        val errors = useCase(valid.copy(name = "a".repeat(101)))

        assertEquals(
            "O nome pode ter no máximo 100 caracteres.",
            errors[ServiceWindowFields.NAME],
        )
    }

    @Test
    fun `a name of exactly the accepted length is allowed`() {
        assertTrue(useCase(valid.copy(name = "a".repeat(100))).isEmpty())
    }

    @Test
    fun `both a bad name and a bad time are reported together`() {
        val errors = useCase(valid.copy(name = "", endTime = LocalTime.of(1, 0)))

        assertEquals(
            setOf(ServiceWindowFields.NAME, ServiceWindowFields.END_TIME),
            errors.keys,
        )
    }
}
