# Authentication API Documentation

BASE URL: `https://localhost:8080`

## Endpoints

### 1. User Registration
**POST** `/auth/register`

Creates a new user account with the provided details. If no role is specified, 
the user is assigned the default role of "user", which is PATIENT.

**Request Body (PATIENT):**

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "dateOfBirth": "YYYY-MM-DD"
}
```

**Response (201 Created):**

```json
{
  "jwtToken": "Profile registered successfully",
  "username": "string",
  "roles": [
    "PATIENT"
  ],
  "email": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Request Body (EMPLOYEE):**

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "role": "EMPLOYEE"
}
```

**Response (201 Created):**

```json
{
  "message": "User registered successfully",
  "username": "string",
  "roles": [
    "EMPLOYEE"
  ],
  "email": "string",
  "firstName": "string",
  "lastName": "string"
}
```

### 2. User Login
**POST** `/auth/login`

Authenticates a user and returns a JWT token upon successful login.

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200 OK):**

```json
{
    "jwtToken": "Login successful",
    "username": "string",
    "roles": [
        "ROLE"
    ],
    "email": "string",
    "firstName": "string",
    "lastName": "string"
}
```

### 3. Logout
**POST** `/auth/logout`

Logs out the user by invalidating the JWT token.

**Response (200 OK):**

```json
{
  "message": "Logout successful"
}
```

### 4. Get profile
**GET** `/auth/profile`

Retrieves the profile information of the authenticated user.

**Request Body:**

```json
{
    "jwtToken": "Profile fetched successfully",
    "username": "string",
    "roles": [
        "ROLE"
    ],
    "email": "string",
    "firstName": "string",
    "lastName": "string"
}
```

### Authentication

All endpoints except /register and /login require jwt authentication.

### Error Responses

**400 Bad Request:**

```json
{
  "error": "Description of the bad request error"
}
```

**401 Unauthorized:**

```json
{
  "error": "Authentication failed"
}
```
