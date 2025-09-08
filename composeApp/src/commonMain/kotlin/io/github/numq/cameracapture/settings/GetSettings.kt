package io.github.numq.cameracapture.settings

import io.github.numq.cameracapture.usecase.UseCase

class GetSettings(private val settingsRepository: SettingsRepository) : UseCase<Unit, Settings> {
    override suspend fun execute(input: Unit) = settingsRepository.getSettings()
}