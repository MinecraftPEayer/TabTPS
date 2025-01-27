import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("tabtps.platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("xyz.jpenilla.run-paper")
}

dependencies {
  implementation(projects.tabtpsCommon)

  compileOnly(libs.paperApi)
  implementation(libs.paperLib)
  implementation(libs.legacyPluginBase)
  implementation(libs.bstatsBukkit)
  implementation(libs.slf4jJdk14)

  implementation(libs.cloudPaper)
  implementation(libs.commodore) {
    exclude("com.mojang")
  }
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
  }

  shadowJar {
    archiveClassifier.set(null as String?)
    minimize()
    sequenceOf(
      "org.slf4j",
      "cloud.commandframework",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "org.spongepowered.configurate",
      "com.typesafe.config",
      "me.lucko.commodore",
      "org.checkerframework",
      "org.bstats",
      "xyz.jpenilla.pluginbase"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.$pkg")
    }
  }

  val mcVer = libs.versions.minecraft.get()

  runServer {
    minecraftVersion(mcVer)
  }

  mapOf(
    8 to setOf(
      "1.8.8",
    ),
    11 to setOf(
      "1.9.4",
      "1.10.2",
      "1.11.2",
    ),
    17 to setOf(
      "1.12.2",
      "1.13.2",
      "1.14.4",
      "1.15.2",
      "1.16.5",
      "1.17.1",
      "1.18.2",
      "1.19.4",
      mcVer,
    ),
  ).forEach { (javaVersion, minecraftVersions) ->
    for (version in minecraftVersions) {
      createVersionedRun(version, javaVersion)
    }
  }
}

bukkit {
  main = "xyz.jpenilla.tabtps.spigot.TabTPSPlugin"
  name = rootProject.name
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/TabTPS"
  loadBefore = listOf("Essentials")
  softDepend = listOf("PlaceholderAPI", "ViaVersion")
  authors = listOf("jmp")
}

tabTPSPlatform {
  productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}

fun TaskContainerScope.createVersionedRun(
  version: String,
  javaVersion: Int
) = register<RunServer>("runServer${version.replace('.', '_')}") {
  group = "tabtps"
  pluginJars.from(shadowJar.flatMap { it.archiveFile })
  minecraftVersion(version)
  systemProperty("Paper.IgnoreJavaVersion", true)
  runDirectory(file("run$version"))
  javaLauncher.set(project.javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(javaVersion))
  })
}

modrinth {
  gameVersions.addAll(bukkitVersions)
}
