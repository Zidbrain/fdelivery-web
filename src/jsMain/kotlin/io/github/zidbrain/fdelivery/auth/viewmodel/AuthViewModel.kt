package io.github.zidbrain.fdelivery.auth.viewmodel

import io.github.zidbrain.fdelivery.auth.model.LoginRequestDto
import io.github.zidbrain.fdelivery.auth.model.LoginResponseDto
import io.github.zidbrain.fdelivery.auth.model.RegisterRequestDto
import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlin.reflect.KClass

class AuthViewModel : ViewModel<AuthAction, AuthState, AuthEvent>() {

    override val initialState: AuthState = AuthState.Content
    override val actions: Map<KClass<out AuthAction>, UseCase<AuthAction, AuthState, AuthEvent>> = mapOf(
        AuthAction.Login::class to SimpleUseCase<AuthAction.Login> {
            emit(AuthState.Loading)
            val request = LoginRequestDto(
                it.login, it.password
            )
            val response = ApiClient.post<LoginRequestDto, LoginResponseDto>("/api/auth/login", request)
            ApiClient.credentials = response
            sendEvent(AuthEvent.Success(response))
            emit(AuthState.Content)
        }.catch {
            emit(AuthState.Error)
        },
        AuthAction.Register::class to SimpleUseCase<AuthAction.Register> {
            emit(AuthState.Loading)
            val request = RegisterRequestDto(
                login = it.login,
                password = it.password,
                name = it.name,
                lastName = it.lastName
            )
            ApiClient.post<RegisterRequestDto, Unit>("/api/auth/register", request)
            sendAction(AuthAction.Login(it.login, it.password))
        }.catch {
            emit(AuthState.Error)
        }
    )
}

sealed class AuthAction {
    data class Login(val login: String, val password: String) : AuthAction()
    data class Register(val login: String, val password: String, val name: String, val lastName: String) : AuthAction()
}

sealed class AuthState {
    data object Content : AuthState()
    data object Loading : AuthState()
    data object Error : AuthState()
}

sealed class AuthEvent {
    data class Success(val response: LoginResponseDto) : AuthEvent()
}