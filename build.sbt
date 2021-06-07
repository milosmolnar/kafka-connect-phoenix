import Dependencies._
import sbtassembly.MergeStrategy

lazy val root = (project in file(".")).
  settings(
    organization := "com.triviadata",
    scalaVersion := "2.12.3",
    version := "1.10.0-SNAPSHOT",
    name := "kafka-connect-phoenix",
    libraryDependencies ++= scalaTest ++ kafkaConnect ++ logger,
    publishMavenStyle := true,
    publishTo := {
      val artifactory = "https://maven.ideata-tech.eu/artifactory"
      if (isSnapshot.value)
        Some("Artifactory realm" at artifactory +  "/libs-snapshot-local")
      else
        Some("Artifactory realm" at artifactory + "/libs-release-local")

    },
    credentials += {
      if (sys.env.get("ARTIFACTORY_USERNAME").isDefined) {
        Credentials(
          "Artifactory Realm",
          "maven.ideata-tech.eu",
          sys.env("ARTIFACTORY_USERNAME"),
          sys.env("ARTIFACTORY_PASSWORD"))
      } else {
        Credentials(Path.userHome / ".sbt" / ".ideata-credentials")
      }
    },

    resolvers ++= Seq(
      "confluent" at "http://packages.confluent.io/maven/",
      "triviadata-snapshots" at "https://maven.ideata-tech.eu/artifactory/libs-snapshot/",
      "triviadata-releases" at "https://maven.ideata-tech.eu/artifactory/libs-release/",
      Resolver.sonatypeRepo("public"),
      Resolver.mavenLocal
    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
