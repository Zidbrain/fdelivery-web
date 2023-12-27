package io.github.zidbrain.fdelivery.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.rememberPageContext
import kotlin.reflect.KClass

object ViewModelHolder {
    val factories: MutableMap<KClass<out ViewModel<*, *, *>>, (PageContext) -> ViewModel<*, *, *>> = mutableMapOf()
    val viewModels: MutableList<ViewModel<*, *, *>> = mutableListOf()

    inline fun <reified T : ViewModel<*, *, *>> register(noinline factory: (PageContext) -> T) {
        factories[T::class] = factory
    }

    @Composable
    inline fun <reified T : ViewModel<*, *, *>> viewModel(): T {
        val ctx = rememberPageContext()
        val viewModel = remember(ctx) {
            (viewModels.find { it::class == T::class } ?: factories[T::class]!!(ctx).also {
                viewModels.add(it)
            }) as T
        }
        DisposableEffect(viewModel) {
            viewModel.onActionsBound()
            onDispose {
                viewModel.close()
                viewModels.removeAll { it::class == T::class }
            }
        }
        return viewModel
    }

    inline fun <reified T : ViewModel<*, *, *>> remove() {
        viewModels.removeAll { it::class == T::class }
    }

    fun clear() {
        viewModels.forEach {
            it.close()
        }
        viewModels.clear()
    }
}

inline fun <reified T : ViewModel<*, *, *>> register(noinline factory: (PageContext) -> T) {
    ViewModelHolder.register(factory)
}

@Composable
inline fun <reified T : ViewModel<*, *, *>> viewModel(): T =
    ViewModelHolder.viewModel()