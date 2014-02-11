name := "slick-json"

version := "1.0"

scalaVersion := "2.10.3"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.0.0-RC1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.166",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "com.typesafe.play" % "play-json_2.10" % "2.2.1"
  /*
  "org.apache.derby" % "derby" % "10.9.1.0",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "net.sourceforge.jtds" % "jtds" % "1.2.4" % "test"
  */
)

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
