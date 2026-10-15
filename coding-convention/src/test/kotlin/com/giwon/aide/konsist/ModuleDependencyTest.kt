package com.giwon.aide.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

class ModuleDependencyTest {

    @Test
    fun `feature 끼리는 서로 import 하지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.packagee?.name?.startsWith("com.giwon.aide.feature.") == true }
            .assertFalse { file ->
                val myFeature = file.packagee!!.name.split(".")[4]
                file.imports.any { import ->
                    import.name.startsWith("com.giwon.aide.feature.") &&
                            import.name.split(".")[4] != myFeature
                }
            }
    }

    @Test
    fun `core model 은 Android 를 import 하지 않는다`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.packagee?.name?.startsWith("com.giwon.aide.core.model") == true }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("android.") || import.name.startsWith("androidx.")
                }
            }
    }

    @Test
    fun `feature 는 Room 과 CalendarContract 를 직접 import 하지 않는다 `() {
        Konsist.scopeFromProject()
            .files
            .filter { it.packagee?.name?.startsWith("com.giwon.aide.feature.") == true }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("androidx.room.") ||
                            import.name.startsWith("android.provider.CalendarContract")
                }
            }
    }
}
