package com.mustafakoceerr.justrelax.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart


/**
 * Uygulama genelinde veri akışının durumunu temsil eder.
 * UI katmanı sadece bu sınıfı dinler.
 */
sealed interface Resource<out T>{
    data class Success<T>(val data:T): Resource<T>
    data class Error(val error: AppError) : Resource<Nothing> // Exception yerine kendi AppError'ımızı kullanıyoruz
    data object Loading: Resource<Nothing>
 }

/**
 * Normal bir Flow'u Resource tipine dönüştüren extension.
 * Otomatik olarak Loading başlatır ve Hata yakalar.
 */
fun <T> Flow<T>.asResource(): Flow<Resource<T>>{
    return this
        .map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { throwable->
            // Beklenmeyen hataları Unknown olarak paketle
            emit(Resource.Error(throwable as? AppError ?: AppError.Unknown(throwable)))
        }
}