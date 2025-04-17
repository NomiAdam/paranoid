/*
 * Copyright 2020 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.paranoid.plugin

import com.android.build.gradle.BaseExtension
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.gradle.AppPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.plugins.JavaPlugin
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ParanoidPlugin : Plugin<Project> {
  private lateinit var extension: ParanoidExtension

  override fun apply(project: Project) {
     extension = project.extensions.create("paranoid", ParanoidExtension::class.java)

        try {
            // Add dependency
            project.addDependencies(getDefaultConfiguration())

            // Ensure Android plugin is applied
            project.plugins.withType(AppPlugin::class.java) {
                val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
                androidComponents.onVariants { variant ->
                    variant.instrumentation.transformClassesWith(
                        ParanoidVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) {
                        // Optional: pass parameters if needed
                    }
                }
            }
        } catch (exception: UnknownDomainObjectException) {
            throw GradleException("Paranoid plugin must be applied *AFTER* Android plugin", exception)
        }
  }

  private fun getDefaultConfiguration(): String {
    return JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
  }

  private fun Project.addDependencies(configurationName: String) {
    val version = Build.VERSION
    dependencies.add(configurationName, "io.michaelrocks:paranoid-core:$version")
  }
}

abstract class ParanoidVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        // Customize logic to filter which classes to instrument
        return true
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
        parameters: InstrumentationParameters.None
    ): ClassVisitor {
        return ParanoidClassVisitor(nextClassVisitor)
    }
}

class ParanoidClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {
    // Modify class/methods here
}