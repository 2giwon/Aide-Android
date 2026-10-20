package com.giwon.aide.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ScreenConventionTest {

    @Test
    fun `Screen 컴포저블은 ViewModel 을 파라미터로 받지 않는다`() {
        Konsist.scopeFromProject()
            .functions()
            .filter { it.name.endsWith("Screen") && it.hasAnnotationWithName("Composable") }
            .assertFalse { function ->
                function.parameters.any { it.type.name.endsWith("ViewModel") }
            }
    }

    @Test
    fun `ViewModel 이 있는 패키지에는 UiState가 있다`() {
        val scope: KoScope = Konsist.scopeFromProject()
        val uiStates: Set<String?> =
            (scope.classes() + scope.interfaces()).map { it.fullyQualifiedName }.toSet()

        scope.classes()
            .filter { it.name.endsWith("ViewModel") }
            .assertTrue { viewModel ->
                val expected =
                    viewModel.packagee?.name + "." + viewModel.name.removeSuffix("ViewModel") + "UiState"

                expected in uiStates
            }
    }
}
