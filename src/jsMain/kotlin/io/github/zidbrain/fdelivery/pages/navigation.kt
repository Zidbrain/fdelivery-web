package io.github.zidbrain.fdelivery.pages

import com.varabyte.kobweb.core.PageContext
import io.github.zidbrain.fdelivery.client.ApiClient

fun paymentNavigation(ctx: PageContext) {
    if (ApiClient.credentials == null) ctx.router.navigateTo("auth?after=payment")
    else ctx.router.navigateTo("payment")
}