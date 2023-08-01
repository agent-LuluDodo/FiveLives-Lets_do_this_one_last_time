package de.luludodo.fivelives.api;

import de.luludodo.fivelives.config.translations.Translation;

public enum ApiResult {
    SUCCESS("api.success"),
    MORE_THAN_MAX_LIVES("api.moreThanMaxLives"),
    LESS_THAN_0_LIVES("api.lessThan0Lives"),
    PLAYER_NOT_ALLOWED("api.playerNotAllowed");

    final String translation;
    ApiResult(String translation) {
        this.translation = translation;
    }

    public String getMessage() {
        return Translation.translationAsString(translation);
    }
}
