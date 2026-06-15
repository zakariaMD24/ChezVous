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
- Home delivery label is set to Larache for the demo.
- Home includes a `Voir tout` action next to `Restaurants partenaires`.
- The all-restaurants screen is implemented with search, sort and filter controls kept at the top while the restaurant list scrolls.
- `RestaurantCard` displays the restaurant `imageUrl` from Firestore when available, with a clean icon fallback.

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

Current implementation note:

- Checkout screen is implemented after cart.
- Delivery address form is available and can prefill from the user profile address.
- Reusable `PaymentMethodCard` is implemented.
- Payment methods include simulated card payment and cash on delivery.
- Online card payment is simulated before order creation.
- Confirming checkout creates an `orders` document through `OrderRepository`.
- Cart is cleared after successful order creation.

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

Current implementation note:

- Order history screen is implemented and listens to the current user's Firestore orders.
- Order tracking/details screen is implemented and listens to one order in real time.
- Reusable `OrderStatusStepper` is implemented.
- Reusable `DriverCard` is implemented.
- Tracking shows status, estimated delivery time, driver info, items, address, payment and totals.
- Customers can cancel orders while status is `PENDING` or `CONFIRMED`.
- Checkout success now opens the tracking screen for the created order.

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

Current implementation note:

- User roles `CUSTOMER`, `PARTNER` and `ADMIN` are supported in the user profile model.
- Home shows the partner dashboard shortcut for users with role `PARTNER` or `ADMIN`.
- Partner dashboard is implemented with restaurant selection.
- Partner dashboard shows incoming restaurant orders in real time.
- Partners can update order status through the normal delivery flow.
- Partner order status updates reuse Firestore, so customer tracking updates live.
- Partners can add menu items, edit name/description/category/price and update availability.
- Firestore rules already require partner/admin role for menu and order management writes.

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

Current implementation note:

- Final brand color tokens are centralized in `ui/theme/Color.kt`.
- Typography, shapes, spacing, sizes and elevations are centralized in `ui/theme`.
- Dynamic Material colors are disabled by default so ChezVous keeps a consistent visual identity.
- Shared components now own repeated UI styling:
  - `ChezVousCard`;
  - `ChezVousTopBar`;
  - `ChezVousButton`;
  - `ChezVousTextField`;
  - `ChezVousPasswordField`;
  - `ChezVousSearchBar`;
  - `CategoryChip`;
  - `FilterSortActionRow`;
  - order, cart, payment, restaurant, food, driver and partner cards.
- Screen and sheet padding helpers are available in `ChezVousLayout.kt`.
- Home, auth, profile, cart, checkout, order history, tracking, restaurant details and partner dashboard now reuse shared design tokens/components.
- Auth layout has a maximum form width to stay usable on larger screens.
- Home screen keeps a simple structured layout:
  - compact top bar;
  - bottom navigation for the main app sections;
  - compact food-focused hero area;
  - greeting;
  - search;
  - separate sort/filter controls;
  - active filter chips;
  - dynamic cuisine chips;
  - larger image-first restaurant cards.
- Restaurant details now uses the restaurant image from Firestore in the header.
- Filter sheets now use simple tappable chips for easier mobile use instead of slider-heavy controls.
- Cart items support customer instructions such as "sans tomate" or "peu de sauce"; the notes stay with the order and appear in tracking/partner views.
- Food item customization is implemented:
  - extras such as egg, cheese, extra protein and sauce;
  - removed ingredients such as tomato, onion, sauce, salad and cheese;
  - spice level selection;
  - custom instruction text.
- Customization now has an image-backed bottom sheet with food imagery, visual extra cards, visual removable ingredient cards, a slider-style spice level panel and a base/extras/total price summary.
- Customization content is database-backed:
  - `extraOptions`;
  - `removableIngredientOptions`;
  - `spiceLevelOptions`.
- Each customization option can store `id`, `name`, `price`, `imageUrl` and `description`.
- Partners can manage menu item image URLs, extra option names/prices/images/descriptions, removable ingredient names/images/descriptions and spice level names/images/descriptions from the menu editor.
- Customized items are stored as separate cart lines, saved in Firestore orders and shown to partners.
- Drinks are modeled as normal menu items using the `Boissons` category and appear in a dedicated drinks section at the end of the restaurant menu.
- Partner/admin users can view menus, but customer ordering is disabled in partner mode.

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

Current implementation note:

- Default French strings and English strings are created.
- Home browsing text has started moving to resources:
  - delivery location;
  - search placeholders;
  - restaurant section titles;
  - filter and sort labels;
  - empty states;
  - navigation/action labels.
- The restaurant browsing flow now uses localized labels for the "all cuisines" chip and sort options.
- New home, menu filter, bottom navigation and cart instruction labels are stored in French and English resources.
- Customization labels and summaries are stored in French and English resources.
- Remaining screens still need the same string migration pass:
  - auth;
  - profile;
  - cart;
  - checkout;
  - orders/tracking;
  - partner dashboard.

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
