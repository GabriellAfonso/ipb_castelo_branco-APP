package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.defaultSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCollectionSettingsUseCaseTest {

    private val useCase = ValidateCollectionSettingsUseCase()

    private fun validForm(): MutableMap<SettingField, String> {
        val settings = defaultSettings()
        return SettingField.entries.associateWith { settings.valueOf(it).toString() }.toMutableMap()
    }

    @Test
    fun `a fully valid form parses into settings`() {
        val validation = useCase(validForm())

        assertTrue(validation.isValid)
        assertEquals(defaultSettings(), validation.values)
    }

    @Test
    fun `each field accepts its minimum and its maximum`() {
        SettingField.entries.forEach { field ->
            listOf(field.min, field.max).forEach { value ->
                val form = validForm().apply { this[field] = value.toString() }

                val validation = useCase(form)

                assertTrue("$field should accept $value", validation.isValid)
            }
        }
    }

    @Test
    fun `each field refuses one below its minimum and one above its maximum`() {
        SettingField.entries.forEach { field ->
            listOf(field.min - 1, field.max + 1).forEach { value ->
                val form = validForm().apply { this[field] = value.toString() }

                val validation = useCase(form)

                assertFalse("$field should refuse $value", validation.isValid)
                assertEquals(setOf(field), validation.errors.keys)
                assertEquals(
                    "Informe um número entre ${field.min} e ${field.max}.",
                    validation.errors[field],
                )
            }
        }
    }

    @Test
    fun `a non-integer value is refused on its own field`() {
        val form = validForm().apply { this[SettingField.MAX_BATCH_SIZE] = "muitos" }

        val validation = useCase(form)

        assertEquals("Informe um número inteiro.", validation.errors[SettingField.MAX_BATCH_SIZE])
        assertNull(validation.values)
    }

    @Test
    fun `an empty value is refused on its own field`() {
        val form = validForm().apply { this[SettingField.MAX_PAST_DAYS] = "   " }

        val validation = useCase(form)

        assertEquals("Informe um valor.", validation.errors[SettingField.MAX_PAST_DAYS])
    }

    @Test
    fun `several bad fields are all reported`() {
        val form = validForm().apply {
            this[SettingField.MIN_SECONDS_TO_COUNT] = "0"
            this[SettingField.MAX_BATCH_SIZE] = "5000"
        }

        val validation = useCase(form)

        assertEquals(
            setOf(SettingField.MIN_SECONDS_TO_COUNT, SettingField.MAX_BATCH_SIZE),
            validation.errors.keys,
        )
    }

    @Test
    fun `surrounding whitespace does not make a valid number invalid`() {
        val form = validForm().apply { this[SettingField.MIN_SECONDS_TO_COUNT] = " 45 " }

        val validation = useCase(form)

        assertTrue(validation.isValid)
        assertEquals(45, validation.values?.minSecondsToCount)
    }

    @Test
    fun `the wire name of every field matches the contract`() {
        assertEquals(
            listOf(
                "min_seconds_to_count",
                "collapse_window_minutes",
                "max_batch_size",
                "max_past_days",
                "future_tolerance_minutes",
                "window_grace_minutes",
            ),
            SettingField.entries.map { it.wireName },
        )
    }
}
