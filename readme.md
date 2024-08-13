I've added a pretty imperfect, but hopefully suitable enough to demonstrate interactive user interface.

run 
```
sbt run
```
and follow the command prompts


Example usage 

```[info] running ShoppingApp
Enter command: start
New cart created with ID: rQo
Enter command: goShopping rQo
Cart rQo contents: Map(ShopProduct(A,5.0) -> 3, ShopProduct(B,10.0) -> 3, ShopProduct(C,15.0) -> 3, ShopProduct(D,20.0) -> 3)
Enter command: updateCart
Which cart would you like to update?
rQo
Which product would you like to purchase?
C
How many C would you like?
2
Updated Cart rQo contents: Map(ShopProduct(A,5.0) -> 3, ShopProduct(B,10.0) -> 3, ShopProduct(C,15.0) -> 2, ShopProduct(D,20.0) -> 3)
Enter command: checkout rQo
Cart rQo checked out. Total Price: $135.0
Enter command: exit
Exiting the program...
[success] Total time: 151 s (02:31), completed 12 Aug 2024, 9:30:35 pm
```

Didn't get round to implementing an interface to add/remove items or delete a cart, but it could have followed a similar pattern.
These capabilities are there, and are covered in the unit tests.

To run the tests run `sbt test`