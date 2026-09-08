# AI Power Fitness - Frontend

React + Vite frontend application for the AI Power Fitness platform.

## Tech Stack

- **React 18** - UI Library
- **Vite** - Build tool
- **React Router** - Routing
- **TailwindCSS** - Styling
- **Zustand** - State management
- **Axios** - HTTP client
- **Keycloak JS** - Authentication
- **Radix UI** - UI components
- **Lucide React** - Icons
- **Recharts** - Charts
- **date-fns** - Date utilities

## Prerequisites

- Node.js 18+
- npm or yarn

## Installation

```bash
cd frontend
npm install
```

## Environment Variables

Create a `.env` file in the root directory:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=fitness-realm
VITE_KEYCLOAK_CLIENT_ID=fitness-app
```

## Development

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## Build

```bash
npm run build
```

## Preview

```bash
npm run preview
```

## Features

- **Authentication** - Keycloak integration
- **User Profile** - Manage personal information
- **Activity Tracking** - Log workouts and exercises
- **AI Recommendations** - View AI-powered fitness insights
- **Dashboard** - Overview of fitness statistics

## Project Structure

```
src/
├── components/
│   ├── ui/           # Reusable UI components
│   ├── Layout.jsx    # Main layout component
│   └── Navbar.jsx    # Navigation bar
├── config/
│   ├── api.js        # Axios configuration
│   └── keycloak.js   # Keycloak configuration
├── pages/
│   ├── Login.jsx     # Login page
│   ├── Register.jsx  # Registration page
│   ├── Dashboard.jsx # Main dashboard
│   ├── Profile.jsx   # User profile
│   ├── Activities.jsx # Activity tracking
│   └── Recommendations.jsx # AI recommendations
├── services/
│   ├── authService.js
│   ├── activityService.js
│   └── recommendationService.js
├── store/
│   ├── useAuthStore.js
│   └── useActivityStore.js
├── lib/
│   └── utils.js
├── App.jsx
├── main.jsx
└── index.css
```

## API Integration

The frontend connects to the backend through the API Gateway at `http://localhost:8080`.

All API calls include the JWT token from Keycloak in the Authorization header.

## Keycloak Setup

1. Create a realm named `fitness-realm` in Keycloak
2. Create a client named `fitness-app` with:
   - Valid Redirect URIs: `http://localhost:3000/*`
   - Web Origins: `http://localhost:3000`
3. Enable OpenID Connect
4. Configure the client as public or confidential

## Architecture

- **State Management**: Zustand for global state
- **Routing**: React Router v6 with protected routes
- **Styling**: TailwindCSS with custom theme
- **Authentication**: Keycloak JS adapter
- **API**: Axios with interceptors for token handling

## Pages

### Login/Register
- Keycloak authentication
- Form validation
- Error handling

### Dashboard
- Activity statistics
- Recent activities
- Quick actions
- AI recommendations summary

### Activities
- Activity tracking form
- Activity list
- Activity details
- Real-time updates

### Recommendations
- AI-powered insights
- Performance analysis
- Improvement suggestions
- Safety guidelines

### Profile
- User information
- Profile editing
- Account settings
