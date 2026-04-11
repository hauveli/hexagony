plugins {
    id("hexagony.minecraft")
    id("org.jetbrains.kotlin.jvm")
}

architectury {
    common("fabric", "forge")
}
/*
loom {
    mixin {
        defaultRefmapName.set ("mixins.hexagony.refmap.json")
    }
}
*/
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(kotlin("reflect"))

    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury)

    modApi(libs.hexcasting.common)

    modApi(libs.clothConfig.common)

    libs.mixinExtras.common.also {
        implementation(it)
        annotationProcessor(it)
    }
}
