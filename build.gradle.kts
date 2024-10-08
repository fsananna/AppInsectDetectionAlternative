buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath ("com.apollographql.apollo:apollo-gradle-plugin:2.5.9")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
//chip navigation
