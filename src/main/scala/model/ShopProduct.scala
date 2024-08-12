package model

// create some example products to work with
final case class ShopProduct(name: String, price: Double)

object ShopProducts {
  val A = ShopProduct("A", 5.0)
  val B = ShopProduct("B", 10.0)
  val C = ShopProduct("C", 15.0)
  val D = ShopProduct("D", 20.0)

  val availableProducts: List[ShopProduct] = List(A, B, C, D)
}
