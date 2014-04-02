name := "clide-xml"

version := "0.1-SNAPSHOT"

organization := "net.flatmap"

organizationName := "flatmap"

organizationHomepage := Some(url("http://www.flatmap.net"))

startYear := Some(2014)

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("http://github.com/martinring/clide.xml"))

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature")

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

publishArtifact in Test := false

pomExtra := {
  <developers>
    <developer>
      <id>martinring</id>
      <name>Martin Ring</name>
      <email>martin.ring@dfki.de</email>
      <url>http://gihub.com/martinring</url>
      <organization>DFKI</organization>
      <organizationUrl>http://www.dfki.de/cps</organizationUrl>
      <roles>
        <role>Junior Researcher</role>
      </roles>
      <timezone>+1</timezone>
      <properties>
        <picUrl>http://www.gravatar.com/avatar/6b421a8330a3c9a2b901796e11c5a27e.png</picUrl>
      </properties>
    </developer>
  </developers>
  <scm>
    <url>https://github.com/martinring/clide.xml</url>
    <connection>scm:git:git@github.com:martinring/clide.xml.git</connection>
  </scm>
}

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "org.scalamacros" % "quasiquotes" % "2.0.0-M3" cross CrossVersion.full

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M3" cross CrossVersion.full)
