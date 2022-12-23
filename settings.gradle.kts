/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/7.5.1/userguide/multi_project_builds.html
 */

rootProject.name = "waambokt"
include("app")
include("service-spec:service-spec-net")
findProject(":service-spec:service-spec-net")?.name = "service-spec-net"
include("common")
include("service-waambokt")
include("service-group")
include("service-group:service-net")
findProject("service-group:service-net")?.name = "service-net"
include("service-group:service-odds")
findProject(":service-group:service-odds")?.name = "service-odds"
include("service-spec:service-spec-odds")
findProject(":service-spec:service-spec-odds")?.name = "service-spec-odds"
