# Authentication Setup

This file tracks Phase 3 from `PROJECT_PLAN.md`: email/password auth, user profile, logout, Google login and Facebook login.

## Current App Support

Implemented in the app:

- Email/password registration.
- Email/password login.
- Firestore user profile creation at registration.
- Profile screen.
- Profile editing:
  - full name;
  - phone;
  - delivery address.
- Logout.
- Google sign-in app code:
  - Android Credential Manager prompt;
  - Firebase Auth Google credential exchange;
  - Firestore user profile creation when missing;
  - credential state clearing on logout.
- User role field:
  - `CUSTOMER` by default.
  - `PARTNER` and `RESTAURANT_ADMIN` for assigned restaurant management.
  - `CHEF` for assigned kitchen command preparation.
  - `DRIVER` for a linked delivery account.
  - `ADMIN` for global restaurant management.

## Firestore User Document

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

Role management rules:

- New users are created as `CUSTOMER`.
- `PARTNER`, `RESTAURANT_ADMIN` and `CHEF` need at least one `managedRestaurantIds` entry.
- `DRIVER` needs `driverId` set to a document from `drivers/{driverId}`.
- Only a global `ADMIN` should assign elevated roles, restaurant access or driver links.

## Google Login

App code is added, but Firebase Console setup is still required before it can work on a real device.

Important current status:

- `app/google-services.json` currently has an empty `oauth_client` list.
- The Google button will show a setup error until Firebase generates `default_web_client_id`.
- After updating the Firebase config file, no extra app code should be needed for the first test.

Firebase Console steps:

1. Open Firebase Console.
2. Go to Authentication > Sign-in method.
3. Enable Google.
4. Add a support email for the Google provider.
5. Add the Android app SHA-1 and SHA-256 fingerprints.
6. Download the updated `google-services.json`.
7. Replace `app/google-services.json`.
8. Rebuild the app.

How to get SHA fingerprints in Android Studio:

1. Open Gradle panel.
2. Run `signingReport`.
3. Copy SHA-1 and SHA-256 from the debug variant.
4. Paste them into Firebase Console > Project settings > Your apps > Android app.

Expected Firestore user document after Google login:

```text
users/{firebaseAuthUid}
```

Fields created automatically when missing:

```text
id: String
fullName: String
email: String
phone: String
address: String
role: "CUSTOMER"
managedRestaurantIds: []
driverId: ""
```

## Facebook Login

Still required later.

Firebase Console steps:

1. Create/configure a Facebook Developer app.
2. Enable Facebook in Firebase Authentication.
3. Add the Facebook App ID and App Secret.
4. Configure Android package/key hashes.
5. Add Facebook Login SDK and app code.

## Demo-Limited Note

If Google/Facebook setup is not ready before the presentation, document it in the README as prepared provider support with email/password used for the live demo.
