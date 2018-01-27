name := "tagless-final-service"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= {
  val monix = "io.monix"
  val monixV = "2.3.3"

  val slick = "com.typesafe.slick"
  val slickV = "3.2.1"

  Seq(
    monix             %% "monix"          % monixV,
    monix             %% "monix-cats"     % monixV,
    slick             %% "slick"          % slickV,
    slick             %% "slick-hikaricp" % slickV,
    "org.postgresql"  % "postgresql"      % "42.1.4"
  )
}