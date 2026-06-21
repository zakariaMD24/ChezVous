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
managedRestaurantIds: List<String>
driverId: String
```

Allowed roles:

```text
CUSTOMER
PARTNER
RESTAURANT_ADMIN
CHEF
DRIVER
ADMIN
```

`DRIVER` is an active login role. A driver user must also have `driverId` set to the matching document in the `drivers` collection.
`CHEF` is an active kitchen role. A chef user uses `managedRestaurantIds` to access only assigned kitchen queues.

Purpose:

- Customer profile.
- Delivery address.
- Partner/admin permissions.
- Restaurant-specific management access through `managedRestaurantIds`.
- Kitchen access through `managedRestaurantIds` for `CHEF`.
- Driver dashboard access through `driverId`.

Security note:

- A user can create only their own `CUSTOMER` profile.
- A user can update only normal profile fields: `fullName`, `phone` and `address`.
- Only global `ADMIN` accounts should change `role`, `managedRestaurantIds` or `driverId`.

Example restaurant admin user:

```text
role: "RESTAURANT_ADMIN"
managedRestaurantIds: ["burger-house"]
```

This user can manage only Burger House orders and menu items. A global `ADMIN` can manage every restaurant.

Example driver user:

```text
role: "DRIVER"
managedRestaurantIds: []
driverId: "driver-yassine"
```

This user can use the delivery dashboard for assigned orders linked to `driver-yassine`.

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
pickupCode: String
pickupCodeValidation: String
pickupCodeValidatedAt: Number
estimatedDeliveryTime: String
createdAt: Number
```

Order status values:

```text
PENDING
CONFIRMED
PREPARING
READY_FOR_PICKUP
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
- Driver dashboard delivery updates.
- Kitchen dashboard preparation and QR/code pickup validation.

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
- Admin-managed delivery driver list.
- Linked driver account availability.

Write access:

- Global `ADMIN` can create, update and delete driver records.
- A linked `DRIVER` account can update only its own `isAvailable` flag.
- Signed-in users can read driver records for tracking and operational screens.

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
