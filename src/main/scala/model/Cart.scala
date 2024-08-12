package model

// todo: model price field so that -ve number can't be set as price
case class Cart(items: Map[ShopProduct, Int] = Map.empty, checkedOut: Boolean = false)
