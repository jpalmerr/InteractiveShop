import ShoppingService._
import cats.effect.unsafe.implicits.global
import model.{Cart, ShopProduct}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ShoppingServiceSpec extends AnyWordSpec with Matchers {
  val service = new ShoppingService()
  "ShoppingSpecService" should {
    "generate a unique CartId when the store is empty" in {
      val cartStore: CartStore = Map.empty

      val result = service.generateUniqueCartId(cartStore = cartStore, length = 3).unsafeRunSync()

      result.length mustEqual 3
      cartStore.contains(result) mustBe false
    }

    "generate a unique CartId when the store already contains some ids" in {
      val existingId = "abcde"
      val cartStore = Map(existingId -> Cart())

      val result = service.generateUniqueCartId(cartStore, length = 3).unsafeRunSync()

      result.length mustEqual 3
      result must not equal existingId
      cartStore.contains(result) mustBe false
    }
    // todo: more complex testing around the unique id

    "create a cart and add it to the cartStore" in {
      val cartStore = Map.empty[String, Cart]
      val cartId = "cart123"

      val updatedCartStore = service.createCart(cartStore, cartId).unsafeRunSync()

      updatedCartStore must contain(cartId -> Cart())
      updatedCartStore.size mustEqual 1
    }

    "retrieve a cart by ID from the cartStore" in {
      val cartId = "cart123"
      val cartStore = Map(cartId -> Cart())

      val cart = service.getCart(cartStore, cartId)

      cart mustBe defined
      cart.get.items mustEqual Map.empty
    }

    "safely handle when cart not present" in {
      val cartStore = Map.empty[String, Cart]
      val cartId = "cart123"

      val cart = service.getCart(cartStore, cartId)

      cart mustBe None
    }

    "add items to the cart and update the item quantity" in {
      val cart = Cart()
      val product = ShopProduct("Apple", 2.0)
      val quantity = 2

      val updatedCart = service.addItemToCart(cart)(product, quantity).unsafeRunSync()

      updatedCart.items must contain(product -> quantity)
    }

    "update the quantity of an existing item in the cart" in {
      val product = ShopProduct("Apple", 2.0)
      val initialCart = Cart(Map(product -> 1))
      val quantity = 3

      val updatedCart = service.addItemToCart(initialCart)(product, quantity).unsafeRunSync()

      updatedCart.items must contain(product -> 4)
    }

    "add a new product to the cart" in {
      val initialCart = Cart(Map(ShopProduct("Apple", 2.0) -> 2))
      val newProduct = ShopProduct("Banana", 1.0)
      val quantity = 5

      val updatedCart = service.addItemToCart(initialCart)(newProduct, quantity).unsafeRunSync()

      updatedCart.items must contain(newProduct -> quantity)
      updatedCart.items.size mustEqual 2
    }

    "remove an item from the cart" in {
      val product = ShopProduct("Apple", 2.0)
      val cart = Cart(Map(product -> 5))

      val updatedCart = service.removeItemFromCart(cart)(product).unsafeRunSync()

      updatedCart.items mustBe empty
    }

    "remove an item from the cart whilst leaving other items in the cart" in {
      val apple = ShopProduct("Apple", 2.0)
      val banana = ShopProduct("Banana", 1.0)
      val cart = Cart(Map(apple -> 2, banana -> 3))

      val updatedCart = service.removeItemFromCart(cart)(apple).unsafeRunSync()

      updatedCart.items must contain(banana -> 3)
      updatedCart.items must not contain apple
    }

    "delete a cart from the cartStore" in {
      val cartId = "cart123"
      val cartStore = Map(cartId -> Cart())

      val updatedCartStore = service.deleteCart(cartStore, cartId).unsafeRunSync()

      updatedCartStore mustBe empty
    }

    "delete a cart from the cartStore whilst leaving other carts in the cart store" in {
      val cartId1 = "cart123"
      val cartId2 = "cart456"
      val cartStore = Map(cartId1 -> Cart(), cartId2 -> Cart())

      val updatedCartStore = service.deleteCart(cartStore, cartId1).unsafeRunSync()

      updatedCartStore must contain(cartId2 -> Cart())
      updatedCartStore must not contain cartId1
    }

    "mark a cart as checked out" in {
      val cart = Cart()

      val checkedOutCart = service.checkout(cart).unsafeRunSync()

      checkedOutCart.checkedOut mustBe true
    }

    "not affect the items in the cart when marking as checked out" in {
      val product = ShopProduct("Apple", 2.0)
      val cart = Cart(Map(product -> 2))

      val checkedOutCart = service.checkout(cart).unsafeRunSync()

      checkedOutCart.items must contain(product -> 2)
      checkedOutCart.checkedOut mustBe true
    }

    "calculate the total price for a cart with one product" in {
      val product = ShopProduct("Apple", 1.50)
      val cart = Cart(Map(product -> 3)) // 3 apples at 1.50 each

      val totalPrice = service.completeCheckout(cart).unsafeRunSync()

      totalPrice mustEqual 4.50 // 3 * 1.50
    }

    "calculate the total price for a cart with multiple products" in {
      val apple = ShopProduct("Apple", 1.50)
      val banana = ShopProduct("Banana", 2.00)
      val cart = Cart(Map(apple -> 2, banana -> 5)) // 2 apples and 5 bananas

      val totalPrice = service.completeCheckout(cart).unsafeRunSync()

      totalPrice mustEqual 13 // (2 * 1.50) + (5 * 2.00) = 3 + 10
    }

    "return 0 for an empty cart" in {
      val cart = Cart()

      val totalPrice = service.completeCheckout(cart).unsafeRunSync()

      totalPrice mustEqual 0.0
    }
  }
}
