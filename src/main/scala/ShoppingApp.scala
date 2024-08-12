import AppUtils.randomizeProducts
import ShoppingService._
import cats.effect.{IO, IOApp}
import model.ShopProduct
import model.ShopProducts.availableProducts

import scala.util.{Random, Try}

object ShoppingApp extends IOApp.Simple {

  val shoppingService = new ShoppingService()

  private def updateCartFlow(cartStore: CartStore, cartId: CartId): IO[Unit] = {
    def safeToInt(s: String): IO[Int] = {
      Try(s.toInt).toEither match {
        case Left(err) => IO.raiseError(err)
        case Right(int) => IO(int)
      }
    }

    val run = for {
      _ <- IO(println("Which product would you like to purchase?"))
      productName <- IO.readLine
      productOpt = availableProducts.find(_.name == productName)
      _ <- productOpt match {
        case Some(product) =>
          for {
            _ <- IO(println(s"How many $productName would you like?"))
            quantityStr <- IO.readLine
            quantity <- safeToInt(quantityStr)
            updatedCart <- shoppingService.updateCart(cartStore(cartId))(product, quantity)
            _ <- IO(println(s"Updated Cart $cartId contents: ${updatedCart.items}"))
            _ <- interactiveProgram(cartStore.updated(cartId, updatedCart))
          } yield ()
        case None =>
          IO(println(s"Product $productName not found.")) *> interactiveProgram(cartStore)
      }
    } yield ()
    run.handleErrorWith(_ => interactiveProgram(cartStore)) // if an error, keep program alive in most recent state
  }

  // an interactive program to work an example from - not perfect but hopefully enough to demonstrate
  def interactiveProgram(cartStore: CartStore): IO[Unit] = {
    IO.print("Enter command: ").flatMap(_ => IO.readLine).flatMap {
      case "start" =>
        for {
          cartId <- shoppingService.generateUniqueCartId(cartStore)
          newStore <- shoppingService.createCart(cartStore, cartId)
          _ <- IO(println(s"New cart created with ID: $cartId"))
          _ <- interactiveProgram(newStore)
        } yield ()

      case cmd if cmd.startsWith("goShopping") =>
        val id = cmd.split(" ").last
        shoppingService.getCart(cartStore, id) match {
          case Some(cart) =>
            val randomizedItems: Map[ShopProduct, Int] = randomizeProducts(availableProducts)
            for {
              updatedCart <- randomizedItems.toList.foldLeft(IO.pure(cart)) { case (ioCart, (product, quantity)) =>
                ioCart.flatMap(c => shoppingService.addItemToCart(c)(product, quantity))
              }
              _ <- IO(println(s"Cart $id contents: ${updatedCart.items}"))
              _ <- interactiveProgram(cartStore.updated(id, updatedCart))
            } yield ()
          case None =>
            IO(println(s"Cart with ID $id not found.")) *> interactiveProgram(cartStore)
        }

      case "updateCart" =>
        for {
          _ <- IO(println("Which cart would you like to update?"))
          cartId <- IO.readLine
          _ <- if (cartStore.contains(cartId)) updateCartFlow(cartStore, cartId)
          else IO(println(s"Cart with ID $cartId not found.")) *> interactiveProgram(cartStore)
        } yield ()

      case cmd if cmd.startsWith("checkout") =>
        val id = cmd.split(" ").last
        shoppingService.getCart(cartStore, id) match {
          case Some(cart) =>
            for {
              checkedOutCart <- shoppingService.checkout(cart)
              totalPrice <- shoppingService.completeCheckout(checkedOutCart)
              _ <- IO(println(s"Cart $id checked out. Total Price: $$${totalPrice}"))
              _ <- interactiveProgram(cartStore.updated(id, checkedOutCart))
            } yield ()
          case None =>
            IO(println(s"Cart with ID $id not found.")) *> interactiveProgram(cartStore)
        }

      case "exit" =>
        IO(println("Exiting the program..."))

      case _ =>
        IO(println("Unknown command.")) *> interactiveProgram(cartStore)
    }
  }

  override def run: IO[Unit] = interactiveProgram(Map.empty)
}

object AppUtils {
  def randomizeProducts(products: List[ShopProduct], maxQuantity: Int = 3): Map[ShopProduct, Int] = {
    products.map { product =>
      val quantity = Random.nextInt(maxQuantity + 1)
      product -> quantity
    }.toMap
  }
}
