import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ahni"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "org.mongodb" % "mongo-java-driver" % "2.11.2"
  )

  //  val main = play.Project(appName, appVersion, appDependencies).settings(
  //    // Add your own project settings here
  //  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
  )
}
