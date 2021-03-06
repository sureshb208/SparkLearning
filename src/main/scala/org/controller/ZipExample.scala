package org.controller

object ZipExample {
  def main(args: Array[String]): Unit = {
    val donuts: Seq[String] = Seq("Plain Donut", "Strawberry Donut")//, "Glazed Donut"
    val donutsPrice: Seq[Double] = Seq(1.5, 6.5,7.5)
    val ZippedDonutandPrices=donutsPrice.zip(donuts)
    println(ZippedDonutandPrices)
    // only ok for equal amount of items and if they are in order both sides
    val a=1 to 100
    print(a.groupBy(_.%(3)))

  }

}
