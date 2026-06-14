# ChezVous Project Plan

Source of truth:

- `Projet de Fin de Module.pdf`
- `CAHIER_DES_CHARGES_CHECKLIST.md`

## Project Scope

ChezVous is the Groupe 14 Android project: a food ordering and real-time delivery application.

The app must support:

- Restaurant browsing.
- Menu browsing.
- Cart management.
- Checkout and online payment simulation.
- Order creation.
- Real-time order tracking.
- Partner restaurant management.
- User authentication and profile management.

## Working Rule

Before adding or changing a feature, verify that it supports the cahier des charges:

- Android Kotlin and Jetpack Compose.
- MVVM or MVI architecture.
- Unified design system.
- Responsive UI for phone, tablet, portrait and landscape.
- Internationalized UI.
- Authentication with email/password, Google and Facebook.
- Food ordering, delivery tracking, online payment and partner restaurant management.
- Project compiles without errors.

## Phase 1: Stabilize The Current App

Goal: make the existing base reliable before adding large features.

Tasks:

- Verify the project compiles.
- Fix broken text encoding in French UI strings.
- Remove unused imports and empty placeholder code when needed.
- Add proper app navigation instead of manual screen switching.
- Keep folders clean:
  - `data/model`
  - `data/repository`
  - `data/local`
  - `data/remote`
  - `presentation`
  - `ui/components`
  - `ui/theme`

Done when:

- The app opens.
- Login/register/home navigation works.
- No obvious broken UI text remains.
- The app builds successfully.

## Phase 2: Firebase Firestore Foundation

Goal: prepare a real lightweight backend using Firebase Firestore.

Tasks:

- Add Firestore dependency.
- Enable Firestore in Firebase Console.
- Keep Firebase Authentication.
- Create repository interfaces where useful.
- Add Firestore repositories for restaurants, menu items, users and orders.
- Keep fake data fallback for safe demo mode.
- Add seed/demo data structure.
- Add Firestore security rules.

Recommended collections:

```text
users/{userId}
restaurants/{restaurantId}
menuItems/{menuItemId}
orders/{orderId}
drivers/{driverId}
```

Recommended order statuses:

```text
PENDING
CONFIRMED
PREPARING
ON_THE_WAY
DELIVERED
CANCELLED
```

Done when:

- Restaurants can be loaded from Firestore or fallback data.
- Orders can be created in Firestore.
- Tracking screen can listen to order status changes.

## Phase 3: Authentication And User Profile

Goal: complete user management required by the cahier des charges.

Tasks:

- Complete email/password login.
- Complete email/password registration.
- Store user profile in Firestore:
  - full name;
  - email;
  - phone;
  - address;
  - role.
- Add logout.
- Add profile screen.
- Add Google login.
- Add Facebook login.

Done when:

- A user can register, log in, log out and view/edit profile info.
- User profile data is saved in Firestore.
- Google and Facebook login are available or clearly documented if demo-limited.

Current implementation note:

- Email/password auth, Firestore profile storage, profile editing and logout are implemented.
- Google sign-in app code is implemented with Android Credential Manager and Firebase Auth.
- Google sign-in still requires Firebase Console provider setup, SHA fingerprints and an updated `google-services.json`.
- Facebook setup is tracked in `AUTH_SETUP.md` because it requires Firebase/Facebook provider configuration.

## Phase 4: Customer Restaurant Flow

Goal: build the normal customer path before ordering.

Tasks:

- Improve home screen.
- Load restaurants from repository.
- Add restaurant details screen.
- Add restaurant menu screen.
- Add food categories.
- Add reusable `FoodItemCard`.
- Show image, description, price and availability.
- Add search by restaurant, food and category.
- Add filters:
  - cuisine type;
  - rating;
  - delivery time;
  - minimum order.

Done when:

- A customer can browse restaurants.
- A customer can open a restaurant and see its menu.
- Search and filters work.

Current implementation note:

- Home loads restaurants from the repository.
- Home search checks restaurant fields and linked menu item fields.
- Home has structured Filter and Sort controls.
- Home filter sheet uses dynamic chips, sliders and toggles for cuisine type, rating, delivery time, minimum order and open restaurants.
- Home shows active removable filter chips and a quick cuisine/category row.
- Home sort sheet supports recommended, top rated, fastest delivery, lowest minimum order and A-Z.
- Restaurant details/menu screen is implemented.
- Menu screen has structured Filter and Sort controls.
- Menu filter sheet uses dynamic chips and a price slider for category, availability and price.
- Menu sort sheet supports recommended, price, A-Z and available-first ordering.
- Reusable `FoodItemCard` is implemented with image placeholder, description, price, category and availability.

## Phase 5: Cart

Goal: allow the user to prepare an order.

Tasks:

- Add cart repository or cart state holder.
- Add "add to cart".
- Increase item quantity.
- Decrease item quantity.
- Remove item.
- Show subtotal.
- Show delivery fee.
- Show total.
- Validate restaurant minimum order.
- Create reusable cart components where repeated.

Done when:

- A customer can add menu items to cart.
- Cart totals are correct.
- Checkout is blocked when minimum order is not met.

Current implementation note:

- A shared cart state holder is implemented in `CartRepository`.
- Restaurant menu cards support adding available items to the cart.
- The cart is limited to one restaurant at a time.
- Home and restaurant details show a cart shortcut with item count.
- Cart screen shows items, quantity controls, remove, clear cart, subtotal, delivery fee and total.
- Checkout button is disabled until the restaurant minimum order is reached.

## Phase 6: Checkout And Payment

Goal: complete order confirmation with online payment simulation.

Tasks:

- Add delivery address form.
- Add payment method screen.
- Add reusable `PaymentMethodCard`.
- Add order summary.
- Add fake online card payment flow.
- Add cash on delivery option if useful.
- Confirm order after successful payment.
- Create order document in Firestore.

Done when:

- A customer can complete checkout.
- Payment success is simulated clearly.
- An order is created and saved.

## Phase 7: Orders And Real-Time Tracking

Goal: satisfy the real-time delivery tracking requirement.

Tasks:

- Add order history screen.
- Add order details screen.
- Add tracking screen.
- Add reusable `OrderStatusStepper`.
- Add reusable `DriverCard`.
- Show current status.
- Show estimated delivery time.
- Listen to Firestore order updates in real time.
- Allow cancel order before preparation.

Done when:

- A customer can see order history.
- A customer can open tracking.
- Status updates appear live when the order changes in Firestore.

## Phase 8: Partner Restaurant Management

Goal: satisfy partner restaurant management requirement.

Tasks:

- Add user roles:
  - customer;
  - partner/admin.
- Add partner dashboard.
- View incoming restaurant orders.
- Update order status.
- Add/manage menu items.
- Update item availability.
- Edit item price and description.

Done when:

- A partner can manage menu items.
- A partner can update order status.
- Customer tracking updates after partner changes status.

## Phase 9: Design System And Responsive UI

Goal: make the app visually coherent and modern.

Tasks:

- Define final colors.
- Define typography.
- Standardize buttons, text fields, cards, chips and top bars.
- Reuse components from `ui/components`.
- Add proper app logo/icon if needed.
- Check phone layout.
- Check tablet layout.
- Check portrait orientation.
- Check landscape orientation.

Done when:

- Screens look consistent.
- Reusable components are used instead of duplicated UI.
- Layouts remain usable on different screen sizes.

## Phase 10: Internationalization

Goal: satisfy multilingual UI requirement.

Tasks:

- Move hardcoded UI text into `strings.xml`.
- Add French strings.
- Add English strings.
- Optionally add Arabic strings.
- Check that screens still fit after translation.

Done when:

- Main UI text is no longer hardcoded.
- At least French and English resources exist.

## Phase 11: Quality And Delivery

Goal: prepare final submission and demo.

Tasks:

- Make sure the project compiles.
- Add basic tests where possible.
- Clean unused imports.
- Add README with:
  - team member names;
  - project description;
  - main features;
  - dependencies;
  - architecture diagram;
  - screenshots.
- Add screenshots.
- Prepare 10-minute demo path.
- Prepare 5-minute Q&A notes.

Done when:

- The app has a complete demo path:
  - register/login;
  - browse restaurant;
  - add food to cart;
  - checkout;
  - pay;
  - track order;
  - partner updates order status.
- README is complete.
- Demo can be presented in 10 minutes.

## Recommended Build Order

1. Stabilize app and navigation.
2. Firestore foundation.
3. Customer restaurant/menu flow.
4. Cart.
5. Checkout and payment simulation.
6. Orders and tracking.
7. Partner dashboard.
8. Profile and social login.
9. Internationalization.
10. README, screenshots and demo preparation.
