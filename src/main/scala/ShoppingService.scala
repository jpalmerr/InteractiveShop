import ShoppingService._
import cats.effect.IO
import model.{Cart, ShopProduct}

import scala.annotation.tailrec
import scala.util.Random

object ShoppingService {
  type CartId = String
  type CartStore = Map[CartId, Cart]
}


class ShoppingService(randomIdGenerator: Int => String = length => Random.alphanumeric.take(length).mkString) {

  // todo: could ensure cartStore not full
  // Generate unique cart id
  def generateUniqueCartId(cartStore: CartStore = Map.empty, length: Int = 3): IO[CartId] = {
    def generateRandomId: CartId = randomIdGenerator(length)

    @tailrec
    def ensureUnique(id: CartId): CartId = {
      if (cartStore.contains(id)) ensureUnique(generateRandomId)
      else id
    }

    IO(ensureUnique(generateRandomId))
  }

  // create a new cart
  def createCart(cartStore: CartStore = Map.empty, id: CartId): IO[CartStore] =
    IO(cartStore + (id -> Cart()))

  // retrieve a cart by ID
  def getCart(cartStore: CartStore, id: CartId): Option[Cart] =
    cartStore.get(id)

  // add items to cart
  def addItemToCart(cart: Cart)(product: ShopProduct, quantity: Int): IO[Cart] = {
    IO(cart.copy(items = cart.items + (product -> (cart.items.getOrElse(product, 0) + quantity))))
  }

  // update quantity of item in cart
  def updateCart(cart: Cart)(product: ShopProduct, quantity: Int): IO[Cart] = {
    if (quantity > 0)
      IO(cart.copy(items = cart.items + (product -> quantity))) // update quantity
    else
      IO(cart.copy(items = cart.items - product)) // remove
  }

  // remove item from cart
  def removeItemFromCart(cart: Cart)(product: ShopProduct): IO[Cart] =
    IO(cart.copy(items = cart.items - product))

  // delete a cart
  def deleteCart(cartStore: CartStore, cartId: CartId): IO[CartStore] =
    IO(cartStore - cartId)

  // mark a cart as ready for checkout
  def checkout(cart: Cart): IO[Cart] =
    IO(cart.copy(checkedOut = true))

  // complete a checkout
  def completeCheckout(cart: Cart): IO[Double] = {
    IO(cart.items.map { case (product, quantity) => product.price * quantity }.sum)
  }
}
