package sw

// Use H2Driver to connect to an H2 database

import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple.{Session, Database}
import scala.slick.direct._
import scala.slick.direct.AnnotationMapper._
import scala.reflect.runtime.universe
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.WrappingQuery
import scala.slick.ast.Drop


/**
 * A simple example that uses statically typed queries against an in-memory
 * H2 database. The example data comes from Oracle's JDBC tutorial at
 * http://download.oracle.com/javase/tutorial/jdbc/basics/tables.html.
 */
object SlickJSON extends App {

  // Definition of the SUPPLIERS table
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey)

    // This is the primary key column
    def name = column[String]("SUP_NAME")

    def street = column[String]("STREET")

    def city = column[String]("CITY")

    def state = column[String]("STATE")

    def zip = column[String]("ZIP")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, street, city, state, zip)
  }

  val suppliers = TableQuery[Suppliers]

  // Definition of the COFFEES table
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)

    def supID = column[Int]("SUP_ID")

    def price = column[Double]("PRICE")

    def sales = column[Int]("SALES")

    def total = column[Int]("TOTAL")

    def * = (name, supID, price, sales, total)

    // A reified foreign key relation that can be navigated to create a join
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }

  val coffees = TableQuery[Coffees]


  // Connect to the database and execute the following block within a session
  Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession {
    implicit session =>


    // Create the tables, including primary and foreign keys
      (suppliers.ddl ++ coffees.ddl).create

      // Insert some suppliers
      suppliers +=(101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199")
      suppliers +=(49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460")
      suppliers +=(150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")

      // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
      coffees ++= Seq(
        ("Colombian", 101, 7.99, 0, 0),
        ("French_Roast", 49, 8.99, 0, 0),
        ("Espresso", 150, 9.99, 0, 0),
        ("Colombian_Decaf", 101, 8.99, 0, 0),
        ("French_Roast_Decaf", 49, 9.99, 0, 0)
      )

      // Iterate through all coffees and output them
      println("Coffees:")
      coffees foreach {
        case (name, supID, price, sales, total) =>
          println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
      }

      // Do the same thing using the navigable foreign key
      println("Join by foreign key:")

      val q1 = for {
        c <- coffees
        s <- c.supplier if s.name === "Acme, Inc."
      } yield (c.name, s.name)

      val q2 = coffees.withFilter(c => c.price < 9.0).flatMap(c => c.supplier.map(s => (c.name, s.name)))

      val q3 = for {
        (c, s) <- coffees innerJoin suppliers on (_.supID === _.id)
        if c.price < 9.0
      } yield (c.name, s.name)

      implicit class QueryExtensions[T, E](val q: Query[T, E]) {
        def jsonCriteria(criteria: String): Query[T, E] =  q
      }

      val q4 = for {
        c <- TableQuery[Coffees].jsonCriteria("") if c.price < 9.0
        s <- c.supplier
      } yield (c.name, s.name)


      val q5 = coffees.jsonCriteria("").withFilter(c => c.price < 9.0).flatMap(c => c.supplier.map(s => (c.name, s.name)))

      val q = q4
      // This time we read the result set into a List
      val l3: List[(String, String)] = q.list
      for ((s1, s2) <- l3) println("  " + s1 + " supplied by " + s2)

      // Check the SELECT statement for that query
      println("Query")
      println(q.selectStatement)
      println

  }
}
