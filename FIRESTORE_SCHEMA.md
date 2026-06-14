# Firestore Schema

This schema follows `PROJECT_PLAN.md` and the Groupe 14 cahier des charges: food ordering, payment simulation, real-time delivery tracking and partner restaurant management.

Firestore creates collections automatically when the first document is written. For this project, the seed helper creates the starter `restaurants`, `menuItems` and `drivers` collections.

## Collections Overview

```text
users/{userId}
restaurants/{restaurantId}
menuItems/{menuItemId}
orders/{orderId}
drivers/{driverId}
```

## users

Created when the app stores profile data after authentication.

Path:

```text
users/{firebaseAuthUid}
```

Fields:

```text
id: String
fullName: String
email: String
phone: String
address: String
role: String
```

Allowed roles:

```text
CUSTOMER
PARTNER
ADMIN
```

Purpose:

- Customer profile.
- Delivery address.
- Partner/admin permissions.

## restaurants

Seeded by the app when empty.

Path example:

```text
restaurants/burger-house
```

Fields:

```text
id: String
name: String
cuisineType: String
rating: Number
deliveryTime: String
minimumOrder: Number
imageUrl: String
isOpen: Boolean
```

Purpose:

- Home screen restaurant list.
- Restaurant search and filters.
- Restaurant details screen.
- Partner restaurant management.

## menuItems

Seeded by the app when empty.

Path example:

```text
menuItems/classic-burger
```

Fields:

```text
id: String
restaurantId: String
name: String
description: String
price: Number
category: String
imageUrl: String
isAvailable: Boolean
```

Purpose:

- Restaurant menu.
- Food item cards.
- Cart items.
- Partner menu management.

## orders

Created later by the checkout flow.

Path example:

```text
orders/{generatedOrderId}
```

Fields:

```text
id: String
userId: String
restaurantId: String
restaurantName: String
items: List<Map>
subtotal: Number
deliveryFee: Number
totalPrice: Number
deliveryAddress: String
paymentMethod: String
paymentStatus: String
status: String
driverId: String
estimatedDeliveryTime: String
createdAt: Number
```

Order status values:

```text
PENDING
CONFIRMED
PREPARING
ON_THE_WAY
DELIVERED
CANCELLED
```

Payment status values:

```text
PENDING
PAID
FAILED
CASH_ON_DELIVERY
```

Purpose:

- Order history.
- Order details.
- Real-time delivery tracking.
- Partner dashboard order updates.

## drivers

Seeded by the app when empty.

Path example:

```text
drivers/driver-yassine
```

Fields:

```text
id: String
fullName: String
phone: String
rating: Number
vehicleType: String
isAvailable: Boolean
```

Purpose:

- Tracking screen driver card.
- Estimated delivery workflow.

## Seeding Behavior

When the Home screen opens, `HomeViewModel` calls:

```text
RestaurantRepository.seedDemoDataIfEmpty()
```

That function checks if `restaurants` has at least one document. If it is empty, it writes:

- 3 restaurants.
- 6 menu items.
- 1 driver.

It uses merge writes, so running it again does not duplicate documents.

Important: if Firestore rules are already strict, the signed-in user must have permission to write `restaurants`, `menuItems` and `drivers`. During initial setup, either use temporary development rules or seed manually from the Firebase Console.

For the first setup, use `firebase/firestore.dev-seed.rules`, run the app once after login, confirm the collections appear, then switch back to `firebase/firestore.rules`.
