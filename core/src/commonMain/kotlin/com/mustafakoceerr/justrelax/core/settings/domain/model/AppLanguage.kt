package com.mustafakoceerr.justrelax.core.settings.domain.model


/**
 * Uygulamada desteklenen dilleri temsil eder.
 * @param code ISO 639-1 standardında dil kodu (örn: "en", "tr").
 * @param nativeName Dilin kendi dilindeki adı (örn: "English", "Türkçe").
 */

enum class AppLanguage(val code: String, val nativeName: String){
    ENGLISH(code = "en", nativeName = "English"),
    TURKISH(code="tr", nativeName = "Türkçe");

    companion object{
        /**
         * Verilen dil koduna karşılık gelen enum'u bulur.
         * Buralamzsa varsayılan olarak ENGLISH döner. Bu uygulamayo,
         * çökertmekten korur
         */
        fun fromCode(code: String?): AppLanguage{
            return entries.find { it.code == code } ?:ENGLISH
        }
    }
}