# Firebase Firestore Setup

This project uses Firebase Authentication and Firebase Firestore for the backend foundation.

## Firebase Console Steps

1. Open Firebase Console.
2. Select the ChezVous Firebase project.
3. Enable Authentication:
   - Email/password now.
   - Google later.
   - Facebook later.
4. Enable Firestore Database.
5. Start in production mode.
6. For first-time seeding only, copy the rules from `firebase/firestore.dev-seed.rules` into Firestore Rules and publish them.
7. Run the app, register/login, and open the Home screen.
8. The app will call `RestaurantRepository.seedDemoDataIfEmpty()` and create starter documents.
9. After the collections appear, replace the rules with `firebase/firestore.rules` and publish them.

## Collections

```text
users/{userId}
restaurants/{restaurantId}
menuItems/{menuItemId}
orders/{orderId}
drivers/{driverId}
```

For the detailed database schema, see `FIRESTORE_SCHEMA.md`.

Firestore creates collections automatically after documents are inserted. You do not need to manually create empty collections.

The app now calls a seed helper when Home opens:

```text
RestaurantRepository.seedDemoDataIfEmpty()
```

If `restaurants` is empty, it creates starter documents for `restaurants`, `menuItems` and `drivers`.

Important: `firebase/firestore.dev-seed.rules` is only for setup/demo seeding. Do not keep it as the final rules file, because partner/admin protection is looser there.

## Required User Roles

```text
CUSTOMER
PARTNER
ADMIN
```

Customers use the normal food-ordering flow. Partner/admin users manage restaurants, menu items and order status.

## Demo Restaurants

Create documents in `restaurants`:

```text
restaurants/burger-house
name: Burger House
cuisineType: Fast Food
rating: 4.6
deliveryTime: 25-35 min
minimumOrder: 30.0
imageUrl: ""
isOpen: true
```

```text
restaurants/casa-pizza
name: Casa Pizza
cuisineType: Pizza
rating: 4.4
deliveryTime: 30-40 min
minimumOrder: 40.0
imageUrl: ""
isOpen: true
```

```text
restaurants/healthy-bowl
name: Healthy Bowl
cuisineType: Healthy
rating: 4.8
deliveryTime: 20-30 min
minimumOrder: 35.0
imageUrl: ""
isOpen: true
```

## Demo Menu Items

Create documents in `menuItems`:

```text
menuItems/classic-burger
restaurantId: burger-house
name: Classic Burger
description: Burger boeuf, fromage, salade et sauce maison.
price: 45.0
category: Burgers
imageUrl: ""
isAvailable: true
```

```text
menuItems/chicken-burger
restaurantId: burger-house
name: Chicken Burger
description: Poulet croustillant, cheddar et sauce ChezVous.
price: 42.0
category: Burgers
imageUrl: ""
isAvailable: true
```

```text
menuItems/margherita
restaurantId: casa-pizza
name: Pizza Margherita
description: Tomate, mozzarella et basilic.
price: 55.0
category: Pizzas
imageUrl: ""
isAvailable: true
```

```text
menuItems/pepperoni
restaurantId: casa-pizza
name: Pizza Pepperoni
description: Mozzarella, pepperoni et sauce tomate.
price: 68.0
category: Pizzas
imageUrl: ""
isAvailable: true
```

```text
menuItems/salmon-bowl
restaurantId: healthy-bowl
name: Salmon Bowl
description: Riz, saumon, avocat, legumes et sauce soja.
price: 72.0
category: Bowls
imageUrl: ""
isAvailable: true
```

```text
menuItems/veggie-bowl
restaurantId: healthy-bowl
name: Veggie Bowl
description: Quinoa, pois chiches, avocat et legumes frais.
price: 58.0
category: Bowls
imageUrl: ""
isAvailable: true
```

## Demo Driver

Create a document in `drivers`:

```text
drivers/driver-yassine
fullName: Yassine El Amrani
phone: +212600000001
rating: 4.7
vehicleType: Moto
isAvailable: true
```

## Order Status Values

```text
PENDING
CONFIRMED
PREPARING
ON_THE_WAY
DELIVERED
CANCELLED
```

These values will be used by customer tracking and the partner dashboard.
